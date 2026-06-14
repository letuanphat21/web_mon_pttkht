<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="row">
    <div class="col-md-5">
        <h6 class="fw-bold">Thông tin nhận hàng</h6>
        <p class="small mb-1">Người nhận: <strong>${orderDetail.fullName}</strong></p>
        <p class="small mb-1">SĐT: <strong>${orderDetail.phone}</strong></p>
        <p class="small">Địa chỉ: <strong>${orderDetail.address}</strong></p>
    </div>
    <div class="col-md-7 text-end">
        <h6 class="fw-bold">Trạng thái: <span class="text-primary">${orderDetail.status}</span></h6>
        <p class="small">Ngày đặt: <fmt:formatDate value="${orderDetail.createdAt}" pattern="dd/MM/yyyy HH:mm"/></p>
    </div>
</div>
<hr>
<table class="table table-sm align-middle">
    <thead>
    <tr>
        <th>Sản phẩm</th>
        <th>Giá</th>
        <th class="text-center">SL</th>
        <th class="text-end">Tạm tính</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${orderDetail.orderDetails}">
        <tr>
            <td>
                <img src="${item.productImage}" class="product-img-mini" alt="ảnh">
                    ${item.productName}
            </td>
            <td><fmt:formatNumber value="${item.price}" pattern="#,###"/> đ</td>
            <td class="text-center">${item.quantity}</td>
            <td class="text-end fw-bold"><fmt:formatNumber value="${item.subTotal}" pattern="#,###"/> đ</td>
        </tr>
    </c:forEach>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="3" class="text-end fw-bold">Tổng cộng:</td>
        <td class="text-end text-danger fw-bold fs-5"><fmt:formatNumber value="${orderDetail.totalPrice}" pattern="#,###"/> đ</td>
    </tr>
    </tfoot>
</table>