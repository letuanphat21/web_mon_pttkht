package org.example.webquanao.api;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.request.ProductRequest;
import org.example.webquanao.service.ProductService;

import java.io.IOException;

@WebServlet(name = "product_api", value = "/api/product")
public class product_api extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final Gson gson = new Gson();

    // GET /api/product           -> lấy tất cả sản phẩm (admin)
    // GET /api/product?type=cat  -> lấy danh mục cho dropdown
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");
        Result result;
        if ("categories".equals(type)) {
            result = productService.getCategoriesForDropdown();
        } else {
            result = productService.getAllProductsForAdmin();
        }
        sendJson(response, result);
    }

    // POST /api/product -> thêm mới
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductRequest dto = gson.fromJson(request.getReader(), ProductRequest.class);
        Result result = productService.addProduct(dto);
        sendJson(response, result);
    }

    // PUT /api/product -> cập nhật
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductRequest dto = gson.fromJson(request.getReader(), ProductRequest.class);
        Result result = productService.updateProduct(dto);
        sendJson(response, result);
    }

    // DELETE /api/product?id=X -> toggle trạng thái (khóa/mở)
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            sendJson(response, Result.fail("Thiếu ID sản phẩm"));
            return;
        }
        int id = Integer.parseInt(idParam);
        Result result = productService.toggleProductStatus(id);
        sendJson(response, result);
    }

    private void sendJson(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(result));
    }
}
