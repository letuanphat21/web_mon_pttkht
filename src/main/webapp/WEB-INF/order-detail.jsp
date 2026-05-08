<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng ${order.orderId} | WebQuầnÁo</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .order-card { border-radius: 15px; border: none; box-shadow: 0 0 20px rgba(0,0,0,0.05); }
        .product-img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; }
        .status-tracker { background: #f8f9fa; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
        .summary-label { color: #6c757d; }
        .summary-value { font-weight: bold; text-align: right; }
    </style>
</head>
<body class="bg-light">

<jsp:include page="header.jsp" />

<div class="container my-5">
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/order-history" class="btn btn-outline-secondary">
            &larr; Quay lại danh sách đơn hàng
        </a>
    </div>

    <div class="row">
        <div class="col-lg-4">
            <div class="card order-card mb-4">
                <div class="card-body">
                    <h5 class="card-title mb-4">Thông tin khách hàng</h5>
                    <p class="mb-1 summary-label">Người nhận:</p>
                    <p class="fw-bold">${order.fullName}</p>

                    <p class="mb-1 summary-label">Số điện thoại:</p>
                    <p class="fw-bold">${order.phone}</p>

                    <p class="mb-1 summary-label">Địa chỉ giao hàng:</p>
                    <p class="fw-bold">${order.address}</p>

                    <hr>

                    <p class="mb-1 summary-label">Ngày đặt hàng:</p>
                    <p class="fw-bold"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></p>
                </div>
            </div>
        </div>

        <div class="col-lg-8">
            <div class="card order-card">
                <div class="card-header bg-white py-3">
                    <div class="d-flex justify-content-between align-items: center;">
                        <h5 class="mb-0">Mã đơn: <span class="text-primary">${order.orderId}</span></h5>
                        <span class="badge ${order.status == 'Đã hủy' ? 'bg-danger' : 'bg-success'}">
                            ${order.status}
                        </span>
                    </div>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Giá</th>
                                <th class="text-center">Số lượng</th>
                                <th class="text-end">Tạm tính</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="item" items="${details}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <img src="https://via.placeholder.com/80" alt="Sản phẩm" class="product-img me-3">
                                            <div>
                                                <p class="mb-0 fw-bold">Sản phẩm #${item.productId}</p>
                                                <small class="text-muted text-uppercase">Mã SP: ${item.productId}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td><fmt:formatNumber value="${item.price}" pattern="#,###"/> đ</td>
                                    <td class="text-center">x${item.quantity}</td>
                                    <td class="text-end fw-bold">
                                        <fmt:formatNumber value="${item.price * item.quantity}" pattern="#,###"/> đ
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div class="row justify-content-end mt-4">
                        <div class="col-md-5">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="summary-label">Tạm tính:</span>
                                <span><fmt:formatNumber value="${order.totalPrice}" pattern="#,###"/> đ</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="summary-label">Phí vận chuyển (6a1):</span>
                                <span>Miễn phí</span>
                            </div>
                            <hr>
                            <div class="d-flex justify-content-between">
                                <span class="h5">Tổng cộng:</span>
                                <span class="h5 text-danger"><fmt:formatNumber value="${order.totalPrice}" pattern="#,###"/> đ</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="card-footer bg-white py-3">
                    <p class="text-muted small mb-0">
                        * Mọi thắc mắc về đơn hàng, vui lòng liên hệ hotline 1900 xxxx để được hỗ trợ.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>