<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác nhận đơn hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="header.jsp" />

<div class="container my-5">
    <div class="row">
        <div class="col-md-5">
            <div class="card shadow-sm mb-4">
                <div class="card-header bg-white fw-bold">Thông tin nhận hàng</div>
                <div class="card-body">
                    <p><strong>Người nhận:</strong> ${sessionScope.checkoutShipping.fullName}</p>
                    <p><strong>Số điện thoại:</strong> ${sessionScope.checkoutShipping.phone}</p>
                    <p><strong>Địa chỉ:</strong> ${sessionScope.checkoutShipping.address}</p>
                    <a href="${pageContext.request.contextPath}/order-process?action=editShipping" class="btn btn-sm btn-outline-primary">
                        Chỉnh sửa thông tin
                    </a>
                </div>
            </div>
        </div>

        <div class="col-md-7">
            <div class="card shadow-sm">
                <div class="card-header bg-white fw-bold">Chi tiết đơn hàng</div>
                <div class="card-body p-0">
                    <table class="table mb-0">
                        <thead class="table-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th class="text-center">SL</th>
                            <th class="text-end">Thành tiền</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${sessionScope.checkoutCart.cartItems}">
                            <tr>
                                <td>
                                    <div class="d-flex align-items-center">
                                            <%-- Dùng item.name vì class CartItemResponse có getName() --%>
                                        <span>${item.name}</span>
                                    </div>
                                </td>
                                    <%-- Dùng item.qty vì class CartItemResponse có getQty() --%>
                                <td class="text-center">${item.qty}</td>
                                <td class="text-end">
                                    <fmt:formatNumber value="${item.subTotal}" pattern="#,###"/> VNĐ
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <tfoot class="table-light">
                        <tr>
                            <td colspan="2" class="fw-bold text-end">Tổng cộng:</td>
                            <td class="text-end fw-bold text-danger fs-5">
                                <fmt:formatNumber value="${sessionScope.checkoutCart.totalAmount}" pattern="#,###"/> VNĐ
                            </td>
                        </tr>
                        </tfoot>
                    </table>
                </div>

                <div class="card-footer d-grid">
                    <form action="order-process" method="post">
                        <input type="hidden" name="action" value="prepareOrder">
                        <button type="submit" class="btn btn-success btn-lg w-100">Xác nhận đặt hàng</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>