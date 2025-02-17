README.md
Description

Ce projet implémente un framework MVC en Java utilisant des annotations personnalisées pour mapper les requêtes HTTP aux méthodes de contrôleurs.
Structure du projet

    classes.mvc
        Annotations : Annotations pour les contrôleurs, méthodes et paramètres.
        FrontController : Servlet principal gérant les requêtes HTTP.
        Mapping : Représente le mapping URL-méthode.
        ModelView : Représente le modèle et la vue.
        ScannerController : Scanne les packages pour trouver les classes de contrôleurs.

    classes.controllers
        Departement : Exemple de contrôleur pour les départements.
        Employee : Exemple de contrôleur pour les employés.

Annotations

    @AnnotationController : Marque une classe comme contrôleur.
    @AnnotationMethod : Marque une méthode comme gestionnaire d'une URL.
    @AnnotationParameter : Spécifie le nom d'un paramètre de méthode.

Configuration et Utilisation
Étape 1 : Configurer web.xml

xml

<web-app ...>
    <context-param>
        <param-name>base_package</param-name>
        <param-value>classes.controllers</param-value>
    </context-param>
    <servlet>
        <servlet-name>FrontController</servlet-name>
        <servlet-class>classes.mvc.FrontController</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>FrontController</servlet-name>
        <url-pattern>/TEST_FRAMEWORK/*</url-pattern>
    </servlet-mapping>
</web-app>

Étape 2 : Définir les Contrôleurs et Méthodes

Exemple de contrôleur :

java

package classes.controllers;

import classes.mvc.Annotations.AnnotationController;
import classes.mvc.Annotations.AnnotationMethod;
import classes.mvc.Annotations.AnnotationParameter;
import classes.mvc.ModelView;

@AnnotationController
public class Employee {
    @AnnotationMethod("/TEST_FRAMEWORK/listEmp")
    public String getListEmployer() {
        return "Calling getListEmployer";
    }

    @AnnotationMethod("/TEST_FRAMEWORK/genererEmployer")
    public ModelView genererEmployer(@AnnotationParameter("name") String nom, @AnnotationParameter("year") int age) {
        ModelView mv = new ModelView("listeEmployer.jsp");
        mv.add("emp", new Employee(nom, age));
        return mv;
    }
}

Étape 3 : Exécuter l'Application

Déployez l'application sur un conteneur de servlets comme Apache Tomcat. Accédez aux URLs définies dans les annotations @AnnotationMethod.
Classes Clés
FrontController

    init(ServletConfig config) : Initialise le servlet et scanne les classes de contrôleurs.
    processRequest(HttpServletRequest request, HttpServletResponse response) : Traite les requêtes.
    callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) : Invoque la méthode de contrôleur appropriée.
    getMethodParams(Method method, HttpServletRequest request) : Récupère les paramètres de méthode à partir de la requête.

ScannerController

    scanPackages(String packages) : Scanne les packages pour trouver les classes de contrôleurs.
    findClasses(File directory, String packageName, Map<String, Mapping> existingMappings) : Trouve et mappe les classes et méthodes annotées.

Remarques

    Supporte les paramètres complexes, y compris les objets.
    Les méthodes retournant ModelView redirigent vers une vue JSP avec des données.
    Toutes les classes et méthodes de contrôleur doivent être correctement annotées.README.md
Description

Ce projet implémente un framework MVC en Java utilisant des annotations personnalisées pour mapper les requêtes HTTP aux méthodes de contrôleurs.
Structure du projet

    classes.mvc
        Annotations : Annotations pour les contrôleurs, méthodes et paramètres.
        FrontController : Servlet principal gérant les requêtes HTTP.
        Mapping : Représente le mapping URL-méthode.
        ModelView : Représente le modèle et la vue.
        ScannerController : Scanne les packages pour trouver les classes de contrôleurs.

    classes.controllers
        Departement : Exemple de contrôleur pour les départements.
        Employee : Exemple de contrôleur pour les employés.

Annotations

    @AnnotationController : Marque une classe comme contrôleur.
    @AnnotationMethod : Marque une méthode comme gestionnaire d'une URL.
    @AnnotationParameter : Spécifie le nom d'un paramètre de méthode.

Configuration et Utilisation
Étape 1 : Configurer web.xml

xml

<web-app ...>
    <context-param>
        <param-name>base_package</param-name>
        <param-value>classes.controllers</param-value>
    </context-param>
    <servlet>
        <servlet-name>FrontController</servlet-name>
        <servlet-class>classes.mvc.FrontController</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>FrontController</servlet-name>
        <url-pattern>/TEST_FRAMEWORK/*</url-pattern>
    </servlet-mapping>
</web-app>

Étape 2 : Définir les Contrôleurs et Méthodes

Exemple de contrôleur :

java

package classes.controllers;

import classes.mvc.Annotations.AnnotationController;
import classes.mvc.Annotations.AnnotationMethod;
import classes.mvc.Annotations.AnnotationParameter;
import classes.mvc.ModelView;

@AnnotationController
public class Employee {
    @AnnotationMethod("/TEST_FRAMEWORK/listEmp")
    public String getListEmployer() {
        return "Calling getListEmployer";
    }

    @AnnotationMethod("/TEST_FRAMEWORK/genererEmployer")
    public ModelView genererEmployer(@AnnotationParameter("name") String nom, @AnnotationParameter("year") int age) {
        ModelView mv = new ModelView("listeEmployer.jsp");
        mv.add("emp", new Employee(nom, age));
        return mv;
    }
}

Étape 3 : Exécuter l'Application

Déployez l'application sur un conteneur de servlets comme Apache Tomcat. Accédez aux URLs définies dans les annotations @AnnotationMethod.
Classes Clés
FrontController

    init(ServletConfig config) : Initialise le servlet et scanne les classes de contrôleurs.
    processRequest(HttpServletRequest request, HttpServletResponse response) : Traite les requêtes.
    callMethod(Mapping mapping, PrintWriter out, HttpServletRequest request, HttpServletResponse response) : Invoque la méthode de contrôleur appropriée.
    getMethodParams(Method method, HttpServletRequest request) : Récupère les paramètres de méthode à partir de la requête.

ScannerController

    scanPackages(String packages) : Scanne les packages pour trouver les classes de contrôleurs.
    findClasses(File directory, String packageName, Map<String, Mapping> existingMappings) : Trouve et mappe les classes et méthodes annotées.

Remarques

    Supporte les paramètres complexes, y compris les objets.
    Les méthodes retournant ModelView redirigent vers une vue JSP avec des données.
    Toutes les classes et méthodes de contrôleur doivent être correctement annotées.