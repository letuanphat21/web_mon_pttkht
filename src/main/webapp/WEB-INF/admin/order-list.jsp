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

<div id="main-container">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2>Quản lý đơn hàng</h2>
  </div>

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
    <tbody id="order-table-body">
    <c:forEach var="ord" items="${orders}">
      <tr id="row-${ord.orderId}">
        <td>${ord.orderId}</td>
        <td>${ord.customerName}</td>
        <td><strong><fmt:formatNumber value="${ord.totalPrice}" type="number"/> VNĐ</strong></td>
        <td class="status-cell">
                    <span class="badge ${ord.status == 'Đã hủy' ? 'bg-danger' : (ord.status == 'Đã xác nhận' ? 'bg-success' : 'bg-warning text-dark')}">
                        ${ord.status}
                    </span>
        </td>
        <td><fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
        <td class="action-cell">
          <button type="button" class="btn btn-sm btn-info text-white" onclick="loadDetail('${ord.orderId}')">Chi tiết</button>
          <c:if test="${ord.status == 'Chờ xác nhận'}">
            <button type="button" class="btn btn-sm btn-success btn-action" onclick="openModal('update', '${ord.orderId}')">Xác nhận</button>
            <button type="button" class="btn btn-sm btn-danger btn-action" onclick="openModal('cancel', '${ord.orderId}')">Hủy</button>
          </c:if>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>

<div class="modal fade" id="actionModal" tabindex="-1">
  <div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5 id="modalTitle"></h5></div>
    <div class="modal-body">
      <div id="updateBody" style="display:none;"><select id="newStatus" class="form-select"><option value="Đã xác nhận">Đã xác nhận</option></select></div>
      <div id="cancelBody" style="display:none;"><textarea id="reason" class="form-control" placeholder="Nhập lý do hủy..."></textarea></div>
    </div>
    <div class="modal-footer"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button><button type="button" class="btn btn-primary" onclick="submitAjax()">Xác nhận</button></div>
  </div></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
  let currentAction = '', currentOrderId = '';
  const modal = new bootstrap.Modal(document.getElementById('actionModal'));

  function loadDetail(id) {
    fetch('${pageContext.request.contextPath}/admin/orders?action=detail&orderId=' + id)
            .then(res => res.text())
            .then(html => { document.getElementById('main-container').innerHTML = html; });
  }

  function openModal(action, id) {
    currentAction = action; currentOrderId = id;
    document.getElementById('modalTitle').innerText = action === 'update' ? 'Xác nhận' : 'Hủy';
    document.getElementById('updateBody').style.display = action === 'update' ? 'block' : 'none';
    document.getElementById('cancelBody').style.display = action === 'cancel' ? 'block' : 'none';
    modal.show();
  }

  function submitAjax() {
    const data = new URLSearchParams();
    data.append('orderId', currentOrderId);
    let statusText = (currentAction === 'update') ? 'Đã xác nhận' : 'Đã hủy';
    data.append('action', currentAction === 'update' ? 'update-status' : 'cancel-order');
    if(currentAction === 'update') data.append('newStatus', document.getElementById('newStatus').value);
    else data.append('reason', document.getElementById('reason').value);

    fetch('${pageContext.request.contextPath}/admin/orders', {
      method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: data.toString()
    }).then(res => res.text()).then(result => {
      if (result.trim() === 'SUCCESS') {
        modal.hide();
        const row = document.getElementById('row-' + currentOrderId);
        row.querySelector('.status-cell').innerHTML = '<span class="badge ' + (statusText === 'Đã hủy' ? 'bg-danger' : 'bg-success') + '">' + statusText + '</span>';
        row.querySelectorAll('.btn-action').forEach(btn => btn.remove());
        alert('Thành công!');
      } else { alert('Lỗi: ' + result); }
    });
  }
  function loadList() {
    // Gọi lại URL gốc của trang danh sách đơn hàng
    fetch('${pageContext.request.contextPath}/admin/orders')
            .then(res => res.text())
            .then(html => {
              // Lấy nội dung từ kết quả trả về, dùng DOMParser để tránh load full trang
              const parser = new DOMParser();
              const doc = parser.parseFromString(html, 'text/html');
              const newContent = doc.getElementById('main-container').innerHTML;
              document.getElementById('main-container').innerHTML = newContent;
            })
            .catch(err => {
              console.error(err);
              location.reload(); // Dự phòng: reload nếu fetch lỗi
            });
  }
</script>
</body>
</html>