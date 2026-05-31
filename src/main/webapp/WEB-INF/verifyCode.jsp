<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Xác nhận OTP</title>
</head>
<body>
<h2>Xác nhận mã OTP</h2>

<% if (request.getAttribute("message") != null) { %>
    <p style="color: green;"><%= request.getAttribute("message") %></p>
<% } %>

<% if (request.getAttribute("error") != null) { %>
    <p style="color: red;"><%= request.getAttribute("error") %></p>
<% } %>

<% if (request.getAttribute("email") != null) { %>
    <p>Email: <%= request.getAttribute("email") %></p>
<% } %>

<form action="<%= request.getContextPath() %>/verifyCode" method="post">
    <p>Mã OTP</p>
    <input type="text" name="otp" placeholder="Nhập mã 6 số" maxlength="6" pattern="[0-9]{6}" required />

    <br><br>
    <button type="submit">Xác nhận</button>
</form>

<form action="<%= request.getContextPath() %>/forgot-password" method="post">
    <input type="hidden" name="email" value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>" />
    <button type="submit">Gửi lại mã</button>
</form>

<br>
<a href="<%= request.getContextPath() %>/login">Hủy</a>
</body>
</html>
