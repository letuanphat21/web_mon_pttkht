<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page isELIgnored="false" %>
        <%@ taglib uri="jakarta.tags.core" prefix="c" %>
            <%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
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

                    .table td,
                    .table th {
                        vertical-align: middle;
                    }

                    .role-badge {
                        margin-right: 4px;
                    }
                </style>
                </style>
                <header style="background: #f8f9fa; padding: 15px; border-bottom: 1px solid #ddd;">
                    <div
                        style="display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto;">

                        <div class="logo">
                            <a href="${pageContext.request.contextPath}/"
                                style="text-decoration: none; font-size: 24px; font-weight: bold; color: #333;">
                                WebQuầnÁo
                            </a>
                        </div>

                        <nav>
                            <ul style="list-style: none; display: flex; gap: 20px; margin: 0;">
                                <li><a href="${pageContext.request.contextPath}/"
                                        style="text-decoration: none; color: #333;">Trang chủ</a></li>
                                <li><a href="${pageContext.request.contextPath}/shop"
                                        style="text-decoration: none; color: #333;">Sản phẩm</a></li>
                                <li><a href="#" style="text-decoration: none; color: #333;">Liên hệ</a></li>
                            </ul>
                        </nav>

                        <div style="display: flex; align-items: center; gap: 30px;">
                            <c:if test="${not empty sessionScope.username}">
                                <div class="history-header">
                                    <a href="${pageContext.request.contextPath}/order-history"
                                        style="text-decoration: none; color: #333; font-weight: 500;">
                                        <span style="font-size: 18px;">📋</span> Đơn hàng của tôi
                                    </a>
                                </div>
                            </c:if>

                            <div class="cart-header">
                                <a href="${pageContext.request.contextPath}/cart"
                                    style="text-decoration: none; color: #333; position: relative; font-weight: 500;">
                                    <span style="font-size: 20px;">🛒</span> Giỏ hàng

                                    <span id="cart-badge" style="
                        background: red;
                        color: white;
                        border-radius: 50%;
                        padding: 2px 7px;
                        font-size: 12px;
                        position: absolute;
                        top: -10px;
                        right: -15px;">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.userId}">
                                                <c:out value="${sessionScope.totalCartCount}" default="0" />
                                            </c:when>

                                            <c:when test="${not empty sessionScope.cart}">
                                                ${fn:length(sessionScope.cart.items)}
                                            </c:when>
                                            <c:otherwise>0</c:otherwise>
                                        </c:choose>
                                    </span>
                                </a>
                            </div>

                            <c:choose>
                                <c:when test="${empty sessionScope.user}">
                                    <div style="display: flex; gap: 10px;">
                                        <a href="${pageContext.request.contextPath}/login"
                                            style="text-decoration: none; color: #333; border: 1px solid #333; padding: 6px 14px; border-radius: 4px; font-weight: 500;">
                                            Login
                                        </a>
                                        <a href="${pageContext.request.contextPath}/register"
                                            style="text-decoration: none; color: white; background: #007bff; border: 1px solid #007bff; padding: 6px 14px; border-radius: 4px; font-weight: 500;">
                                            Register
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="display: flex; gap: 15px; align-items: center;">
                                        <span style="font-weight: 500; color: #333;">Xin chào,
                                            ${sessionScope.user.getFullName()}!</span>
                                        <a href="${pageContext.request.contextPath}/logout"
                                            style="text-decoration: none; color: white; background: #dc3545; border: 1px solid #dc3545; padding: 6px 14px; border-radius: 4px; font-weight: 500;">
                                            Logout
                                        </a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </header>