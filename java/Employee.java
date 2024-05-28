package classes.controllers;

import classes.Annotations.AnnotationController;
import classes.Annotations.AnnotationMethod;

@AnnotationController()
public class Employee {
    String nom;
    int age;
    @AnnotationMethod("/TEST_FRAMEWORK/listEmp")
    public void getListEmployer(){
        
    }
}