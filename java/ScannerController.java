package classes.mvc;

import classes.mvc.Mapping;
import classes.mvc.Session;
import classes.mvc.Annotations.AnnotationController;
import classes.mvc.Annotations.AnnotationMethod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerController {

    public Map<String, Mapping> scanPackages(String packages) throws ClassNotFoundException, IOException {
        if (packages == null || packages.trim().isEmpty()) {
            throw new IllegalArgumentException("Packages cannot be null or empty");
        }

        Map<String, Mapping> controllers = new HashMap<>();
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

    private Map<String, Mapping> scanPackage(String packageName) throws ClassNotFoundException, IOException {
        Map<String, Mapping> controllers = new HashMap<>();
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

        return controllers;
    }

    private Map<String, Mapping> findClasses(File directory, String packageName, Map<String, Mapping> existingMappings) throws ClassNotFoundException {
        Map<String, Mapping> classes = new HashMap<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.putAll(findClasses(file, packageName + "." + file.getName(), existingMappings));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.isAnnotationPresent(AnnotationController.class)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(AnnotationMethod.class)) {
                            AnnotationMethod annotation = method.getAnnotation(AnnotationMethod.class);
                            String url = annotation.value();
                            if (existingMappings.containsKey(url)) {
                                throw new IllegalArgumentException("Duplicate URL detected: " + url);
                            }
                            
                            Mapping mapping = new Mapping();
                            mapping.setController(clazz.getName());
                            mapping.setMethod(method.getName());
                            mapping.setParameterTypes(method.getParameterTypes());
                            mapping.setSession(null);
                            for (Field field : clazz.getDeclaredFields()) {
                                field.setAccessible(true);
                                if (field.getType() == Session.class) {
                                    mapping.setSession(field.getName());
                                }
                            }
                            classes.put(url, mapping);
                            existingMappings.put(url, mapping);
                        }
                    }
                }
            }
        }
        return classes;
    }
}
