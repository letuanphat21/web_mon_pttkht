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
        .empty-state { padding: 80px 0; }
    </style>
</head>
<body class="bg-light">

<jsp:include page="header.jsp" />

<div class="container my-5">
    <h2 class="mb-4">Lịch sử đơn hàng của bạn</h2>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${sessionScope.message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <% session.removeAttribute("message"); %>
    </c:if>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <% session.removeAttribute("error"); %>
    </c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="text-center empty-state">
                <div class="mb-4">
                    <span style="font-size: 64px;">📦</span>
                </div>
                <h3>Bạn chưa có đơn hàng nào</h3>
                <p class="text-muted">Có vẻ như bạn chưa thực hiện giao dịch nào với chúng tôi.</p>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary btn-lg mt-3">
                    Mua sắm ngay
                </a>
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
                        <tr>
                            <td class="fw-bold text-primary">${o.orderId}</td>
                            <td>
                                <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                            </td>
                            <td class="fw-bold">
                                <fmt:formatNumber value="${o.totalPrice}" pattern="#,###"/> VNĐ
                            </td>
                            <td>
                                <c:set var="badgeClass" value="bg-secondary"/>
                                <c:if test="${o.status == 'Chờ xác nhận'}"><c:set var="badgeClass" value="bg-warning text-dark"/></c:if>
                                <c:if test="${o.status == 'Đang giao'}"><c:set var="badgeClass" value="bg-info"/></c:if>
                                <c:if test="${o.status == 'Đã giao'}"><c:set var="badgeClass" value="bg-success"/></c:if>
                                <c:if test="${o.status == 'Đã hủy'}"><c:set var="badgeClass" value="bg-danger"/></c:if>

                                <span class="badge status-badge ${badgeClass}">${o.status}</span>
                            </td>
                            <td class="text-center">
                                <div class="btn-group">
                                    <a href="${pageContext.request.contextPath}/order-history?action=detail&id=${o.orderId}"
                                       class="btn btn-sm btn-outline-primary">
                                        Chi tiết
                                    </a>

                                    <c:if test="${o.status == 'Chờ xác nhận'}">
                                        <button class="btn btn-sm btn-danger"
                                                onclick="confirmCancel('${o.orderId}')">
                                            Hủy đơn
                                        </button>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <nav class="mt-4">
                <ul class="pagination justify-content-center">
                    <li class="page-item disabled"><a class="page-link" href="#">Trước</a></li>
                    <li class="page-item active"><a class="page-link" href="#">1</a></li>
                    <li class="page-item disabled"><a class="page-link" href="#">Sau</a></li>
                </ul>
            </nav>
        </c:otherwise>
    </c:choose>
</div>

<div class="modal fade" id="cancelModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/order-history" method="post">
            <input type="hidden" name="action" value="cancel">
            <input type="hidden" name="orderId" id="cancelOrderId">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title text-danger">Xác nhận hủy đơn hàng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>Bạn đang yêu cầu hủy đơn hàng <strong id="displayOrderId"></strong>.</p>
                    <div class="mb-3">
                        <label class="form-label">Lý do hủy đơn (7a3):</label>
                        <select class="form-select" name="reason" required>
                            <option value="">-- Chọn lý do --</option>
                            <option value="Đổi ý, không muốn mua nữa">Đổi ý, không muốn mua nữa</option>
                            <option value="Tìm thấy giá rẻ hơn ở nơi khác">Tìm thấy giá rẻ hơn ở nơi khác</option>
                            <option value="Thay đổi địa chỉ giao hàng">Thay đổi địa chỉ giao hàng</option>
                            <option value="Khác">Lý do khác</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="submit" class="btn btn-danger">Đồng ý hủy (7a4)</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function confirmCancel(orderId) {
        document.getElementById('cancelOrderId').value = orderId;
        document.getElementById('displayOrderId').innerText = orderId;
        var myModal = new bootstrap.Modal(document.getElementById('cancelModal'));
        myModal.show();
    }
</script>
</body>
</html>