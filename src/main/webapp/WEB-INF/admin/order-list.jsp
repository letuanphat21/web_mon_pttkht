<%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
      <html>

      <head>
        <meta charset="UTF-8">
        <title>Quản lý đơn hàng - Admin</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
          .sidebar {
            min-height: 100vh;
            background: #212529;
            color: #fff;
          }

          .sidebar a {
            color: #adb5bd;
            text-decoration: none;
            padding: 10px 20px;
            display: block;
          }

          .sidebar a:hover {
            background: #343a40;
            color: #fff;
          }

          .sidebar a.active {
            background: #0d6efd;
            color: #fff;
          }
        </style>
      </head>

      <body>
        <div class="container-fluid">
          <div class="row">
            <div class="col-md-2 p-0 sidebar">
              <div class="p-3">
                <h4>Quản lý</h4>
              </div>
              <nav>
                <a href="<%= request.getContextPath() %>/admin/dashboard">Dashboard</a>
                <a href="<%= request.getContextPath() %>/admin/statistics">Thống kê</a>
                <a href="<%= request.getContextPath() %>/admin/managerCategory">Quản lý Danh mục</a>
                <a href="<%= request.getContextPath() %>/admin/managerProduct">Quản lý Sản phẩm</a>
                <a href="<%= request.getContextPath() %>/admin/orders" class="active">Quản lý Đơn hàng</a>
                <a href="<%= request.getContextPath() %>/admin/managerUser">Quản lý Người dùng</a>
                <hr>
                <a href="<%= request.getContextPath() %>/logout" class="text-danger">Đăng xuất</a>
              </nav>
            </div>
            <div class="col-md-10 p-4">

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
                        <td><strong>
                            <fmt:formatNumber value="${ord.totalPrice}" type="number" /> VNĐ
                          </strong></td>
                        <td class="status-cell">
                          <span
                            class="badge ${ord.status == 'Đã hủy' ? 'bg-danger' : (ord.status == 'Đã xác nhận' ? 'bg-success' : 'bg-warning text-dark')}">
                            ${ord.status}
                          </span>
                        </td>
                        <td>
                          <fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                        </td>
                        <td class="action-cell">
                          <button type="button" class="btn btn-sm btn-info text-white"
                            onclick="loadDetail('${ord.orderId}')">Chi tiết</button>

                          <c:if test="${ord.status == 'Chờ xác nhận'}">
                            <button type="button" class="btn btn-sm btn-success btn-action"
                              onclick="openModal('update', '${ord.orderId}', 'Đã xác nhận')">Duyệt đơn</button>
                            <button type="button" class="btn btn-sm btn-danger btn-action"
                              onclick="openModal('cancel', '${ord.orderId}', 'Đã hủy')">Hủy</button>
                          </c:if>

                          <c:if test="${ord.status == 'Đã xác nhận'}">
                            <button type="button" class="btn btn-sm btn-primary btn-action"
                              onclick="openModal('update', '${ord.orderId}', 'Đang giao')">Giao hàng</button>
                            <button type="button" class="btn btn-sm btn-danger btn-action"
                              onclick="openModal('cancel', '${ord.orderId}', 'Đã hủy')">Hủy</button>
                          </c:if>

                          <c:if test="${ord.status == 'Đang giao'}">
                            <button type="button" class="btn btn-sm btn-success btn-action"
                              onclick="openModal('update', '${ord.orderId}', 'Đã giao')">Hoàn thành</button>
                            <button type="button" class="btn btn-sm btn-warning btn-action text-dark"
                              onclick="openModal('cancel', '${ord.orderId}', 'Đã hủy')">Hủy đơn ( hoàn trả )</button>
                          </c:if>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>

              <div class="modal fade" id="actionModal" tabindex="-1">
                <div class="modal-dialog">
                  <div class="modal-content">
                    <div class="modal-header">
                      <h5 id="modalTitle"></h5>
                    </div>
                    <div class="modal-body">
                      <div id="updateBody" style="display:none;">
                        <p>Bạn có chắc chắn muốn chuyển trạng thái đơn hàng thành: <strong id="targetStatusText"
                            class="text-primary"></strong>?</p>
                        <input type="hidden" id="hiddenNewStatus" value="">
                      </div>
                      <div id="cancelBody" style="display:none;"><textarea id="reason" class="form-control"
                          placeholder="Nhập lý do hủy..."></textarea></div>
                    </div>
                    <div class="modal-footer"><button type="button" class="btn btn-secondary"
                        data-bs-dismiss="modal">Đóng</button><button type="button" class="btn btn-primary"
                        onclick="submitAjax()">Xác nhận</button></div>
                  </div>
                </div>
              </div>

              <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
              <script>
                let currentAction = '', currentOrderId = '', nextStatusTarget = '';
                const modal = new bootstrap.Modal(document.getElementById('actionModal'));

                function loadDetail(id) {
                  fetch('${pageContext.request.contextPath}/admin/orders?action=detail&orderId=' + id)
                    .then(res => res.text())
                    .then(html => { document.getElementById('main-container').innerHTML = html; });
                }

                function openModal(action, id, statusValue) {
                  currentAction = action;
                  currentOrderId = id;
                  nextStatusTarget = statusValue;

                  document.getElementById('modalTitle').innerText = action === 'update' ? 'Cập nhật trạng thái' : 'Hủy đơn hàng';

                  if (action === 'update') {
                    document.getElementById('targetStatusText').innerText = statusValue;
                    document.getElementById('hiddenNewStatus').value = statusValue;
                    document.getElementById('updateBody').style.display = 'block';
                    document.getElementById('cancelBody').style.display = 'none';
                  } else {
                    document.getElementById('updateBody').style.display = 'none';
                    document.getElementById('cancelBody').style.display = 'block';
                  }

                  modal.show();
                }

                function submitAjax() {
                  const data = new URLSearchParams();
                  data.append('orderId', currentOrderId);

                  if (currentAction === 'update') {
                    data.append('action', 'update-status');
                    data.append('newStatus', document.getElementById('hiddenNewStatus').value);
                  } else {
                    data.append('action', 'cancel-order');
                    data.append('reason', document.getElementById('reason').value);
                  }

                  fetch('${pageContext.request.contextPath}/admin/orders', {
                    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: data.toString()
                  }).then(res => res.text()).then(result => {
                    if (result.trim() === 'SUCCESS') {
                      modal.hide();
                      location.reload(); // Tải lại trang để đồng bộ hóa các nút thao tác tương ứng với Finite State Machine
                    } else { alert('Lỗi hệ thống, không thể chuyển đổi trạng thái: ' + result); }
                  });
                }

                function loadList() {
                  fetch('${pageContext.request.contextPath}/admin/orders')
                    .then(res => res.text())
                    .then(html => {
                      const parser = new DOMParser();
                      const doc = parser.parseFromString(html, 'text/html');
                      const newContent = doc.getElementById('main-container').innerHTML;
                      document.getElementById('main-container').innerHTML = newContent;
                    })
                    .catch(err => {
                      console.error(err);
                      location.reload();
                    });
                }
              </script>
            </div>
          </div>
        </div>
      </body>

      </html>