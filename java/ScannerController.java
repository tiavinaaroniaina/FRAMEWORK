package classes;

import classes.Annotations.AnnotationController;
import classes.Annotations.AnnotationMethod;

import java.io.File;
import java.io.IOException;
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

    public Map<String, Mapping> scanPackage(String packageName) throws ClassNotFoundException, IOException {
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

        for (File directory : dirs) {
            controllers.putAll(findClasses(directory, packageName));
        }

        return controllers;
    }

    private Map<String, Mapping> findClasses(File directory, String packageName) throws ClassNotFoundException {
        Map<String, Mapping> classes = new HashMap<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.putAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.isAnnotationPresent(AnnotationController.class)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(AnnotationMethod.class)) {
                            AnnotationMethod annotation = method.getAnnotation(AnnotationMethod.class);
                            Mapping mapping = new Mapping();
                            mapping.setController(clazz.getName());
                            mapping.setMethod(method.getName());
                            classes.put(annotation.value(), mapping);
                        }
                    }
                }
            }
        }
        return classes;
    }
}
