package main.java.com.etu2728.controller;

import main.java.com.etu2728.modele.Mapping;
import main.java.com.etu2728.modele.Scanner;
import main.java.com.etu2728.annotation.Get;
import main.java.com.etu2728.annotation.Controller;

import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    HashMap<String, Mapping> urlMappings = new HashMap<>();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // récupérer la liste des contrôleurs
        String packageName = this.getInitParameter("packageController");
        try {
            for (Class<?> controller: Scanner.getControllerClasses(packageName, Controller.class)) {
                for (Method method : controller.getMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                        String className = controller.getName();
                        String methodName = method.getName();

                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value();

                        Mapping mapping = new Mapping(className, methodName);
                        
                        urlMappings.put(url, mapping);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();

        Mapping mapping = urlMappings.get(url);
        if (mapping == null) {
            resp.setContentType("text/html");
            out.println("<h1>Erreur: Il n'y a pas de méthode associé à ce chemin!</h1>");
            return;
        }

        String controllerName = mapping.getClassName();
        String methodeName = mapping.getMethodName();

        resp.setContentType("text/html");
        out.println("<h1>Sprint 2</h1>");
        out.println("<p><b>Lien: </b>" + url + "</p>");
        out.println("<p><b>Contrôleur: </b>" + controllerName + "</p>");
        out.println("<p><b>Méthode: </b>" + methodeName + "</p>");
    }

}
