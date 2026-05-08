package org.example.webquanao;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.entity.Product;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/"})
public class HomeController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.equals("/")) {
            request.setAttribute("msg", "Chào mừng bạn!");
            request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
        } else {
            request.getServletContext().getNamedDispatcher("default").forward(request, response);
        }
    }
}