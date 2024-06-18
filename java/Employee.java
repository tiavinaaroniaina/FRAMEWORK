package classes.controllers;

import classes.mvc.ModelView;
import classes.mvc.Annotations.AnnotationController;
import classes.mvc.Annotations.AnnotationMethod;
import classes.mvc.Annotations.AnnotationParameter;
import java.util.HashMap;

@AnnotationController
public class Employee {
   String nom;
   int age;
   
   public Employee(){}

   public Employee(String nom , int age){
      setNom(nom);
      setAge(age);
   }

   public String getNom() {
      return nom;
   }

   public void setNom(String nom) {
      this.nom = nom;
   }

   public int getAge() {
      return age;
   }

   public void setAge(int age) {
      this.age = age;
   }

   @AnnotationMethod("/TEST_FRAMEWORK/listEmp")
   public String getListEmployer() {
      return "Calling getListEmployer";
   }

   @AnnotationMethod("/TEST_FRAMEWORK/getNomEmployer")
   public String getNomEmp(String nom) {
      return nom ;
   }
   
   @AnnotationMethod("/TEST_FRAMEWORK/listeEmployer")
   public ModelView listeEmployer() {
      String viewUrl = "listeEmployer.jsp";
      ModelView mv = new ModelView(viewUrl);
      String nom = "Jean" ;
      int age = 22 ;
      mv.add("emp",new Employee(nom,age));
      return mv;
   }
   @AnnotationMethod("/TEST_FRAMEWORK/genererEmployer")
   public ModelView genererEmployer(@AnnotationParameter("name") String nom,@AnnotationParameter("year")int age) {
      String viewUrl = "listeEmployer.jsp";
      ModelView mv = new ModelView(viewUrl);
      mv.add("emp",new Employee(nom,age));
      return mv;
   }

   @AnnotationMethod("/TEST_FRAMEWORK/createEmployer")
   public ModelView createEmployer(@AnnotationParameter("emp") Employee e,@AnnotationParameter("page") String nexPage) {
      String viewUrl = nexPage;
      ModelView mv = new ModelView(viewUrl);
      mv.add("emp",e);
      return mv;
   }
   @AnnotationMethod("/TEST_FRAMEWORK/index")
   public ModelView index() {
      String viewUrl = "formulaire.jsp";
      ModelView mv = new ModelView(viewUrl);
      return mv;
   }
}
