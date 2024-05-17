package classes;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationController {
    String value(); // Attribut pour représenter le nom du contrôleur
}
