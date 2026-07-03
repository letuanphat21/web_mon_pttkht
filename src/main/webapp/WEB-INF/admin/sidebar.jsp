<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<div class="col-md-2 p-0 sidebar">
    <div class="p-3"><h4>Quản lý</h4></div>
    <nav>
        <a href="<%= request.getContextPath() %>/admin/dashboard">Dashboard</a>
        <a href="<%= request.getContextPath() %>/admin/managerCategory">Quản lý Danh mục</a>
        <a href="<%= request.getContextPath() %>/admin/orders">Quản lý Đơn hàng</a>
        <a href="<%= request.getContextPath() %>/admin/managerUser" class="active">Quản lý Người dùng</a>
        <a href="<%= request.getContextPath() %>/admin/managerProduct">Quản lý Sản phẩm</a>
        <hr>
        <a href="<%= request.getContextPath() %>/logout" class="text-danger">Đăng xuất</a>
    </nav>
</div>