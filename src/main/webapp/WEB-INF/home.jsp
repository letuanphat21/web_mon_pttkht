<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page isELIgnored="false" %>
        <%@ taglib uri="jakarta.tags.core" prefix="c" %>
            <%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
                <html>

                <head>
                    <title>Trang chủ - Web Quần Áo</title>
                </head>

                <body>

                    <jsp:include page="header.jsp" />

                    <div class="container" style="padding: 20px; max-width: 1200px; margin: 0 auto;">

                        <!-- <section class="user-info">
        <h2>Xin chào, <c:out value="${sessionScope.user.getFullName()}" default="Khách"/></h2>
        <p>Email: <c:out value="${sessionScope.user.getEmail()}" default="Chưa cập nhật"/></p>
        <div style="margin-top: 10px;">
            <a href="${pageContext.request.contextPath}/logout"
               style="color: #dc3545; font-weight: bold; text-decoration: none;
               border: 1px solid #dc3545; padding: 5px 10px; border-radius: 4px;">
                ❌ Đăng xuất để đăng nhập tài khoản khác
            </a>
        </div>
    </section> -->

                        <hr>

                        <h3>Danh sách sản phẩm mới nhất</h3>
                        <div style="display: flex; flex-wrap: wrap; gap: 20px;">
                            <c:forEach var="p" items="${products}">

                                <c:set var="safeDesc">
                                    <c:choose>
                                        <c:when test="${not empty p.productDescription}">
                                            <c:out value="${p.productDescription}" />
                                        </c:when>
                                        <c:otherwise>Chưa có mô tả cho sản phẩm này.</c:otherwise>
                                    </c:choose>
                                </c:set>

                                <div class="product-card" onclick="openProductModalFromData(this)"
                                    data-id="${p.productId}" data-name="<c:out value='${p.productName}' />"
                                    data-price="${p.productPrice}" data-image="${p.productImage}"
                                    data-desc="${safeDesc}"
                                    style="border: 1px solid #ddd; padding: 15px; width: 200px; border-radius: 8px; cursor: pointer; transition: transform 0.2s;">

                                    <img src="${p.productImage}" alt="${p.productName}"
                                        style="width: 100%; height: 200px; object-fit: cover; border-radius: 4px;">
                                    <h4 style="margin: 10px 0; color: #007bff;">${p.productName}</h4>
                                    <p style="color: #e44d26; font-weight: bold;">
                                        Giá:
                                        <fmt:formatNumber value="${p.productPrice}" pattern="#,###" /> VNĐ
                                    </p>

                                    <div style="text-align: center; margin-top: 10px;">
                                        <span
                                            style="font-size: 13px; color: white; background: #007bff; padding: 5px 12px; border-radius: 4px; display: inline-block;">
                                            Xem chi tiết
                                        </span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                    <div id="product-detail-modal"
                        style="display:none; position:fixed; z-index:998; background:rgba(0,0,0,0.5); top:0; left:0; width:100%; height:100%;">
                        <div
                            style="background:white; width: 450px; padding:25px; position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); text-align: center;">

                            <span onclick="closeProductModal()"
                                style="position: absolute; right: 15px; top: 10px; font-size: 24px; cursor: pointer; color: #aaa;">&times;</span>

                            <img id="modal-p-image" src=""
                                style="width: 100%; height: 250px; object-fit: cover; border-radius: 6px;">
                            <h3 id="modal-p-name" style="margin: 15px 0 5px 0; color: #333;"></h3>
                            <p id="modal-p-price"
                                style="color: #e44d26; font-weight: bold; font-size: 20px; margin: 5px 0;"></p>

                            <p style="font-weight: bold; margin-bottom: 2px; text-align: left;">Mô tả:</p>
                            <p id="modal-p-desc"
                                style="color: #666; font-size: 14px; text-align: left; margin-top: 0; background: #f9f9f9; padding: 10px; border-radius: 4px; max-height: 80px; overflow-y: auto;">
                            </p>

                            <hr style="border:0; border-top: 1px solid #eee; margin: 15px 0;">

                            <div style="display: flex; align-items: center; justify-content: center; gap: 15px;">
                                <label for="modal-p-qty" style="font-weight: 500;">Số lượng:</label>
                                <input type="number" id="modal-p-qty" value="1" min="1"
                                    style="width: 60px; padding: 6px; text-align: center; border: 1px solid #ccc; border-radius: 4px;">

                                <input type="hidden" id="modal-p-id">

                                <button onclick="triggerAddToCartFromModal()"
                                    style="padding: 8px 20px; cursor: pointer; background: #28a745; color: white; border: none; border-radius: 5px; font-weight: bold;">
                                    🛒 Thêm vào giỏ
                                </button>
                            </div>
                        </div>
                    </div>


                    <div id="cart-modal"
                        style="display:none; position:fixed; z-index:999; background:white; border:1px solid #ccc; padding:20px; top:50%; left:50%; transform:translate(-50%, -50%); box-shadow: 0 4px 12px rgba(0,0,0,0.3); border-radius: 10px; min-width: 300px; text-align: center;">
                        <h4 style="color: green; margin-top: 0; font-size: 18px;">Thêm sản phẩm thành công!</h4>
                        <p style="color: #555;">Bạn muốn làm gì tiếp theo?</p>
                        <hr style="border: 0; border-top: 1px solid #eee;">
                        <div style="display: flex; justify-content: space-around; margin-top: 15px;">
                            <button onclick="continueShopping()"
                                style="padding: 8px 15px; cursor: pointer; background: #eee; border: 1px solid #ccc; border-radius: 4px;">Tiếp
                                tục mua sắm</button>
                            <button onclick="viewCart()"
                                style="padding: 8px 15px; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 4px; font-weight: bold;">Xem
                                giỏ hàng</button>
                        </div>
                    </div>

                    <script src="<c:url value='/assets/js/cart.js'/>"></script>
                </body>

                </html>