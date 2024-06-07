package controller;

import annotation.Get;
import modele.Mapping;
import modele.ListClass;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
 HashMap<String, Mapping> urlMappings = new HashMap<>();
 ArrayList<Class<?>> controllers;

    // getter et setter
    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }

    @Override
    public void init() throws ServletException{
        super.init();
        // récupérer la liste des contrôleurs
        String packageName = this.getInitParameter("Package_Controller");
        try {
            this.setControllers(ListClass.getAllClasses(packageName));

            // itérer les contrôleurs et récupérer les méthodes annotées par @Get
            for (Class<?> controller : this.getControllers()) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                       //nom_classe et nom_methode
                        String className = controller.getName();
                        String methodName = method.getName();

                        //@Get value
                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value();

                        Mapping mapping = new Mapping(className, methodName);
                        urlMappings.put(url, mapping); //add dans HashMap
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getServletPath();
    
        PrintWriter out = resp.getWriter();
        // Check URL
        Mapping mapping = urlMappings.get(url);
        if (mapping == null) {
            resp.setContentType("text/html");
            out.println("<h1>Erreur: L'URL demandée n'est pas disponible!</h1>");
            return;
        }
    
        // Récupération du nom de contrôleur et de la méthode
        String controllerName = mapping.getClassName();
        String methodName = mapping.getMethodName();
        try {
            // Instanciation du contrôleur
            Class<?> controllerClass = Class.forName(controllerName);
            @SuppressWarnings("deprecation")
            Object controllerInstance = controllerClass.newInstance();
            
            // Récupération de la méthode
            Method method = controllerClass.getMethod(methodName);
            
            // Exécution de la méthode et récupération du résultat
            Object result = method.invoke(controllerInstance);
            
            // Affichage du résultat
            resp.setContentType("text/html");
            out.println("<h1>Sprint 3 </h1><br>");
            out.println("<p>Lien : " + url + "</p>");
            out.println("<p>Contrôleur : " + controllerName + "</p>");
            out.println("<p>Méthode : " + methodName + "</p>");
            out.println("<p>Résultat : " + result + "</p>");
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode", e);
        }
    }

}
