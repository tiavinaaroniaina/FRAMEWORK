package classes.mvc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScannerController {

    // Adjusted to return Map<String, List<Mapping>> to allow multiple mappings per
    // URL
    public Map<String, List<Mapping>> scanPackages(String packages) throws ClassNotFoundException, IOException {
        if (packages == null || packages.trim().isEmpty()) {
            throw new IllegalArgumentException("Packages cannot be null or empty");
        }

        Map<String, List<Mapping>> controllers = new HashMap<>();
        String[] packageArray = packages.split(",");
        for (String packageName : packageArray) {
            packageName = packageName.trim();
            if (!packageName.isEmpty()) {
                controllers.putAll(scanPackage(packageName));
            }
        }

        if (controllers.isEmpty()) {
            throw new IOException("No classes found in the provided packages: " + packages);
        }

        return controllers;
    }

    // Adjusted to return Map<String, List<Mapping>> to store multiple mappings for
    // the same URL
    private Map<String, List<Mapping>> scanPackage(String packageName) throws ClassNotFoundException, IOException {
        Map<String, List<Mapping>> controllers = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String decodedPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
            dirs.add(new File(decodedPath));
        }

        if (dirs.isEmpty()) {
            throw new IOException("No package found for the name: " + packageName);
        }

        for (File directory : dirs) {
            controllers.putAll(findClasses(directory, packageName, controllers));
        }

        // Log all URL mappings and associated HTTP verbs and methods
        for (Map.Entry<String, List<Mapping>> entry : controllers.entrySet()) {
            String url = entry.getKey();
            List<Mapping> mappings = entry.getValue();
            for (Mapping mapping : mappings) {
                System.out.println("URL: " + url + " => Controller: " + mapping.getController()
                        + " | Method: " + mapping.getMethod() + " | Verb: " + mapping.getVerb());
            }
        }

        return controllers;
    }

    // Adjusted to return Map<String, List<Mapping>> and to handle multiple mappings
    // for the same URL
    private Map<String, List<Mapping>> findClasses(File directory, String packageName,
            Map<String, List<Mapping>> existingMappings)
            throws ClassNotFoundException {
        Map<String, List<Mapping>> classes = new HashMap<>();
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.putAll(findClasses(file, packageName + "." + file.getName(), existingMappings));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class
                        .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.isAnnotationPresent(Annotations.AnnotationController.class)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Annotations.AnnotationMethod.class)) {
                            Annotations.AnnotationMethod annotation = method
                                    .getAnnotation(Annotations.AnnotationMethod.class);
                            String url = annotation.value();

                            VerbMethod verbMethod = null;
                            if (method.isAnnotationPresent(Verb.GET.class)) {
                                verbMethod = new VerbMethod("GET", method.getName());
                            }
                            if (method.isAnnotationPresent(Verb.POST.class)) {
                                verbMethod = new VerbMethod("POST", method.getName());
                            }

                            if (verbMethod == null) {
                                verbMethod = new VerbMethod("GET", method.getName()); // Default to GET
                            }

                            Mapping mapping = new Mapping();
                            mapping.setController(clazz.getName());
                            mapping.setMethod(method.getName());
                            mapping.setParameterTypes(method.getParameterTypes());
                            mapping.setSession(null);
                            mapping.setRestApi(method.isAnnotationPresent(Annotations.RestApi.class));
                            mapping.setVerb(verbMethod.getVerb());

                            for (Field field : clazz.getDeclaredFields()) {
                                field.setAccessible(true);
                                if (field.getType() == Session.class) {
                                    mapping.setSession(field.getName());
                                }
                            }

                            // Check if URL already exists
                            if (existingMappings.containsKey(url)) {
                                // Check if the same verb is already mapped to this URL
                                List<Mapping> existingMappingList = existingMappings.get(url);
                                for (Mapping existingMapping : existingMappingList) {
                                    if (existingMapping.getVerb().equals(mapping.getVerb())) {
                                        // Throw an exception if the same URL and verb combination already exists
                                        throw new IllegalArgumentException("Duplicate mapping for URL: " + url
                                                + " with verb: " + mapping.getVerb());
                                    }
                                }
                                // If no duplicate verb is found, add the new mapping
                                existingMappingList.add(mapping);
                            } else {
                                // Create a new list for this URL if it doesn't exist
                                List<Mapping> mappingList = new ArrayList<>();
                                mappingList.add(mapping);
                                existingMappings.put(url, mappingList);
                            }

                        }
                    }
                }
            }
        }
        return classes;
    }
}
