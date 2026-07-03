<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <title>Thống kê</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    .sidebar {
                        min-height: 100vh;
                        background: #212529;
                        color: white;
                    }

                    .sidebar a {
                        color: #adb5bd;
                        text-decoration: none;
                        padding: 10px 20px;
                        display: block;
                    }

                    .sidebar a:hover {
                        background: #343a40;
                        color: white;
                    }

                    .sidebar a.active {
                        background: #0d6efd;
                        color: white;
                    }

                    .metric {
                        border-left: 4px solid #0d6efd;
                    }

                    .chart-box {
                        height: 320px;
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
                                <a href="<%= request.getContextPath() %>/admin/statistics" class="active">Thống kê</a>
                                <a href="<%= request.getContextPath() %>/admin/managerCategory">Quản lý Danh mục</a>
                                <a href="<%= request.getContextPath() %>/admin/managerProduct">Quản lý Sản phẩm</a>
                                <a href="<%= request.getContextPath() %>/admin/orders">Quản lý Đơn hàng</a>
                                <a href="<%= request.getContextPath() %>/admin/managerUser">Quản lý Người dùng</a>
                                <hr>
                                <a href="<%= request.getContextPath() %>/logout" class="text-danger">Đăng xuất</a>
                            </nav>
                        </div>

                        <div class="col-md-10 p-4">
                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <h2 class="mb-0">Thống kê</h2>
                                <a class="btn btn-outline-primary"
                                    href="${pageContext.request.contextPath}/admin/statistics?action=export&type=${type}&startDate=${startDate}&endDate=${endDate}&categoryId=${categoryId}">
                                    Xuất báo cáo CSV
                                </a>
                            </div>

                            <c:if test="${not empty error}">
                                <div class="alert alert-danger">${error}</div>
                            </c:if>
                            <c:if test="${not empty message}">
                                <div class="alert alert-warning">${message}</div>
                            </c:if>

                            <form class="row g-3 align-items-end mb-4"
                                action="${pageContext.request.contextPath}/admin/statistics" method="get">
                                <div class="col-md-3">
                                    <label class="form-label">Loại thống kê</label>
                                    <select class="form-select" name="type">
                                        <option value="overview" ${type=='overview' ? 'selected' : '' }>Tổng quan
                                        </option>
                                        <option value="revenue" ${type=='revenue' ? 'selected' : '' }>Doanh thu</option>
                                        <option value="products" ${type=='products' ? 'selected' : '' }>Sản phẩm bán
                                            chạy</option>
                                        <option value="categories" ${type=='categories' ? 'selected' : '' }>Theo danh
                                            mục</option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">Từ ngày</label>
                                    <input class="form-control" type="date" name="startDate" value="${startDate}"
                                        required>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">Đến ngày</label>
                                    <input class="form-control" type="date" name="endDate" value="${endDate}" required>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Danh mục</label>
                                    <select class="form-select" name="categoryId">
                                        <option value="">Tất cả danh mục</option>
                                        <c:forEach var="category" items="${categories}">
                                            <option value="${category.id}" ${categoryId==category.id ? 'selected' : ''
                                                }>
                                                <c:out value="${category.name}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2 d-grid">
                                    <button type="submit" class="btn btn-primary">Xác nhận</button>
                                </div>
                            </form>

                            <div class="row mb-4">
                                <div class="col-md-4">
                                    <div class="card metric">
                                        <div class="card-body">
                                            <div class="text-muted">Doanh thu</div>
                                            <div class="fs-3 fw-bold">
                                                <fmt:formatNumber value="${summary.revenue}" pattern="#,###" /> VNĐ
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card metric">
                                        <div class="card-body">
                                            <div class="text-muted">Đơn hàng hợp lệ</div>
                                            <div class="fs-3 fw-bold">${summary.orderCount}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card metric">
                                        <div class="card-body">
                                            <div class="text-muted">Sản phẩm đã bán</div>
                                            <div class="fs-3 fw-bold">${summary.soldQuantity}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <c:if test="${type == 'overview' || type == 'revenue'}">
                                <div class="card mb-4">
                                    <div class="card-header fw-bold">Doanh thu theo ngày</div>
                                    <div class="card-body chart-box">
                                        <canvas id="revenueChart"></canvas>
                                    </div>
                                </div>

                                <table class="table table-bordered table-hover mb-4">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Ngày</th>
                                            <th>Số đơn hàng</th>
                                            <th>Doanh thu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${dailyRevenue}">
                                            <tr>
                                                <td>${row.label}</td>
                                                <td>${row.orderCount}</td>
                                                <td>
                                                    <fmt:formatNumber value="${row.revenue}" pattern="#,###" /> VNĐ
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>

                            <c:if test="${type == 'overview' || type == 'products'}">
                                <div class="card mb-4">
                                    <div class="card-header fw-bold">Top sản phẩm bán chạy</div>
                                    <div class="card-body chart-box">
                                        <canvas id="productChart"></canvas>
                                    </div>
                                </div>

                                <table class="table table-bordered table-hover mb-4">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Mã SP</th>
                                            <th>Sản phẩm</th>
                                            <th>Danh mục</th>
                                            <th>Số lượng</th>
                                            <th>Doanh thu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${topProducts}">
                                            <tr>
                                                <td>${row.productId}</td>
                                                <td>
                                                    <c:out value="${row.productName}" />
                                                </td>
                                                <td>
                                                    <c:out value="${row.categoryName}" />
                                                </td>
                                                <td>${row.soldQuantity}</td>
                                                <td>
                                                    <fmt:formatNumber value="${row.revenue}" pattern="#,###" /> VNĐ
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>

                            <c:if test="${type == 'overview' || type == 'categories'}">
                                <div class="card mb-4">
                                    <div class="card-header fw-bold">Doanh thu theo danh mục</div>
                                    <div class="card-body chart-box">
                                        <canvas id="categoryChart"></canvas>
                                    </div>
                                </div>

                                <table class="table table-bordered table-hover mb-4">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Danh mục</th>
                                            <th>Số lượng bán</th>
                                            <th>Doanh thu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${categoryRevenue}">
                                            <tr>
                                                <td>
                                                    <c:out value="${row.categoryName}" />
                                                </td>
                                                <td>${row.soldQuantity}</td>
                                                <td>
                                                    <fmt:formatNumber value="${row.revenue}" pattern="#,###" /> VNĐ
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>

                            <div class="card">
                                <div class="card-header fw-bold">Trạng thái đơn hàng trong khoảng thời gian</div>
                                <div class="card-body p-0">
                                    <table class="table table-bordered table-hover mb-0">
                                        <thead class="table-dark">
                                            <tr>
                                                <th>Trạng thái</th>
                                                <th>Số đơn</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="row" items="${orderStatuses}">
                                                <tr>
                                                    <td>
                                                        <c:out value="${row.status}" />
                                                    </td>
                                                    <td>${row.orderCount}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    const chartData = <c:out value="${chartJson}" default="{}" escapeXml="false" />;
                    const currencyFormatter = new Intl.NumberFormat('vi-VN');

                    function renderChart(id, type, labels, values, label, color) {
                        const element = document.getElementById(id);
                        if (!element) return;

                        new Chart(element, {
                            type: type,
                            data: {
                                labels: labels || [],
                                datasets: [{
                                    label: label,
                                    data: values || [],
                                    backgroundColor: color,
                                    borderColor: color,
                                    tension: 0.25
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    tooltip: {
                                        callbacks: {
                                            label: function (context) {
                                                return label + ': ' + currencyFormatter.format(context.raw);
                                            }
                                        }
                                    }
                                },
                                scales: type === 'pie' ? {} : { y: { beginAtZero: true } }
                            }
                        });
                    }

                    renderChart('revenueChart', 'line', chartData.dailyLabels, chartData.dailyRevenue, 'Doanh thu', '#0d6efd');
                    renderChart('productChart', 'bar', chartData.productLabels, chartData.productQuantity, 'Số lượng bán', '#198754');
                    renderChart('categoryChart', 'pie', chartData.categoryLabels, chartData.categoryRevenue, 'Doanh thu', ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1', '#20c997']);
                </script>
            </body>

            </html>