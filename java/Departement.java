package classes.controllers;

import classes.Annotations.AnnotationController;
import classes.Annotations.AnnotationMethod;

@AnnotationController()
public class Departement {
    String nom;
    int numero;
    @AnnotationMethod("/TEST_FRAMEWORK/listDept")
    public void getListDepartement(){
        
    }
}
