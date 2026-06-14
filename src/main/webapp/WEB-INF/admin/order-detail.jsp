<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="p-4 bg-white rounded shadow-sm">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h3>Chi tiết đơn hàng: ${orderDetail.orderId}</h3>
        <button type="button" class="btn btn-secondary" onclick="loadList()">Quay lại</button>
    </div>
    <div class="row">
        <div class="col-md-6">
            <p>Khách hàng: <strong>${orderDetail.fullName}</strong></p>
            <p>Địa chỉ: <strong>${orderDetail.address}</strong></p>
        </div>
        <div class="col-md-6 text-end">
            <p>Trạng thái: <strong>${orderDetail.status}</strong></p>
            <p>Ngày đặt: <fmt:formatDate value="${orderDetail.createdAt}" pattern="dd/MM/yyyy HH:mm"/></p>
        </div>
    </div>
    <table class="table table-bordered">
        <thead class="table-dark">
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
                    <img src="${item.productImage}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px; margin-right: 10px;" alt="img">
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
            <td colspan="3" class="text-end fw-bold">Tổng:</td>
            <td class="text-end fw-bold text-danger"><fmt:formatNumber value="${orderDetail.totalPrice}" pattern="#,###"/> đ</td>
        </tr>
        </tfoot>
    </table>
</div>