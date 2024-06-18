package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

import util.Annotation.AnnotationController;
import util.Mapping;

public class Util {
    public static List<Class<?>> listeController;

    public static HashMap<String, Mapping> getUrlMapping(List<Class<?>> listeController) {
        HashMap<String, Mapping> result = new HashMap<>();
        for (Class<?> class1 : listeController) {
            Method[] methods = class1.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(util.Annotation.Get.class)) {
                    String url = method.getAnnotation(util.Annotation.Get.class).value();
                    if (result.containsKey(url)) {
                        throw new IllegalArgumentException("Duplicate URL found: " + url);
                    }
                    result.put(url, new Mapping(class1.getName(), method.getName()));
                }
            }
        }
        return result;
    }

    public static List<Class<?>> allMappingUrls(String packageNames, Class<? extends Annotation> annotationClass, ServletContext context) {
        listeController = new ArrayList<>();
        String[] packages = packageNames.split(",");
        boolean foundPackage = false;

        for (String packageName : packages) {
            packageName = packageName.trim();
            String path = "/WEB-INF/classes/" + packageName.replace('.', '/');

            Set<String> classNames = context.getResourcePaths(path);
            if (classNames == null) {
                continue; // Try the next package
            }

            foundPackage = true;

            for (String className : classNames) {
                if (className.endsWith(".class")) {
                    String fullClassName = packageName + "." + className.substring(path.length() + 1, className.length() - 6).replace('/', '.');
                    try {
                        Class<?> clazz = Class.forName(fullClassName);
                        Annotation annotation = clazz.getAnnotation(annotationClass);
                        if (annotation instanceof AnnotationController) {
                            AnnotationController controllerAnnotation = (AnnotationController) annotation;
                            listeController.add(clazz);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }

        if (!foundPackage) {
            throw new IllegalArgumentException("None of the specified packages were found or none are in the init parameter: " + packageNames);
        }

        return listeController;
    }
}
