<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý người dùng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .sidebar { min-height: 100vh; background: #212529; color: #fff; }
        .sidebar a { color: #adb5bd; text-decoration: none; padding: 10px 20px; display: block; }
        .sidebar a:hover { background: #343a40; color: #fff; }
        .sidebar a.active { background: #0d6efd; color: #fff; }
        .table td, .table th { vertical-align: middle; }
        .role-badge { margin-right: 4px; }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 p-0 sidebar">
            <div class="p-3"><h4>Quản lý</h4></div>
            <nav>
                <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/managerCategory">Quản lý Danh mục</a>
                <a href="${pageContext.request.contextPath}/admin/invoice?action=list">Quản lý Hóa đơn</a>
                <a href="${pageContext.request.contextPath}/admin/managerUser" class="active">Quản lý Người dùng</a>
                <hr>
                <a href="${pageContext.request.contextPath}/logout" class="text-danger">Đăng xuất</a>
            </nav>
        </div>

        <main class="col-md-10 p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="mb-1">Quản lý người dùng</h2>
                    <div class="text-muted">Thêm, sửa, khóa tài khoản và phân quyền.</div>
                </div>
                <button type="button" class="btn btn-primary" onclick="openAddModal()">+ Thêm mới</button>
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

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <table class="table table-bordered table-hover">
                <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Điện thoại</th>
                    <th>Quyền</th>
                    <th>Trạng thái</th>
                    <th style="width: 180px;">Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}">
                    <c:set var="roleIdsText" value="" />
                    <c:forEach var="role" items="${user.roles}" varStatus="status">
                        <c:set var="roleIdsText" value="${roleIdsText}${role.id}${status.last ? '' : ','}" />
                    </c:forEach>
                    <tr>
                        <td><c:out value="${user.id}"/></td>
                        <td><c:out value="${user.fullName}"/></td>
                        <td><c:out value="${user.email}"/></td>
                        <td><c:out value="${user.phone}"/></td>
                        <td>
                            <c:forEach var="role" items="${user.roles}">
                                <span class="badge bg-info text-dark role-badge"><c:out value="${role.name}"/></span>
                            </c:forEach>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${user.active}">
                                    <span class="badge bg-success">Đang hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Đã khóa</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <button type="button"
                                    class="btn btn-sm btn-primary"
                                    data-id="<c:out value='${user.id}'/>"
                                    data-full-name="<c:out value='${user.fullName}'/>"
                                    data-email="<c:out value='${user.email}'/>"
                                    data-phone="<c:out value='${user.phone}'/>"
                                    data-address="<c:out value='${user.address}'/>"
                                    data-role-ids="<c:out value='${roleIdsText}'/>"
                                    onclick="openEditModal(this)">
                                Sửa
                            </button>
                            <button type="button"
                                    class="btn btn-sm ${user.active ? 'btn-warning' : 'btn-outline-success'}"
                                    data-id="<c:out value='${user.id}'/>"
                                    data-name="<c:out value='${user.fullName}'/>"
                                    data-active="<c:out value='${user.active}'/>"
                                    onclick="openLockModal(this)">
                                ${user.active ? 'Khóa' : 'Mở khóa'}
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </main>
    </div>
</div>

<div class="modal fade" id="userModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/managerUser" method="post">
                <div class="modal-header">
                    <h5 class="modal-title" id="userModalTitle">Thêm user</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="action" id="userAction" value="add">
                    <input type="hidden" name="id" id="userId">

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Họ tên <span class="text-danger">*</span></label>
                            <input type="text" name="fullName" id="fullName" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Email <span class="text-danger">*</span></label>
                            <input type="email" name="email" id="email" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Mật khẩu <span id="passwordRequired" class="text-danger">*</span></label>
                            <input type="password" name="password" id="password" class="form-control">
                            <small class="text-muted" id="passwordHint">Bắt buộc khi thêm mới. Khi sửa, để trống nếu không đổi mật khẩu.</small>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Điện thoại</label>
                            <input type="text" name="phone" id="phone" class="form-control">
                        </div>
                        <div class="col-12">
                            <label class="form-label">Địa chỉ</label>
                            <input type="text" name="address" id="address" class="form-control">
                        </div>
                        <div class="col-12">
                            <label class="form-label">Quyền hạn <span class="text-danger">*</span></label>
                            <div class="d-flex flex-wrap gap-3">
                                <c:forEach var="role" items="${roles}">
                                    <label class="form-check">
                                        <input class="form-check-input role-checkbox" type="checkbox" name="roleIds" value="${role.id}">
                                        <span class="form-check-label"><c:out value="${role.name}"/></span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-success">Xác nhận</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="lockModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/managerUser" method="post">
                <div class="modal-header">
                    <h5 class="modal-title">Xác nhận</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="action" value="toggleLock">
                    <input type="hidden" name="id" id="lockUserId">
                    <p id="lockMessage" class="mb-0"></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Xác nhận</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const userModal = new bootstrap.Modal(document.getElementById('userModal'));
    const lockModal = new bootstrap.Modal(document.getElementById('lockModal'));

    // UC-1.11 / 2a1.1 -> 2a1.2: Admin nhan "Them moi", he thong hien thi form nhap lieu.
    function openAddModal() {
        document.getElementById('userModalTitle').innerText = 'Thêm user';
        document.getElementById('userAction').value = 'add';
        document.getElementById('userId').value = '';
        document.getElementById('fullName').value = '';
        document.getElementById('email').value = '';
        document.getElementById('password').value = '';
        document.getElementById('password').required = true;
        document.getElementById('phone').value = '';
        document.getElementById('address').value = '';
        setCheckedRoles([]);
        userModal.show();
    }

    // UC-1.11 / 2a2.1 -> 2a2.3: Admin chon user can sua, he thong do du lieu hien tai vao form.
    function openEditModal(button) {
        document.getElementById('userModalTitle').innerText = 'Sửa user';
        document.getElementById('userAction').value = 'update';
        document.getElementById('userId').value = button.dataset.id;
        document.getElementById('fullName').value = button.dataset.fullName || '';
        document.getElementById('email').value = button.dataset.email || '';
        document.getElementById('password').value = '';
        document.getElementById('password').required = false;
        document.getElementById('phone').value = button.dataset.phone || '';
        document.getElementById('address').value = button.dataset.address || '';
        setCheckedRoles((button.dataset.roleIds || '').split(',').filter(Boolean));
        userModal.show();
    }

    // UC-1.11 / 2a3.2 -> 2a3.3: Admin nhan khoa/mo khoa, he thong hien thi modal canh bao.
    function openLockModal(button) {
        const isActive = button.dataset.active === 'true';
        const actionText = isActive ? 'khóa' : 'mở khóa';
        document.getElementById('lockUserId').value = button.dataset.id;
        document.getElementById('lockMessage').innerText =
            'Bạn có chắc chắn muốn ' + actionText + ' tài khoản "' + (button.dataset.name || '') + '"?';
        lockModal.show();
    }

    function setCheckedRoles(roleIds) {
        document.querySelectorAll('.role-checkbox').forEach(function (checkbox) {
            checkbox.checked = roleIds.includes(checkbox.value);
        });
    }
</script>
</body>
</html>
