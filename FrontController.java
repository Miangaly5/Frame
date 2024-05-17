package controller;

import java.io.IOException;
import java.io.PrintWriter;

import modele.*;
import java.util.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    ArrayList<Class<?>> classes;

    public ArrayList<Class<?>> getClasses() {
        return classes;
    }
    public void setClasses(ArrayList<Class<?>> classes) {
        this.classes = classes;
    }

    boolean scanned = false;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String url = req.getRequestURI();
       PrintWriter out = resp.getWriter();
       String packageName = this.getInitParameter("Controller_Package");

       // Vérifier si les contrôleurs ont déjà été scannés
       if (!scanned) {
           // Scanner les contrôleurs et stocker la liste dans l'attribut
           try {
               this.setClasses(ListClasse.getControllerClasses(packageName));
               scanned = true;
           } catch (ClassNotFoundException | IOException e) {
               throw new ServletException("Erreur lors du scan des contrôleurs", e);
           }
       }

       out.println("Bienvenue dans le test de Sprint 0. URL : " + url);
       out.println("<br>Liste des contrôleurs :");
       out.println("<ul>");
       for (Class<?> controller : this.getClasses()) {
           out.println("<li>" + controller.getName() + "</li>");
       }
       out.println("</ul>");
    }
}
