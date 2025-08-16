package main.java.com.etu2728.controller;

import main.java.com.etu2728.modele.Scanner;
import main.java.com.etu2728.modele.Mapping;
import main.java.com.etu2728.modele.ModelView;
import main.java.com.etu2728.annotation.Get;
import main.java.com.etu2728.annotation.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    HashMap<String, Mapping> urlMappings = new HashMap<>();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // récupérer la liste des contrôleurs
        String packageName = this.getInitParameter("packageController");
        
        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le package 'packageController' est vide");
        }

        try {
            for (Class<?> controller: Scanner.getControllerClasses(packageName, Controller.class)) {
                for (Method method : controller.getMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                        String className = controller.getName();
                        String methodName = method.getName();

                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value();

                        if (urlMappings.containsKey(url)) {
                            throw new ServletException("URL en double détectée: " + url);
                        }

                        Mapping mapping = new Mapping(className, methodName);
                        
                        urlMappings.put(url, mapping);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();

        Mapping mapping = urlMappings.get(url);
        if (mapping == null) {
            throw new ServletException("Aucune methode associe a ce chemin");
        }

        String controllerName = mapping.getClassName();
        String methodeName = mapping.getMethodName();

        try {
            Class<?> controllerClass = Class.forName(controllerName);
            @SuppressWarnings("deprecation")
            Object controllerInstance = controllerClass.newInstance();
            Method method = null;
            for (Method m : controllerClass.getMethods()) {
                if (m.getName().equals(methodeName)) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new ServletException("Methode introuvable: " + methodeName);
            }
    
            Object result;
            Parameter[] parameters = method.getParameters();
            if (parameters.length > 0) {
                ArrayList<Object> values = Scanner.parameterMethod(method, req);
                if (values.size() != parameters.length) {
                    throw new ServletException("Nombre d'arguments incorrect pour la méthode " + method);
                }
                result = method.invoke(controllerInstance, values.toArray());
            } else {
                result = method.invoke(controllerInstance);
            }

            if (result instanceof String) {
                resp.setContentType("text/html");
                out.println("<h1>Sprint 3</h1>");
                out.println("<p><b>Lien: </b>" + url + "</p>");
                out.println("<p><b>Contrôleur: </b>" + controllerName + "</p>");
                out.println("<p><b>Méthode: </b>" + methodeName + "</p>");
                out.println("<p><b>Résultat: </b>" + result + "</p>");
            }
            else if (result instanceof ModelView) {
                ModelView modelView = (ModelView)result;
                String urlView = modelView.getUrl();
                HashMap<String, Object> data = modelView.getData();

                for (String key : data.keySet()) {
                    req.setAttribute(key, data.get(key));
                }

                req.getRequestDispatcher(urlView).forward(req, resp);
            }
            else {
                throw new ServletException("Type de retour invalide");
            }

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'execution de la methode", e);
        }
    }

}
