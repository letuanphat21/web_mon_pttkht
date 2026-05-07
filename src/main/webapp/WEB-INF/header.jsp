<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<header style="background: #f8f9fa; padding: 15px; border-bottom: 1px solid #ddd;">
    <div style="display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto;">

        <div class="logo">
            <a href="${pageContext.request.contextPath}/home" style="text-decoration: none; font-size: 24px; font-weight: bold; color: #333;">
                WebQuầnÁo
            </a>
        </div>

        <nav>
            <ul style="list-style: none; display: flex; gap: 20px; margin: 0;">
                <li><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>
                <li><a href="#">Sản phẩm</a></li>
                <li><a href="#">Liên hệ</a></li>
            </ul>
        </nav>

        <div class="cart-header">
            <a href="${pageContext.request.contextPath}/cart" style="text-decoration: none; color: #333; position: relative;">
                <span style="font-size: 20px;">🛒</span> Giỏ hàng

                <span id="cart-count" style="
                    background: red;
                    color: white;
                    border-radius: 50%;
                    padding: 2px 7px;
                    font-size: 12px;
                    position: absolute;
                    top: -10px;
                    right: -15px;">
                    <c:choose>
                        <c:when test="${not empty sessionScope.cart}">
                            ${fn:length(sessionScope.cart)}
                        </c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                </span>
            </a>
        </div>
    </div>
</header>