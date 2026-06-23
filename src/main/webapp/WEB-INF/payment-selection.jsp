<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chọn phương thức thanh toán</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
  <style>
    body { background: #f5f6fa; }
    .payment-card {
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      padding: 20px 24px;
      cursor: pointer;
      transition: border-color .2s, box-shadow .2s;
      margin-bottom: 14px;
      background: #fff;
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .payment-card:hover { border-color: #0d6efd; box-shadow: 0 2px 10px rgba(13,110,253,.15); }
    .payment-card.selected { border-color: #0d6efd; background: #f0f4ff; }
    .payment-card .icon { font-size: 36px; color: #495057; }
    .payment-card .info h6 { margin: 0 0 4px; font-weight: 700; }
    .payment-card .info p  { margin: 0; font-size: 13px; color: #777; }

    .order-summary { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
    .order-summary table { width: 100%; font-size: 14px; }
    .order-summary td { padding: 6px 0; }
    .order-summary .total-row td { font-size: 18px; font-weight: 700; color: #e44d26; border-top: 1px solid #eee; padding-top: 12px; }
  </style>
</head>
<body>
<jsp:include page="header.jsp"/>

<div class="container my-5" style="max-width:900px;">
  <h3 class="mb-4 fw-bold"><i class="bi bi-credit-card-2-front me-2"></i>Chọn phương thức thanh toán</h3>

  <c:if test="${not empty error}">
    <div class="alert alert-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>${error}</div>
  </c:if>

  <div class="row g-4">

    <div class="col-md-6">
      <form action="${pageContext.request.contextPath}/payment" method="post" id="paymentForm">

        <div class="payment-card selected" id="card-COD" onclick="selectMethod('COD')">
          <span class="icon"><i class="bi bi-truck"></i></span>
          <div class="info">
            <h6>Thanh toán khi nhận hàng (COD)</h6>
            <p>Trả tiền mặt khi nhận được hàng</p>
          </div>
        </div>

        <div class="payment-card" id="card-MOMO" onclick="selectMethod('MOMO')">
          <span class="icon"><i class="bi bi-phone"></i></span>
          <div class="info">
            <h6>Thanh toán MoMo</h6>
            <p>Ví điện tử MoMo (Sandbox)</p>
          </div>
        </div>

        <input type="hidden" name="paymentMethod" id="paymentMethodInput" value="COD">

        <button type="submit" class="btn btn-success btn-lg w-100 mt-3" id="btnPay">
          <i class="bi bi-check-circle me-1"></i> Xác nhận thanh toán
        </button>
        <form id="momoForm" action="${pageContext.request.contextPath}/payment" method="post" class="d-none">
          <input type="hidden" name="paymentMethod" value="MOMO">
        </form>
        <div class="text-center mt-4">
          <a href="${pageContext.request.contextPath}/cart" class="btn btn-secondary">
            <i class="bi bi-arrow-left me-1"></i> Quay lại giỏ hàng
          </a>
        </div>
      </form>
    </div>

    <div class="col-md-6">
      <div class="order-summary">
        <h6 class="fw-bold mb-3"><i class="bi bi-clipboard me-2"></i>Đơn hàng của bạn</h6>

        <c:if test="${not empty sessionScope.checkoutShipping}">
          <p style="font-size:13px;color:#555;margin-bottom:12px;">
            <strong><i class="bi bi-geo-alt-fill me-1"></i>Giao tới:</strong> ${sessionScope.checkoutShipping.fullName}
            — ${sessionScope.checkoutShipping.phone}<br>
              ${sessionScope.checkoutShipping.address}
          </p>
        </c:if>

        <table>
          <c:forEach var="item" items="${sessionScope.checkoutCart.cartItems}">
            <tr>
              <td>${item.name}</td>
              <td style="text-align:right;white-space:nowrap;">
                x${item.qty} &nbsp;
                <fmt:formatNumber value="${item.subTotal}" pattern="#,###"/> ₫
              </td>
            </tr>
          </c:forEach>
          <tr class="total-row">
            <td>Tổng cộng</td>
            <td style="text-align:right;">
              <fmt:formatNumber value="${sessionScope.checkoutCart.totalAmount}" pattern="#,###"/> ₫
            </td>
          </tr>
        </table>
      </div>
    </div>

  </div>
</div>

<script>
  function selectMethod(method) {
    // Reset tất cả
    document.querySelectorAll('.payment-card').forEach(c => c.classList.remove('selected'));
    // Highlight card được chọn
    document.getElementById('card-' + method).classList.add('selected');
    // Set hidden input
    document.getElementById('paymentMethodInput').value = method;
    // Đổi text nút
    const btn = document.getElementById('btnPay');
    if (method === 'MOMO') {
      btn.innerHTML = '<i class="bi bi-phone me-1"></i> Thanh toán qua MoMo';
      btn.className = 'btn btn-danger btn-lg w-100 mt-3';
    } else {
      btn.innerHTML = '<i class="bi bi-check-circle me-1"></i> Xác nhận thanh toán';
      btn.className = 'btn btn-success btn-lg w-100 mt-3';
    }
  }
</script>
</body>
</html>