package classes.controllers;

import classes.mvc.Annotations.AnnotationController;
import classes.mvc.Annotations.AnnotationMethod;

@AnnotationController
public class Departement {
   String nom;
   int numero;

   @AnnotationMethod("/TEST_FRAMEWORK/listDept")
   public String getListDepartement() {
      return "Calling getListDepartement";
   }
   @AnnotationMethod("/TEST_FRAMEWORK/numberDept")
   public int getNumberDepartement() {
      return 10;
   }
}
