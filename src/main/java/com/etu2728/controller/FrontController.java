package main.java.com.etu2728.controller;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();

        resp.setContentType("text/html");
        out.println("<h1>Sprint 0</h1>");
        out.println("<p>Lien: " + url + "</p>");
    }
}
