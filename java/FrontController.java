package classes.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson; 

import annotations.Restapi;

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
// Cette méthode extrait les paramètres d'une méthode donnée et les associe avec les paramètres de la requête HTTP.
protected Object[] getMethodParams(Method method, HttpServletRequest request) throws IllegalArgumentException {
    // Récupération des paramètres de la méthode
    Parameter[] parameters = method.getParameters();
    // Création d'un tableau pour stocker les valeurs des paramètres
    Object[] methodParams = new Object[parameters.length];

    // Boucle sur chaque paramètre de la méthode
    for (int i = 0; i < parameters.length; i++) {
        String paramName = "";
        Class<?> paramType = parameters[i].getType(); // Type du paramètre

        // Si le paramètre est de type MySession, on l'instancie à partir de la session
        if (paramType == MySession.class) {
            methodParams[i] = new MySession(request.getSession());
            continue;
        }

        // Vérification si le paramètre est annoté avec @AnnotationParameter pour récupérer son nom
        if (parameters[i].isAnnotationPresent(Annotations.AnnotationParameter.class)) {
            paramName = parameters[i].getAnnotation(Annotations.AnnotationParameter.class).value();
        } else {
            // Si l'annotation est manquante, on lève une exception
            paramName = parameters[i].getName();
            throw new IllegalArgumentException("ERROR ANNOTATION ETU002751 test: " + paramName);
        }

        // Gérer les objets personnalisés (non primitifs et non String)
        if (!paramType.isPrimitive() && !paramType.equals(String.class)) {
            try {
                // Création d'une instance de l'objet personnalisé
                Object paramObject = paramType.getDeclaredConstructor().newInstance();
                Field[] fields = paramType.getDeclaredFields(); // Récupération des champs de l'objet

                // Pour chaque champ, on cherche la valeur correspondante dans la requête
                for (Field field : fields) {
                    String fieldName = field.getName();
                    String fieldValue = request.getParameter(paramName + "." + fieldName);
                    if (fieldValue != null) {
                        field.setAccessible(true);
                        Object typedValue = typage(fieldValue, fieldName, field.getType()); // Conversion au bon type
                        field.set(paramObject, typedValue); // On affecte la valeur au champ
                    }
                }
                methodParams[i] = paramObject; // On ajoute l'objet à la liste des paramètres
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException("Error creating parameter object: " + paramName, e);
            }
        } else {
            // Gestion des types primitifs et String
            String paramValue = request.getParameter(paramName);
            if (paramValue == null) {
                throw new IllegalArgumentException("Missing parameter: " + paramName);
            }
            methodParams[i] = typage(paramValue, paramName, paramType); // Conversion au bon type et ajout dans le tableau
        }
    }

    return methodParams; // Retourne le tableau des paramètres prêts pour l'appel
}

// Cette méthode appelle une méthode spécifiée par le mapping en passant les bons paramètres
protected void callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
        // Chargement de la classe du contrôleur depuis le nom de la classe
        Class<?> clazz = Class.forName(mapping.getController());
        Object object = clazz.newInstance(); // Création d'une instance du contrôleur

        // Récupération de la méthode à appeler avec ses paramètres
        Method method = clazz.getMethod(mapping.getMethod(), mapping.getParameterTypes());
        method.setAccessible(true); // On permet d'accéder à la méthode

        // Récupération des paramètres de la méthode
        Object[] methodParams = getMethodParams(method, request); 
        Object result = method.invoke(object, methodParams); // Appel de la méthode avec les paramètres

        // Vérification si la méthode est annotée avec @Restapi pour savoir comment traiter le retour
        if (method.isAnnotationPresent(Restapi.class)) {
            Gson gson = new Gson(); // Utilisation de Gson pour convertir en JSON
            response.setContentType("application/json");
            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                String jsonData = gson.toJson(modelView.getData());
                out.print(jsonData); // Renvoyer "data" sous forme de JSON
            } else {
                String jsonData = gson.toJson(result);
                out.print(jsonData); // Renvoyer l'objet sous forme de JSON
            }
            out.flush();
        } else {
            // Si ce n'est pas une API REST, on traite le résultat en tant que vue ou chaîne
            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                settingAttribute(modelView, request); // Mettre en place les attributs du modèle
                RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                dispatcher.forward(request, response); // Redirection vers la vue
            } else if (result instanceof String) {
                out.println("<li>Results: " + result + "</li>");
            } else {
                throw new IllegalArgumentException("Unsupported type: " + result.getClass().getName());
            }
        }
    } catch (Exception e) {
        e.printStackTrace(response.getWriter()); // Gestion des erreurs
    }
}

// Valide si le mapping existe et appelle la méthode correspondante
protected void validateMapping(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestURI = request.getRequestURI();
    if (mapping != null) {
        out.println("<h2>Mapping Found:</h2>");
        out.println("<ul>");
        out.println("<li>Controller: " + mapping.getController() + "</li>");
        out.println("<li>Method: " + mapping.getMethod() + "</li>");

        try {
            this.callMethod(mapping, out, request, response); // Appelle la méthode si le mapping est trouvé
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(response.getWriter());
        }

        out.println("</ul>");
    } else {
        throw new IllegalArgumentException("URL not found :" + requestURI); // Si aucun mapping n'est trouvé
    }
}

// Traite la requête principale en cherchant le mapping correspondant à l'URL
protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    if (this.urlMappings != null) {
        String requestURI = request.getRequestURI();
        Mapping mapping = this.urlMappings.get(requestURI); // Recherche du mapping correspondant à l'URL
        PrintWriter out = response.getWriter();
        try {
            out.println("<h1>Hello, World!</h1>");
            out.println("<h1>LINK : " + request.getRequestURL() + "</h1>");
            out.println("<h2>List of Controllers and Methods:</h2>");
            out.println("<ul>");
            // Affiche tous les mappings (URL, contrôleur et méthode)
            for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
                out.println("<li>URL: " + entry.getKey() + ", Controller: " + entry.getValue().getController() + ", Method: " + entry.getValue().getMethod() + 
                ", Parameter Types : " + Arrays.toString(entry.getValue().getParameterTypes()) + "</li>");
            }
            out.println("</ul>");
            this.validateMapping(mapping, out, request, response); // Valide et appelle la méthode correspondante au mapping
        } catch (Exception e) {
            e.printStackTrace(response.getWriter()); // Gestion des erreurs
        }
    }
}

// Méthode appelée pour traiter les requêtes POST
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    this.processRequest(request, response); // Appelle la méthode de traitement principale
}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }
}
