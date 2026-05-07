<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thông tin giao hàng | UC-1.28</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .step-header { background-color: #f8f9fa; padding: 20px 0; margin-bottom: 30px; border-bottom: 1px solid #dee2e6; }
        .is-invalid { border-color: #dc3545 !important; } /* E8: Đánh dấu đỏ các ô bị thiếu/sai */
        .error-message { color: #dc3545; font-size: 0.875em; margin-top: 0.25rem; }
    </style>
</head>
<body>
<jsp:include page="header.jsp" />
<div class="step-header">
    <div class="container">
        <h2 class="text-center">Đặt hàng - Bước 1: Thông tin giao hàng</h2>
    </div>
</div>

<div class="container" style="max-width: 600px;">
    <div class="card shadow-sm">
        <div class="card-body p-4">
            <c:if test="${not empty errorDetail}">
                <div class="alert alert-danger" role="alert">
                        ${errorDetail}
                </div>
            </c:if>

            <form action="order-process" method="post" class="needs-validation">

                <div class="mb-3">
                    <label for="fullName" class="form-label fw-bold">Họ và tên người nhận</label>
                    <input type="text" class="form-control" id="fullName" name="fullName"
                           value="${not empty inputName ? inputName : defaultName}"
                           placeholder="Nhập họ và tên" required>
                </div>

                <div class="mb-3">
                    <label for="phone" class="form-label fw-bold">Số điện thoại</label>
                    <input type="text"
                           class="form-control ${not empty errorDetail && errorDetail.contains('Số điện thoại') ? 'is-invalid' : ''}"
                           id="phone" name="phone"
                           value="${not empty inputPhone ? inputPhone : defaultPhone}"
                           placeholder="Ví dụ: 0901234567" required>
                    <c:if test="${not empty errorDetail && errorDetail.contains('Số điện thoại')}">
                        <div class="error-message">Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.</div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="address" class="form-label fw-bold">Địa chỉ giao hàng</label>
                    <textarea class="form-control ${not empty errorDetail && errorDetail.contains('Địa chỉ') ? 'is-invalid' : ''}"
                              id="address" name="address" rows="3"
                              placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố"
                              required>${not empty inputAddress ? inputAddress : defaultAddress}</textarea>
                </div>

                <div class="mt-4 d-grid gap-2">
                    <button type="submit" class="btn btn-primary btn-lg">
                        Kiểm tra thông tin
                    </button>
                    <a href="cart" class="btn btn-outline-secondary">Quay lại giỏ hàng</a>
                </div>

            </form>
        </div>
    </div>

    <div class="text-center mt-4 text-muted">
        <small>Thông tin cá nhân của bạn được bảo mật theo tiêu chuẩn hệ thống.</small>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>