package classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Annotations {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationController {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationMethod {
        String value(); 
    }
}
