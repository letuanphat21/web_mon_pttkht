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
<h2>Đăng nhập</h2>

<!-- Hiển thị lỗi nếu có -->
<p style="color:red;">
    ${error}
</p>
<p style="color:green;">
    ${message}
</p>

<form action="login" method="post">

    <p>Tài khoản</p>
    <input type="email" name="email" placeholder="Nhập email" required />

    <p>Mật khẩu</p>
    <input type="password" name="password" placeholder="Nhập mật khẩu" required />

    <br><br>

    <button type="submit">Đăng nhập</button>

</form>

<br>
<a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>

<br><br>

<!-- Google login -->
<a href="https://accounts.google.com/o/oauth2/v2/auth
?client_id=783679203986-3fkjbmtc49vj8a8u06smumq6g068j1um.apps.googleusercontent.com
&redirect_uri=http://localhost:8080/loginGoogle
&response_type=code
&scope=email profile">
    Đăng nhập bằng Google
</a>
</body>
</html>
