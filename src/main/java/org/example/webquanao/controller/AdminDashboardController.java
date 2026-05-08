package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dao.InvoiceDAO;

import java.io.IOException;

@WebServlet(name = "AdminDashboardController", value = "/admin/dashboard")
public class AdminDashboardController extends HttpServlet {
    private InvoiceDAO invoiceDAO = new org.example.webquanao.dao.InvoiceDAO();
    private CategoryDAO categoryDAO = new org.example.webquanao.dao.CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int totalInvoices = invoiceDAO.getAllInvoices().size();
        int totalCategories = categoryDAO.findAll().size();

        request.setAttribute("totalInvoices", totalInvoices);
        request.setAttribute("totalCategories", totalCategories);

        request.getRequestDispatcher("/WEB-INF/admin/dashboard.jsp").forward(request, response);
    }
}
