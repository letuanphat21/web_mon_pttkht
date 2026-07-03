<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Kích hoạt tài khoản</title>
            </head>

            <body>
                <div class="card">

                    <h2 class="title">
                        <c:choose>
                            <c:when test="${isExpired}">Mã xác nhận đã hết hạn</c:when>
                            <c:when test="${fn:contains(message, 'thành công')}">Kích hoạt thành công</c:when>
                            <c:otherwise>Kích hoạt thất bại</c:otherwise>
                        </c:choose>
                    </h2>

                    <p class="message">
                        <c:out value="${message}" />
                    </p>

                    <c:choose>
                        <c:when test="${isExpired}">
                            <form class="resend-form" method="post" action="${pageContext.request.contextPath}/verify">
                                <input type="hidden" name="email" value="${email}">
                                <button type="submit" class="btn btn-primary" id="btnResend"> Gửi lại mã kích
                                    hoạt</button>
                            </form>
                            <br>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn-secondary">← Quay lại
                                đăng ký</a>
                            <p class="note">Mã mới sẽ được gửi vào email của bạn và có hiệu lực trong 5 phút.</p>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Đăng nhập
                                ngay</a>
                        </c:otherwise>
                    </c:choose>

                </div>
            </body>

            </html>