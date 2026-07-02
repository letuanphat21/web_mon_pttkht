<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt hàng thành công</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        body { background: #f5f6fa; }
        .success-box {
            max-width: 500px;
            margin: 80px auto;
            background: #fff;
            border-radius: 16px;
            padding: 48px 36px;
            text-align: center;
            box-shadow: 0 4px 24px rgba(0,0,0,.08);
        }
        .success-icon { font-size: 72px; color: #198754; margin-bottom: 20px; }
        .success-box h3 { font-weight: 700; color: #198754; margin-bottom: 12px; }
        .success-box p  { color: #666; margin-bottom: 6px; }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>

<div class="success-box">
    <div class="success-icon"><i class="bi bi-check-circle-fill"></i></div>
    <h3>Đặt hàng thành công!</h3>
    <p>${message}</p>
    <c:if test="${not empty orderId}">
        <p style="font-size:14px;color:#999;">Mã đơn hàng: <strong>${orderId}</strong></p>
    </c:if>
    <hr style="margin:24px 0;">
    <div class="d-grid gap-2">
        <a href="${pageContext.request.contextPath}/order-history" class="btn btn-primary">
            <i class="bi bi-receipt me-1"></i> Xem đơn hàng của tôi
        </a>
        <a href="${pageContext.request.contextPath}/shop" class="btn btn-outline-secondary">
            <i class="bi bi-bag-check me-1"></i> Tiếp tục mua sắm
        </a>
    </div>
</div>
</body>
</html>