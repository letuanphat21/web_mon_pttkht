<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quản lý hóa đơn</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="container mt-4">

<div class="d-flex justify-content-between align-items-center mb-4">
  <h2>Danh sách hóa đơn</h2>
  <button type="button" class="btn btn-primary" onclick="openAddModal()">Thêm hóa đơn mới</button>
</div>

<c:if test="${not empty sessionScope.message}">
  <div class="alert alert-success alert-dismissible fade show">
      ${sessionScope.message}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  </div>
  <% session.removeAttribute("message"); %>
</c:if>

<c:if test="${not empty sessionScope.error}">
  <div class="alert alert-danger alert-dismissible fade show">
      ${sessionScope.error}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  </div>
  <% session.removeAttribute("error"); %>
</c:if>

<table class="table table-bordered table-hover">
  <thead class="table-dark">
  <tr>
    <th>Mã HĐ</th>
    <th>Mã Đơn</th>
    <th>Khách hàng</th>
    <th>Tổng tiền</th>
    <th>Trạng thái</th>
    <th>Hành động</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="inv" items="${invoices}">
    <tr>
      <td><c:out value="${inv.invoiceId}"/></td>
      <td><c:out value="${inv.orderId}"/></td>
      <td><c:out value="${inv.customerName}"/></td>
      <td>
        <strong><fmt:formatNumber value="${inv.totalAmount}" type="number"/> VNĐ</strong>
      </td>
      <td>
                <span class="badge ${inv.paymentStatus == 'Đã thanh toán' ? 'bg-success' : (inv.paymentStatus == 'Đã hủy' ? 'bg-danger' : 'bg-warning')}">
                    <c:out value="${inv.paymentStatus}"/>
                </span>
      </td>
      <td>
        <button type="button" class="btn btn-sm btn-info text-white"
                onclick="openEditModal('<c:out value="${inv.invoiceId}"/>',
                        '<c:out value="${inv.orderId}"/>',
                        '<c:out value="${inv.customerName}"/>',
                        '${inv.totalAmount}',
                        '${inv.paymentMethod}',
                        '${inv.paymentStatus}')">
          Sửa
        </button>
        <button type="button" onclick="handleDelete('<c:out value="${inv.invoiceId}"/>')" class="btn btn-sm btn-outline-danger">Xóa</button>
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>

<div class="modal fade" id="invoiceModal" tabindex="-1" aria-labelledby="modalTitle" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <form action="${pageContext.request.contextPath}/admin/invoice" method="POST">
        <div class="modal-header">
          <h5 class="modal-title" id="modalTitle">Hóa đơn</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <input type="hidden" name="action" id="modalAction" value="add">

          <div class="mb-3">
            <label class="form-label font-weight-bold">Mã hóa đơn</label>
            <input type="text" name="invoiceId" id="modalInvoiceId" class="form-control bg-light" readonly placeholder="Hệ thống tự động tạo">
          </div>

          <div class="mb-3" id="orderSelectGroup">
            <label class="form-label">Chọn đơn hàng chưa có hóa đơn</label>
            <select name="orderId" id="modalOrderId" class="form-select">
              <option value="">-- Chọn đơn hàng --</option>
              <c:forEach var="ord" items="${eligibleOrders}">
                <option value="${ord.orderId}">${ord.orderId} - ${ord.fullName}</option>
              </c:forEach>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Tên khách hàng</label>
            <input type="text" name="customerName" id="modalCustomerName" class="form-control" required>
          </div>

          <div class="mb-3" id="amountGroup">
            <label class="form-label">Tổng tiền (VNĐ)</label>
            <input type="number" name="totalAmount" id="modalTotalAmount" class="form-control bg-light" step="0.01" readonly>
            <small class="text-muted" id="amountNote">Tiền được lấy tự động từ đơn hàng và không được phép sửa đổi.</small>
          </div>

          <div class="mb-3">
            <label class="form-label">Phương thức thanh toán</label>
            <select name="paymentMethod" id="modalPaymentMethod" class="form-select">
              <option value="Tiền mặt">Tiền mặt</option>
              <option value="Chuyển khoản">Chuyển khoản</option>
              <option value="Ví điện tử">Ví điện tử</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Trạng thái thanh toán</label>
            <select name="paymentStatus" id="modalPaymentStatus" class="form-select">
              <option value="Chưa thanh toán">Chưa thanh toán</option>
              <option value="Đã thanh toán">Đã thanh toán</option>
              <option value="Đã hủy">Đã hủy</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
          <button type="submit" class="btn btn-primary">Xác nhận lưu</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
  // 1. Khởi tạo Modal (đảm bảo Bootstrap đã load xong)
  let bModal;
  document.addEventListener("DOMContentLoaded", function() {
    bModal = new bootstrap.Modal(document.getElementById('invoiceModal'));
  });

  // 2. Hàm mở modal để Thêm mới
  function openAddModal() {
    document.getElementById('modalTitle').innerText = "Tạo hóa đơn từ đơn hàng";
    document.getElementById('modalAction').value = "add";
    document.getElementById('modalInvoiceId').value = "";
    document.getElementById('orderSelectGroup').style.display = "block";
    document.getElementById('amountNote').style.display = "block";

    // Reset form
    document.getElementById('modalCustomerName').value = "";
    document.getElementById('modalTotalAmount').value = "";
    document.getElementById('modalPaymentMethod').value = "Tiền mặt";
    document.getElementById('modalPaymentStatus').value = "Chưa thanh toán";

    bModal.show();
  }

  // 3. Hàm mở modal để Sửa
  function openEditModal(id, orderId, name, amount, method, status) {
    document.getElementById('modalTitle').innerText = "Chỉnh sửa hóa đơn: " + id;
    document.getElementById('modalAction').value = "update";
    document.getElementById('modalInvoiceId').value = id;
    document.getElementById('orderSelectGroup').style.display = "none"; // Ẩn chọn đơn hàng vì không được đổi đơn
    document.getElementById('amountNote').style.display = "none";

    // Đổ dữ liệu vào Form
    document.getElementById('modalCustomerName').value = name;
    document.getElementById('modalTotalAmount').value = amount;
    document.getElementById('modalPaymentMethod').value = method;
    document.getElementById('modalPaymentStatus').value = status;

    bModal.show();
  }

  // 4. Hàm xóa (Soft Delete)
  function handleDelete(id) {
    if (!id || id.trim() === "") {
      alert("Lỗi: Không tìm thấy mã hóa đơn!");
      return;
    }

    if (confirm("Xác nhận hủy hóa đơn " + id + "?")) {
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '${pageContext.request.contextPath}/admin/invoice';

      const actionInput = document.createElement('input');
      actionInput.type = 'hidden';
      actionInput.name = 'action';
      actionInput.value = 'cancel';

      const idInput = document.createElement('input');
      idInput.type = 'hidden';
      idInput.name = 'invoiceId';
      idInput.value = id;

      form.appendChild(actionInput);
      form.appendChild(idInput);
      document.body.appendChild(form);
      form.submit();
    }
  }
</script>
</body>
</html>