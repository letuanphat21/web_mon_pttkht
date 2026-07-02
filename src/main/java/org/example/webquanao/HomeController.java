package org.example.webquanao;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.entity.Product;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/"})
public class HomeController extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.equals("/")) {
            try {
                // Load sản phẩm active để hiển thị trên home
                List<Product> products = productDAO.findAllActive();
                request.setAttribute("products", products);
            } catch (Exception e) {
                e.printStackTrace();
                // Không throw — trang home vẫn hiện dù DB lỗi
            }
            request.getRequestDispatcher("/WEB-INF/home.jsp").forward(request, response);
        } else {
            request.getServletContext().getNamedDispatcher("default").forward(request, response);
        }
    }
}