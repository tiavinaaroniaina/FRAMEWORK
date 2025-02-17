package classes.mvc;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.regex.*;

// Classe Validation avec les annotations internes
public class Validation {

    // Annotation de validation pour l'email
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Email {
        String message() default "L'adresse e-mail est invalide";
    }

    // Annotation de validation pour la longueur
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Length {
        int min() default 1;

        int max() default 255;

        String message() default "La longueur de la chaîne est invalide";
    }

    // Annotation de validation pour les valeurs numériques
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Value {
        double min() default Double.MIN_VALUE;

        double max() default Double.MAX_VALUE;

        String message() default "La valeur numérique est invalide";
    }

    // Annotation de validation pour les dates
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Date {
        String format() default "yyyy-MM-dd"; // Format attendu

        String message() default "La date est invalide";
    }

    // Méthode principale pour valider un objet
    public static boolean validate(Object object) throws IllegalAccessException {
        boolean isValid = true;

        // Récupérer toutes les variables déclarées dans la classe de l'objet
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // Rendre les champs accessibles, même s'ils sont privés

            // Vérifier si un champ est annoté avec @Email
            if (field.isAnnotationPresent(Email.class)) {
                Object value = field.get(object);
                if (value instanceof String) {
                    String email = (String) value;
                    Email annotation = field.getAnnotation(Email.class);
                    if (!isValidEmail(email)) {
                        System.out.println(annotation.message());
                        isValid = false;
                    }
                }
            }

            // Vérifier si un champ est annoté avec @Length
            if (field.isAnnotationPresent(Length.class)) {
                Object value = field.get(object);
                if (value instanceof String) {
                    String strValue = (String) value;
                    Length annotation = field.getAnnotation(Length.class);
                    if (!isValidLength(strValue, annotation.min(), annotation.max())) {
                        System.out.println(annotation.message());
                        isValid = false;
                    }
                }
            }

            // Vérifier si un champ est annoté avec @Value
            if (field.isAnnotationPresent(Value.class)) {
                Object value = field.get(object);
                if (value instanceof Number) {
                    double numericValue = ((Number) value).doubleValue();
                    Value annotation = field.getAnnotation(Value.class);
                    if (!isValidValue(numericValue, annotation.min(), annotation.max())) {
                        System.out.println(annotation.message());
                        isValid = false;
                    }
                }
            }

            // Vérifier si un champ est annoté avec @Date
            if (field.isAnnotationPresent(Date.class)) {
                Object value = field.get(object);
                if (value instanceof String) {
                    String date = (String) value;
                    Date annotation = field.getAnnotation(Date.class);
                    if (!isValidDate(date, annotation.format())) {
                        System.out.println(annotation.message());
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    // Vérifier si l'email est valide
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Vérifier si la longueur du texte est dans les limites spécifiées
    private static boolean isValidLength(String value, int min, int max) {
        return value != null && value.length() >= min && value.length() <= max;
    }

    // Vérifier si la valeur est dans les limites spécifiées
    private static boolean isValidValue(double value, double min, double max) {
        return value >= min && value <= max;
    }

    // Vérifier si la date est valide selon le format
    private static boolean isValidDate(String date, String format) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
            sdf.setLenient(false); // Ne pas accepter les dates invalides comme le 32/12/2024
            sdf.parse(date);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }
}
