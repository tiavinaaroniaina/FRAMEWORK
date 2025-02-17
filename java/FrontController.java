package classes.mvc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.annotation.MultipartConfig;

import com.google.gson.Gson;

@MultipartConfig
public class FrontController extends HttpServlet {
    private String controllerPackage;
    private ScannerController scanner = new ScannerController();
    private Map<String, List<Mapping>> urlMappings; // Change to List<Mapping> to handle multiple verbs

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        this.controllerPackage = context.getInitParameter("base_package");
        try {
            this.urlMappings = scanner.scanPackages(controllerPackage);
            validateMappings(); // Call validate after scanning
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace(); // Log the error or handle it appropriately
        }
    }

    // New method to validate mappings for duplicates
    private void validateMappings() {
        Map<String, Set<String>> urlVerbMap = new HashMap<>();

        for (Map.Entry<String, List<Mapping>> entry : urlMappings.entrySet()) {
            String url = entry.getKey();
            for (Mapping mapping : entry.getValue()) {
                String verb = mapping.getVerb(); // Get the verb

                // Check for duplicate URL and verb
                Set<String> verbs = urlVerbMap.computeIfAbsent(url, k -> new HashSet<>());

                // If the verb already exists, throw an exception
                if (!verbs.add(verb)) { // add returns false if the item was already in the set
                    throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.DUPLICATE_MAPPING));
                }
            }
        }
    }

    protected void validateMapping(Mapping mapping, PrintWriter out, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if (mapping != null) {
            try {
                String requestMethod = request.getMethod();
                // Ensure the request method matches the mapping verb
                if (!mapping.getVerb().equalsIgnoreCase(requestMethod)) {
                    throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.METHOD_NOT_ALLOWED));
                }
                this.callMethod(mapping, out, request, response);
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace(response.getWriter());
            }
        } else {
            throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.URL_NOT_FOUND));
        }
    }

    protected void callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Class<?> clazz = Class.forName(mapping.getController());
            Object object = clazz.getDeclaredConstructor().newInstance();

            if (mapping.getSession() != null) {
                Field field = object.getClass().getDeclaredField(mapping.getSession());
                field.setAccessible(true);
                field.set(object, new Session(request.getSession()));
            }

            Method method = clazz.getMethod(mapping.getMethod(), mapping.getParameterTypes());
            method.setAccessible(true);
            Object[] methodParams = getMethodParams(method, request);
            Object result = method.invoke(object, methodParams);

            // Handle the result of the invoked method
            handleResult(result, mapping, request, response, out);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void handleResult(Object result, Mapping mapping, HttpServletRequest request,
            HttpServletResponse response, PrintWriter out) throws IOException, ServletException {
        if (result instanceof String) {
            String resultString = (String) result;
            if (mapping.isRestApi()) {
                response.setContentType("application/json");
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("result", resultString);
                response.getWriter().write(new Gson().toJson(responseMap));
                response.getWriter().flush();
                return;
            } else {
                out.println("<li>Results: " + resultString + "</li>");
                out.println("<li>Type: " + resultString.getClass().getName() + "</li>");
            }
        } else if (result instanceof ModelView) {
            ModelView modelView = (ModelView) result;
            if (mapping.isRestApi()) {
                response.setContentType("application/json");
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("url", modelView.getUrl());
                responseMap.put("data", modelView.getData());
                response.getWriter().write(new Gson().toJson(responseMap));
                response.getWriter().flush();
                return;
            } else {
                settingAttribute(modelView, request);
                RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                dispatcher.forward(request, response);
            }
        } else {
            throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.UNSUPPORTED_TYPE));
        }
    }

    protected void settingAttribute(ModelView mv, HttpServletRequest request) {
        if (mv.getData() instanceof HashMap) {
            HashMap<String, Object> dataMap = (HashMap<String, Object>) mv.getData();
            for (HashMap.Entry<String, Object> entry : dataMap.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    protected String setStringer(String attribute) {
        return "set" + Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
    }

    protected Object typage(String paramValue, String paramName, Class<?> paramType) {
        if (paramType == Date.class || paramType == java.sql.Date.class) {
            return java.sql.Date.valueOf(paramValue);
        } else if (paramType == int.class) {
            return Integer.parseInt(paramValue);
        } else if (paramType == double.class) {
            return Double.parseDouble(paramValue);
        } else if (paramType == boolean.class) {
            return Boolean.parseBoolean(paramValue);
        } else {
            return paramValue;
        }
    }

    protected FileUpload handleFileUpload(HttpServletRequest request, String inputFileParam)
            throws IOException, ServletException {
        Part filePart = request.getPart(inputFileParam);
        String fileName = extractFileName(filePart);
        byte[] fileContent = filePart.getInputStream().readAllBytes();

        String uploadDir = request.getServletContext().getRealPath("") + "uploads/" + fileName;
        System.out.println("upload = " + uploadDir);

        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String uploadPath = uploadDir + File.separator + fileName;
        System.out.println("upload = " + uploadPath);

        filePart.write(uploadPath);

        return new FileUpload(fileName, uploadPath, fileContent);
    }

    protected String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return "";
    }

    protected Object[] getMethodParams(Method method, HttpServletRequest request) throws IllegalArgumentException {
        Parameter[] parameters = method.getParameters();
        Object[] methodParams = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            String paramName = parameters[i].isAnnotationPresent(Annotations.AnnotationParameter.class)
                    ? parameters[i].getAnnotation(Annotations.AnnotationParameter.class).value()
                    : parameters[i].getName();

            // Handling file upload (Part type)
            if (paramType == FileUpload.class) {
                try {
                    methodParams[i] = handleFileUpload(request, paramName);
                } catch (IOException | ServletException e) {
                    throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.FILE_UPLOAD_ERROR), e);
                }
            } else if (paramType == Session.class) {
                methodParams[i] = new Session(request.getSession());
            } else if (!paramType.isPrimitive() && !paramType.equals(String.class)) {
                try {
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
                    for (Field field : paramType.getDeclaredFields()) {
                        String fieldName = field.getName();
                        String fieldValue = request.getParameter(paramName + "." + fieldName);
                        if (fieldValue != null) {
                            field.setAccessible(true);
                            Object typedValue = typage(fieldValue, fieldName, field.getType());
                            Method setterMethod = paramObject.getClass().getDeclaredMethod(setStringer(fieldName),
                                    field.getType());
                            setterMethod.invoke(paramObject, typedValue);
                        }
                    }
                    methodParams[i] = paramObject;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    throw new IllegalArgumentException(
                            ErrorCode.getErrorMessage(ErrorCode.ERROR_CREATING_PARAMETER_OBJECT));
                }
            } else {
                String paramValue = request.getParameter(paramName);
                if (paramValue == null) {
                    throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.MISSING_PARAMETER));
                }
                methodParams[i] = typage(paramValue, paramName, paramType);
            }
        }
        return methodParams;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        if (this.urlMappings != null) {
            String requestURI = request.getRequestURI();
            List<Mapping> mappings = this.urlMappings.get(requestURI);
            PrintWriter out = response.getWriter();
            try {
                if (mappings != null && !mappings.isEmpty()) {
                    Mapping mapping = findMatchingMapping(mappings, request.getMethod());
                    this.validateMapping(mapping, out, request, response);
                } else {
                    throw new IllegalArgumentException(ErrorCode.getErrorMessage(ErrorCode.URL_NOT_FOUND));
                }
            } catch (Exception e) {
                e.printStackTrace(response.getWriter());
            }
        }
    }

    protected Mapping findMatchingMapping(List<Mapping> mappings, String requestMethod) {
        for (Mapping mapping : mappings) {
            if (mapping.getVerb().equalsIgnoreCase(requestMethod)) {
                return mapping;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
