package controller;

import com.google.gson.Gson;

import annotation.Url;
import annotation.Get;
import annotation.Post;
import annotation.RestAPI;
import modele.Mapping;
import modele.ListClass;
import modele.ModelView;

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
        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le paramètre 'Package_Controller' est manquant ou vide");
        }
        try {
            this.setControllers(ListClass.getAllClasses(packageName));

            // itérer les contrôleurs et récupérer les méthodes annotées par @Get
            for (Class<?> controller : this.getControllers()) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Url.class)) {
                       //nom_classe et nom_methode
                        String className = controller.getName();
                        String methodName = method.getName();
                        String verb = "GET";
                        //@Get value
                        // Get getAnnotation = method.getAnnotation(Get.class);
                        Url getAnnotation = method.getAnnotation(Url.class);
                        String url = getAnnotation.value();
                        if(method.isAnnotationPresent(Get.class)) {
                            verb = "GET";
                        }
                        else if(method.isAnnotationPresent(Post.class)) {
                            verb = "POST";
                        }
                        if(urlMappings.containsKey(url)) {
                            throw new ServletException("URL en double détectée: " + url + " pour " + className + "#" + methodName);
                        }
                        Mapping mapping = new Mapping(className, methodName, verb);
                        urlMappings.put(url, mapping); //add dans HashMap
                    }
                }
            }
        }
        catch (ClassNotFoundException | IOException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
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
        String requestedVerb = req.getMethod();
        String verb = mapping.getVerb();
        if (!requestedVerb.equalsIgnoreCase(verb)) {
            resp.setContentType("text/html");
            out.println("<h2>Erreur: Le verbe HTTP " + requestedVerb + " ne correspond pas à l'annotation " + mapping.getVerb() + " pour " + mapping.getClassName() + "#" + mapping.getMethodName() + "</h2>");
            return;
        }

        try {
            // Instanciation du contrôleur
            Class<?> controllerClass = Class.forName(controllerName);
            @SuppressWarnings("deprecation")
            Object controllerInstance = controllerClass.newInstance();
            
            // Récupération de la méthode
            Method method = null;
            for(Method m : controllerClass.getMethods()) {
                if(m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            
            if(method == null) {
                throw new NoSuchMethodException(controllerClass.getName() + "." + methodName + "()");
            }
            
            // Exécution de la méthode et récupération du résultat
            Object result;
            if(method.getParameterCount() > 0) {
                ArrayList<Object> parameterValues = ListClass.getParameterValuesCombined(method, req);
                if (parameterValues.size() != method.getParameterCount()) {
                    throw new IllegalArgumentException("Nombre d'argument incorrect pour la methode" + method);
                }
                result = method.invoke(controllerInstance,parameterValues.toArray());
            }
            else{
                result = method.invoke(controllerInstance);
            }
            boolean isRestAPI = method.isAnnotationPresent(RestAPI.class);
            if(isRestAPI) {
                // La méthode est annotée avec @RestApi, on traite le résultat en JSON
                resp.setContentType("application/json");
                if (result instanceof ModelView) {
                    // Transformer le 'data' du ModelView en JSON
                    ModelView modelView = (ModelView) result;
                    HashMap<String, Object> data = modelView.getData();
                    String json = new Gson().toJson(data);
                    out.print(json);
                } else {
                    // Transformer directement le résultat en JSON
                    String json = new Gson().toJson(result);
                    out.print(json);
                }
            }
            else {
                if(result instanceof ModelView) {
                    ModelView modelView = (ModelView)result;
                    String urlView = modelView.getUrl();
                    HashMap<String, Object> data = modelView.getData();
                    for(String key : data.keySet()) {
                        req.setAttribute(key,data.get(key));
                    }
                    req.getRequestDispatcher(urlView).forward(req, resp);
                }
                else if(result instanceof String) {
                    // Affichage du résultat
                    resp.setContentType("text/html");
                    out.println("<h1>Sprint 4</h1><br>");
                    out.println("<p>Lien : " + url + "</p>");
                    out.println("<p>Contrôleur : " + controllerName + "</p>");
                    out.println("<p>Méthode : " + methodName + "</p>");
                    out.println("<p>Résultat : " + result.toString() + "</p>");
                }
                else {
                    throw new ServletException("Le type de retour de la méthode est invalide");
                }
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode", e);
        } catch (Exception e){
            out.println(e.getLocalizedMessage());
        }
    }

}
