package main.java.com.etu2728.controller;

import main.java.com.etu2728.modele.Scanner;
import main.java.com.etu2728.annotation.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    ArrayList<Class<?>> controllers;
    Boolean isScanned = false;

    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }
    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }
    
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
            controllers = new ArrayList<Class<?>>();
            for (Class<?> classe : Scanner.getAllClasses(packageName)) {
                if (classe.isAnnotationPresent(Controller.class)) {
                    this.getControllers().add(classe);
                    isScanned = true;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();

        resp.setContentType("text/html");
        out.println("<h1>Sprint 1</h1>");
        out.println("<p><b>Lien: </b>" + url + "</p>");

        if (isScanned) {
            int count = 0;
            for (Class<?> controller : controllers) {
                count++;
    
                out.println("<p><b>Controller " + count + ": </b>" + controller.getName());
            }
        }
    }

}
