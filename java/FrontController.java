package classes.mvc;

import javax.servlet.*;
import javax.servlet.http.*;

import classes.mvc.MySession;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;

public class FrontController extends HttpServlet {
    private String controllerPackage;
    private ScannerController scanner = new ScannerController();
    private Map<String, Mapping> urlMappings;

    public void init(ServletConfig config) throws ServletException {
        try {
            super.init(config);
            ServletContext context = config.getServletContext();
            this.controllerPackage = context.getInitParameter("base_package");
            this.urlMappings = scanner.scanPackages(controllerPackage);
            
            // Log the URL mappings to verify they are loaded correctly
            System.out.println("URL Mappings loaded: " + this.urlMappings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void settingAttribute(ModelView mv, HttpServletRequest request) {
        if (mv.getData() instanceof HashMap) {
            HashMap<String, Object> dataMap = (HashMap<String, Object>) mv.getData();

            for (HashMap.Entry<String, Object> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                request.setAttribute(key, value);
            }
        }
    }

    protected String setStringer(String attribut) {
        return "set" + Character.toUpperCase(attribut.charAt(0)) + attribut.substring(1);
    }

    protected Object typage(String paramValue, String paramName, Class<?> paramType) {
        Object o = null;
        if (paramType == Date.class || paramType == java.sql.Date.class) {
            try {
                o = java.sql.Date.valueOf(paramValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid date format for parameter: " + paramName);
            }
        } else if (paramType == int.class) {
            o = Integer.parseInt(paramValue);
        } else if (paramType == double.class) {
            o = Double.parseDouble(paramValue);
        } else if (paramType == boolean.class) {
            o = Boolean.parseBoolean(paramValue);
        } else {
            o = paramValue;
        }
        return o;
    }

    protected Object[] getMethodParams(Method method, HttpServletRequest request) throws IllegalArgumentException {
        Parameter[] parameters = method.getParameters();
        Object[] methodParams = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            String paramName = "";
            Class<?> paramType = parameters[i].getType();
            if(paramType==MySession.class)
            {
               methodParams[i]=new MySession(request.getSession());
               continue;
            }
            if (parameters[i].isAnnotationPresent(Annotations.AnnotationParameter.class)) {
                paramName = parameters[i].getAnnotation(Annotations.AnnotationParameter.class).value();
            } else {
                paramName = parameters[i].getName();
                throw new IllegalArgumentException(" ERROR ANNOTATION ETU002751 test: " + paramName);
            }


            if (paramType == MySession.class) {
                methodParams[i] = new MySession(request.getSession());
            } else if (!paramType.isPrimitive() && !paramType.equals(String.class)) {
                try {
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
                    Field[] fields = paramType.getDeclaredFields();

                    for (Field field : fields) {
                        String fieldName = field.getName();
                        String fieldValue = request.getParameter(paramName + "." + fieldName);
                        if (fieldValue != null) {
                            field.setAccessible(true);
                            Object typedValue = typage(fieldValue, fieldName, field.getType());
                            field.set(paramObject, typedValue);
                        }
                    }
                    methodParams[i] = paramObject;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new IllegalArgumentException("Error creating parameter object: " + paramName, e);
                }
            } else {
                String paramValue = request.getParameter(paramName);
                if (paramValue == null) {
                    throw new IllegalArgumentException("Missing parameter: " + paramName);
                }
                methodParams[i] = typage(paramValue, paramName, paramType);
            }
        }
        return methodParams;
    }

    protected void callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Class<?> clazz = Class.forName(mapping.getController());
            Object object = clazz.getDeclaredConstructor().newInstance();

            Method method = clazz.getMethod(mapping.getMethod(), mapping.getParameterTypes());
            method.setAccessible(true);

            Object[] methodParams = getMethodParams(method, request);
            Object result = method.invoke(object, methodParams);

            if (result instanceof String) {
                String resultString = (String) result;
                out.println("<li>Results: " + resultString + "</li>");
                out.println("<li>Type: " + resultString.getClass().getName() + "</li>");
            } else if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                out.println("<li>Type: " + modelView.getClass().getName() + "</li>");
                settingAttribute(modelView, request);
                RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                dispatcher.forward(request, response);
            } else {
                throw new IllegalArgumentException("Unsupported type : " + result.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        }
    }

    protected void validateMapping(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if (mapping != null) {
            out.println("<h2>Mapping Found:</h2>");
            out.println("<ul>");
            out.println("<li>Controller: " + mapping.getController() + "</li>");
            out.println("<li>Method: " + mapping.getMethod() + "</li>");

            try {
                this.callMethod(mapping, out, request, response);
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace(response.getWriter());
            }

            out.println("</ul>");
        } else {
            throw new IllegalArgumentException("URL not found :" + requestURI);
        }
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        if (this.urlMappings != null) {
            String requestURI = request.getRequestURI();
            Mapping mapping = this.urlMappings.get(requestURI);
            PrintWriter out = response.getWriter();
            try {
                out.println("<h1>Hello, World!</h1>");
                out.println("<h1>LINK : " + request.getRequestURL() + "</h1>");
                out.println("<h2>List of Controllers and Methods:</h2>");
                out.println("<ul>");
                for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
                    out.println("<li>URL: " + entry.getKey() + ", Controller: " + entry.getValue().getController() + ", Method: " + entry.getValue().getMethod() + 
                    ", Parameter Types : " + Arrays.toString(entry.getValue().getParameterTypes()) + "</li>");
                }
                out.println("</ul>");
    
                // Log the request URI to verify it matches a defined mapping
                System.out.println("Processing request for URI: " + requestURI);
    
                this.validateMapping(mapping, out, request, response);
            } catch (Exception e) {
                e.printStackTrace(response.getWriter());
            }
        }
    }
    

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }
}
