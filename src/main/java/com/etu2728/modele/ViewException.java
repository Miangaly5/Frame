package main.java.com.etu2728.modele;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ViewException {

    public ViewException() {
        
    }
    
    public static void sendException(Exception error, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("error", error);

        request.getRequestDispatcher("view/error.jsp").forward(request, response);
    }

}
