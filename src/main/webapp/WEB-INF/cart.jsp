<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<html>
<head>
    <title>Giỏ hàng & Thanh toán</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .cart-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .cart-table th, .cart-table td { border: 1px solid #ddd; padding: 12px; text-align: center; }
        .total-section { text-align: right; margin-top: 20px; font-size: 1.2em; }
        .btn-delete { background: #dc3545; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 3px; }
        .btn-delete:hover { background: #c82333; }

        #order-info-container {
            display: none;
            animation: fadeIn 0.5s;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp" />

<div class="container my-5">

    <div id="cart-container">
        <h2 class="mb-4">Giỏ hàng của bạn</h2>

        <c:choose>
            <c:when test="${empty sessionScope.cart or fn:length(sessionScope.cart) == 0}">
                <div class="text-center py-5">
                    <p class="fs-5 text-muted">Giỏ hàng đang trống!</p>
                    <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary">Tiếp tục mua sắm</a>
                </div>
            </c:when>
            <c:otherwise>
                <table class="cart-table">
                    <thead class="table-light">
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
                                       min="1" style="width: 60px; padding: 5px;"
                                       onchange="updateQuantityAjax(${item.productId}, this.value)">
                            </td>
                            <td id="total-item-${item.productId}" class="fw-bold text-primary">
                                <fmt:formatNumber value="${item.totalAmount}" pattern="#,###"/> VNĐ
                            </td>
                            <td>
                                <button type="button" class="btn-delete" onclick="deleteItemAjax(${item.productId})">
                                    Xóa
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <div class="total-section card p-3 shadow-sm">
                    <div>
                        <strong>Tổng tiền dự kiến: </strong>
                        <span id="total-amount" class="text-danger fw-bold fs-3">
                            <fmt:formatNumber value="${totalAmount}" pattern="#,###"/>
                        </span> VNĐ
                    </div>
                    <div class="mt-3">
                        <button type="button"
                                onclick="proceedToCheckout(event)"
                                class="btn btn-success btn-lg px-5 fw-bold">
                            Đặt hàng ngay
                        </button>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div id="order-info-container">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow">
                    <div class="card-header bg-success text-white py-3">
                        <h4 class="mb-0 text-center">Bước tiếp theo: Thông tin giao hàng</h4>
                    </div>
                    <div class="card-body p-4">
                        <div id="ajax-error-info" class="alert alert-danger d-none"></div>

                        <form action="order-process" method="post">
                            <div class="mb-3">
                                <label for="fullName" class="form-label fw-bold">Họ và tên người nhận</label>
                                <input type="text" class="form-control" name="fullName"
                                       value="${not empty sessionScope.pendingOrder.fullName ? sessionScope.pendingOrder.fullName : sessionScope.username}" required>
                            </div>

                            <div class="mb-3">
                                <label for="phone" class="form-label fw-bold">Số điện thoại</label>
                                <input type="text" class="form-control" name="phone"
                                       value="${not empty sessionScope.pendingOrder.phone ? sessionScope.pendingOrder.phone : ''}" required>
                            </div>

                            <div class="mb-3">
                                <label for="address" class="form-label fw-bold">Địa chỉ giao hàng</label>
                                <textarea class="form-control" name="address" rows="3" required>${not empty sessionScope.pendingOrder.address ? sessionScope.pendingOrder.address : ''}</textarea>
                            </div>

                            <div class="mt-4 d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg fw-bold">
                                    Xác nhận và Review đơn hàng
                                </button>
                                <button type="button" onclick="backToCart()" class="btn btn-outline-secondary">
                                    Quay lại chỉnh sửa giỏ hàng
                                </button>
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center text-muted py-3">
                        <small>Vui lòng kiểm tra kỹ số điện thoại để shipper liên lạc dễ dàng.</small>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    function backToCart() {
        document.getElementById('cart-container').style.display = 'block';
        document.getElementById('order-info-container').style.display = 'none';
        window.scrollTo(0, 0);
    }
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        const isEdit = urlParams.get('edit');
        if (isEdit === 'true' || ${openOrderForm == true}) {
            const cartContainer = document.getElementById('cart-container');
            const infoContainer = document.getElementById('order-info-container');

            if (cartContainer && infoContainer) {
                cartContainer.style.display = 'none';
                infoContainer.style.display = 'block';
                console.log("Đang ở chế độ chỉnh sửa thông tin giao hàng.");
            }
        }
    });
</script>

</body>
</html>