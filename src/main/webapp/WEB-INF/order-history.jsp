<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đơn hàng | WebQuầnÁo</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .table-container { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .status-badge { font-weight: 500; padding: 0.5em 0.8em; }
        .product-img-mini { width: 50px; height: 50px; object-fit: cover; border-radius: 5px; }
        .summary-label { color: #6c757d; }
        .spinner-container { display: none; text-align: center; padding: 20px; }
    </style>
</head>
<body class="bg-light">

<jsp:include page="header.jsp" />

<div class="container my-5">
    <h2 class="mb-4">Lịch sử đơn hàng của bạn</h2>

    <div id="alert-container"></div>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="text-center py-5">
                <h3>Bạn chưa có đơn hàng nào</h3>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary mt-3">Mua sắm ngay</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-container">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>Mã đơn hàng</th>
                        <th>Ngày đặt hàng</th>
                        <th>Tổng thanh toán</th>
                        <th>Trạng thái</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="o" items="${orders}">
                        <tr id="order-row-${o.orderId}">
                            <td class="fw-bold text-primary">${o.orderId}</td>
                            <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            <td class="fw-bold"><fmt:formatNumber value="${o.totalPrice}" pattern="#,###"/> VNĐ</td>
                            <td>
                                <c:set var="badgeClass" value="bg-secondary"/>
                                <c:if test="${o.status == 'Chờ xác nhận'}"><c:set var="badgeClass" value="bg-warning text-dark"/></c:if>
                                <c:if test="${o.status == 'Đang giao'}"><c:set var="badgeClass" value="bg-info"/></c:if>
                                <c:if test="${o.status == 'Đã giao'}"><c:set var="badgeClass" value="bg-success"/></c:if>
                                <c:if test="${o.status == 'Đã hủy'}"><c:set var="badgeClass" value="bg-danger"/></c:if>
                                <span class="badge status-badge ${badgeClass}" id="status-badge-${o.orderId}">${o.status}</span>
                            </td>
                            <td class="text-center">
                                <div class="btn-group">
                                    <button class="btn btn-sm btn-outline-primary" onclick="showOrderDetail('${o.orderId}')">Chi tiết</button>
                                    <c:if test="${o.status == 'Chờ xác nhận'}">
                                        <button class="btn btn-sm btn-danger btn-cancel-action" id="btn-cancel-${o.orderId}" onclick="openCancelModal('${o.orderId}')">Hủy đơn</button>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="modal fade" id="orderDetailModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">Chi tiết đơn hàng #<span id="detail-order-id"></span></h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="detail-modal-body">
                <div class="spinner-container" id="detail-spinner">
                    <div class="spinner-border text-primary" role="status"></div>
                    <p>Đang tải thông tin...</p>
                </div>
                <div id="detail-content"></div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="cancelModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title text-danger">Xác nhận hủy đơn hàng</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>Bạn muốn hủy đơn hàng <strong id="displayOrderId"></strong>?</p>
                <div class="mb-3">
                    <label class="form-label">Lý do hủy:</label>
                    <select class="form-select" id="cancelReason">
                        <option value="Đổi ý, không muốn mua nữa">Đổi ý, không muốn mua nữa</option>
                        <option value="Tìm thấy giá rẻ hơn ở nơi khác">Tìm thấy giá rẻ hơn ở nơi khác</option>
                        <option value="Khác">Lý do khác</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="button" class="btn btn-danger" onclick="submitCancel()">Xác nhận hủy Ajax</button>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    var detailModalObj;
    var cancelModalObj;

    (function() {
        if (typeof bootstrap === 'undefined') {
            alert("LỖI: Thư viện Bootstrap chưa được tải! Kiểm tra kết nối mạng.");
            return;
        }

        window.onload = function() {
            console.log("DOM loaded");
            try {
                detailModalObj = new bootstrap.Modal(document.getElementById('orderDetailModal'));
                cancelModalObj = new bootstrap.Modal(document.getElementById('cancelModal'));
            } catch (e) {
                console.error("Lỗi khởi tạo Modal:", e);
            }
        };
    })();

    function showOrderDetail(id) {
        console.log("Bấm nút chi tiết ID:", id);

        var content = document.getElementById('detail-content');
        var spinner = document.getElementById('detail-spinner');
        document.getElementById('detail-order-id').innerText = id;

        content.innerHTML = '';
        spinner.style.display = 'block';

        if(detailModalObj) detailModalObj.show();

        fetch('order-history?action=detail&id=' + id, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(function(res) {
                if(!res.ok) throw new Error("Mã lỗi: " + res.status);
                return res.text();
            })
            .then(function(html) {
                spinner.style.display = 'none';
                content.innerHTML = html;
            })
            .catch(function(err) {
                spinner.style.display = 'none';
                content.innerHTML = '<p class="text-danger">Lỗi tải dữ liệu: ' + err.message + '</p>';
            });
    }

    function openCancelModal(id) {
        window.currentOrderId = id;
        document.getElementById('displayOrderId').innerText = id;
        if(cancelModalObj) cancelModalObj.show();
    }

    function submitCancel() {
        var reason = document.getElementById('cancelReason').value;
        var id = window.currentOrderId;

        fetch('order-history', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=cancel&orderId=' + id + '&reason=' + encodeURIComponent(reason)
        })
            .then(function(res) {
                if (!res.ok) {
                    return res.text().then(text => { throw new Error("Server trả về lỗi: " + text) });
                }
                return res.json();
            })
            .then(function(data) {
                if(data.status === 'success') {
                    if(cancelModalObj) cancelModalObj.hide();

                    var badge = document.getElementById('status-badge-' + id);
                    if (badge) {
                        badge.innerText = 'Đã hủy';
                        badge.className = 'badge status-badge bg-danger';
                    }

                    var btnCancel = document.getElementById('btn-cancel-' + id);
                    if (btnCancel) btnCancel.remove();

                    showAlert('Đã hủy đơn hàng ' + id + ' thành công!', 'success');
                } else {
                    showAlert(data.message, 'danger');
                }
            })
            .catch(function(err) {
                console.error("Chi tiết lỗi:", err);
                alert("Lỗi: " + err.message);
            });
    }

    function showAlert(msg, type) {
        const container = document.getElementById('alert-container');
        if (!container) return;

        container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show">
            ${msg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>`;

        setTimeout(() => {
            const alertElement = container.querySelector('.alert');
            if (alertElement) {
                const bsAlert = new bootstrap.Alert(alertElement);
                bsAlert.close();
            }
        }, 3000);
    }
</script>
</body>
</html>