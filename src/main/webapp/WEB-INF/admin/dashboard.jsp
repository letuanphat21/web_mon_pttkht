<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .sidebar { min-height: 100vh; background: #212529; color: white; }
        .sidebar a { color: #adb5bd; text-decoration: none; padding: 10px 20px; display: block; }
        .sidebar a:hover { background: #343a40; color: white; }
        .sidebar a.active { background: #0d6efd; color: white; }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 p-0 sidebar">
            <div class="p-3"><h4>Quản lý</h4></div>
            <nav>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="active">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/managerCategory">Quản lý Danh mục</a>
                <a href="${pageContext.request.contextPath}/admin/invoice?action=list">Quản lý Hóa đơn</a>
                <a href="${pageContext.request.contextPath}/admin/managerUser">Quản lý Người dùng</a>
                <hr>
                <a href="${pageContext.request.contextPath}/logout" class="text-danger">Đăng xuất</a>
            </nav>
        </div>

        <div class="col-md-10 p-4">
            <h2 class="mb-4">Chào mừng Admin quay trở lại!</h2>

            <div class="row">
                <div class="col-md-4">
                    <div class="card text-white bg-primary mb-3">
                        <div class="card-body">
                            <h5 class="card-title">Tổng hóa đơn</h5>
                            <p class="card-text fs-2"></p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card text-white bg-success mb-3">
                        <div class="card-body">
                            <h5 class="card-title">Sản phẩm đang bán</h5>
                            <p class="card-text fs-2"></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>