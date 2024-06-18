package classes.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import classes.mvc.Mapping;

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
        } catch (Exception e) {
            System.err.println(e);
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
    protected String setStringer(String attribut){
        return "set" + Character.toUpperCase(attribut.charAt(0)) + attribut.substring(1);
    }
    protected Object typage(String paramValue ,String paramName, Class paramType){
        Object o = null ;
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
            o =Boolean.parseBoolean(paramValue);
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
        if (parameters[i].isAnnotationPresent(Annotations.AnnotationParameter.class)) {
            paramName = parameters[i].getAnnotation(Annotations.AnnotationParameter.class).value();
        } else {
            paramName = parameters[i].getName();
        }

        Class<?> paramType = parameters[i].getType();

      
            String paramValue = request.getParameter(paramName);
            if (paramValue == null) {
                throw new IllegalArgumentException("Missing parameter: " + paramName);
            }
            methodParams[i] = typage(paramValue, paramName, paramType);
       
    }
    return methodParams;
}

    
    protected void callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Class<?> clazz = Class.forName(mapping.getController());
            Object object = clazz.newInstance(); 

            Method method = clazz.getMethod(mapping.getMethod(), mapping.getParameterTypes());
            method.setAccessible(true);

            Object[] methodParams = getMethodParams(method,request); 
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
            throw new IllegalArgumentException("URL not found :" +requestURI);
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
                    ", Parameter Types : " + Arrays.toString(entry.getValue().getParameterTypes()) +"</li>");
                }
                out.println("</ul>");
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
