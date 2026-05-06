<%--
  Created by IntelliJ IDEA.
  User: LAPTOP USA PRO
  Date: 5/3/2026
  Time: 10:21 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h2>Đăng ký tài khoản</h2>

<!-- Hiển thị thông báo -->
<c:if test="${not empty message}">
    <p style="color: green">${message}</p>
</c:if>

<c:if test="${not empty error}">
    <p style="color: red">${error}</p>
</c:if>

<!-- Form -->
<form action="register" method="post">

    <div>Tên đăng nhập</div>
    <input type="text" name="username" placeholder="Nhập tên đăng nhập" required />

    <div>Mật khẩu</div>
    <input type="password" name="password" placeholder="Nhập mật khẩu" required />

    <div>Nhập lại mật khẩu</div>
    <input type="password" name="password_again" placeholder="Nhập lại mật khẩu" required />

    <div>Email</div>
    <input type="email" name="email" placeholder="Nhập email" required />

    <div>Họ và tên</div>
    <input type="text" name="fullname" placeholder="Nhập họ tên" required />

    <br><br>
    <button type="submit">Đăng ký</button>

</form>
</body>
</html>
