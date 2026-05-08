<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<html>
<head>
    <title>Trang chủ - Web Quần Áo</title>
</head>
<body>

<jsp:include page="header.jsp" />

<div class="container" style="padding: 20px; max-width: 1200px; margin: 0 auto;">

    <section class="user-info">
        <h2>Xin chào, <c:out value="${sessionScope.username}" default="Khách"/></h2>
        <p>Email: <c:out value="${sessionScope.email}" default="Chưa cập nhật"/></p>
    </section>

    <hr>

    <h3>Danh sách sản phẩm mới nhất</h3>
    <div style="display: flex; flex-wrap: wrap; gap: 20px;">
        <c:forEach var="p" items="${products}">
            <div class="product-card" style="border: 1px solid #ddd; padding: 15px; width: 200px; border-radius: 8px;">
                <img src="${p.productImage}" alt="${p.productName}" style="width: 100%; height: 200px; object-fit: cover; border-radius: 4px;">
                <h4 style="margin: 10px 0;">${p.productName}</h4>
                <p style="color: #e44d26; font-weight: bold;">
                    Giá: <fmt:formatNumber value="${p.productPrice}" pattern="#,###"/> VNĐ
                </p>

                <div style="margin-top: 10px;">
                    <input type="number" id="qty-${p.productId}" value="1" min="1" style="width: 50px; padding: 5px;">
                    <button onclick="addToCart(${p.productId})" style="padding: 5px 10px; cursor: pointer; background: #28a745; color: white; border: none; border-radius: 3px;">
                        Thêm vào giỏ
                    </button>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<div id="cart-modal" style="display:none; position:fixed; z-index:999; background:white; border:1px solid #ccc; padding:20px; top:50%; left:50%; transform:translate(-50%, -50%); box-shadow: 0 4px 8px rgba(0,0,0,0.2); border-radius: 10px; min-width: 300px; text-align: center;">
    <h4 style="color: green; margin-top: 0;">Thêm sản phẩm thành công!</h4>
    <p>Bạn muốn làm gì tiếp theo?</p>
    <hr style="border: 0; border-top: 1px solid #eee;">
    <div style="display: flex; justify-content: space-around; margin-top: 15px;">
        <button onclick="continueShopping()" style="padding: 8px 15px; cursor: pointer;">Tiếp tục mua sắm</button>
        <button onclick="viewCart()" style="padding: 8px 15px; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 3px;">Xem giỏ hàng</button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>

</body>
</html>