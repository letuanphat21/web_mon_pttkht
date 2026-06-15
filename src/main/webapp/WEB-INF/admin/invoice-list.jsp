<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quản lý đơn hàng - Admin</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="container mt-4">

<div class="d-flex justify-content-between align-items-center mb-4">
  <h2>Quản lý đơn hàng</h2>
</div>

<c:if test="${not empty param.msg}">
  <div class="alert alert-info alert-dismissible fade show">
    Thao tác thành công: ${param.msg}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  </div>
</c:if>

<table class="table table-bordered table-hover">
  <thead class="table-dark">
  <tr>
    <th>Mã đơn</th>
    <th>Khách hàng</th>
    <th>Tổng tiền</th>
    <th>Trạng thái</th>
    <th>Ngày tạo</th>
    <th>Hành động</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="ord" items="${orders}">
    <tr>
      <td>${ord.orderId}</td>
      <td>${ord.customerName}</td>
      <td><strong><fmt:formatNumber value="${ord.totalPrice}" type="number"/> VNĐ</strong></td>
      <td>
                <span class="badge ${ord.status == 'Đã hủy' ? 'bg-danger' : (ord.status == 'Đã giao' ? 'bg-success' : 'bg-warning')}">
                    ${ord.status}
                </span>
      </td>
      <td><fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
      <td>
        <a href="${pageContext.request.contextPath}/admin/orders?action=detail&orderId=${ord.orderId}" class="btn btn-sm btn-info text-white">Chi tiết</a>

        <c:if test="${ord.status == 'Chờ xác nhận'}">
          <button type="button" class="btn btn-sm btn-warning" onclick="openUpdateModal('${ord.orderId}', '${ord.status}')">Cập nhật</button>
          <button type="button" class="btn btn-sm btn-danger" onclick="openCancelModal('${ord.orderId}')">Hủy đơn</button>
        </c:if>
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>

<div class="modal fade" id="updateModal" tabindex="-1">
  <div class="modal-dialog">
    <form action="${pageContext.request.contextPath}/admin/orders" method="POST" class="modal-content">
      <input type="hidden" name="action" value="update-status">
      <input type="hidden" name="orderId" id="updateOrderId">
      <div class="modal-header"><h5>Cập nhật trạng thái</h5></div>
      <div class="modal-body">
        <select name="newStatus" class="form-select">
          <option value="Đã xác nhận">Đã xác nhận</option>
          <option value="Đang giao">Đang giao</option>
          <option value="Đã giao">Đã giao</option>
        </select>
      </div>
      <div class="modal-footer"><button type="submit" class="btn btn-primary">Lưu</button></div>
    </form>
  </div>
</div>

<div class="modal fade" id="cancelModal" tabindex="-1">
  <div class="modal-dialog">
    <form action="${pageContext.request.contextPath}/admin/orders" method="POST" class="modal-content">
      <input type="hidden" name="action" value="cancel-order">
      <input type="hidden" name="orderId" id="cancelOrderId">
      <div class="modal-header"><h5>Lý do hủy đơn</h5></div>
      <div class="modal-body">
        <textarea name="reason" class="form-control" required placeholder="Nhập lý do hủy..."></textarea>
      </div>
      <div class="modal-footer"><button type="submit" class="btn btn-danger">Xác nhận hủy</button></div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
  function openUpdateModal(id, status) {
    document.getElementById('updateOrderId').value = id;
    new bootstrap.Modal(document.getElementById('updateModal')).show();
  }
  function openCancelModal(id) {
    document.getElementById('cancelOrderId').value = id;
    new bootstrap.Modal(document.getElementById('cancelModal')).show();
  }
</script>
</body>
</html>