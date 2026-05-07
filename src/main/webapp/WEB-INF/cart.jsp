<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<html>
<head>
    <title>Giỏ hàng của bạn</title>
    <style>
        .cart-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .cart-table th, .cart-table td { border: 1px solid #ddd; padding: 12px; text-align: center; }
        .total-section { text-align: right; margin-top: 20px; font-size: 1.2em; }
        .btn-delete { background: #dc3545; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 3px; }
        .btn-delete:hover { background: #c82333; }
    </style>
</head>
<body>
<jsp:include page="header.jsp" />

<div style="max-width: 1000px; margin: 30px auto; padding: 20px;">
    <h2>Giỏ hàng của bạn</h2>

    <c:choose>
        <c:when test="${empty sessionScope.cart or fn:length(sessionScope.cart) == 0}">
            <div style="text-align: center; padding: 50px;">
                <p>Giỏ hàng đang trống!</p>
                <a href="${pageContext.request.contextPath}/shop" style="color: blue;">Tiếp tục mua sắm</a>
            </div>
        </c:when>
        <c:otherwise>
            <table class="cart-table">
                <thead>
                <tr>
                    <th>Hình ảnh</th>
                    <th>Sản phẩm</th>
                    <th>Đơn giá</th>
                    <th>Số lượng</th>
                    <th>Thành tiền</th>
                    <th>Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="entry" items="${sessionScope.cart}">
                    <c:set var="item" value="${entry.value}"/>
                    <tr id="row-${item.productId}">
                        <td><img src="${item.productImage}" width="60" alt="${item.productName}"></td>
                        <td>${item.productName}</td>

                        <td><fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/> VNĐ</td>

                        <td>
                            <input type="number" value="${item.quantity}"
                                   min="0" style="width: 50px; padding: 5px;"
                                   onchange="updateQuantityAjax(${item.productId}, this.value)">
                        </td>

                        <td id="total-item-${item.productId}">
                            <fmt:formatNumber value="${item.totalAmount}" pattern="#,###"/> VNĐ
                        </td>

                        <td>
                            <form action="cart" method="POST" style="margin:0;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="productId" value="${item.productId}">
                                <button type="button" class="btn-delete" onclick="deleteItemAjax(${item.productId})">
                                    Xóa
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="total-section">
                <strong>Tổng tiền dự kiến: </strong>
                <span id="total-amount" style="color: red; font-weight: bold;">
                    <fmt:formatNumber value="${totalAmount}" pattern="#,###"/>
                </span> VNĐ
                <br><br>
                <button onclick="location.href='checkout'" style="padding: 10px 20px; background: #28a745; color: white; border: none; border-radius: 5px; cursor: pointer;">
                    Đặt hàng
                </button>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
</body>
</html>