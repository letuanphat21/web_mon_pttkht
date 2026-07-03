package org.example.webquanao.service;

import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.dao.PaymentDAO;
import org.example.webquanao.dto.response.OrderDetailResponse;
import org.example.webquanao.dto.response.OrderResponse;
import org.example.webquanao.entity.Payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.UUID;

public class PaymentService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // ===== MOMO CONFIG =====
    private static final String MOMO_PARTNER_CODE = "MOMO";
    private static final String MOMO_ACCESS_KEY   = "F8BBA842ECF85";
    private static final String MOMO_SECRET_KEY   = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String MOMO_ENDPOINT     = "https://test-payment.momo.vn/v2/gateway/api/create";

    /**
     * Xử lý xác nhận đơn COD
     */
    public boolean processCODPayment(String orderId) {
        boolean updated = orderDAO.updateStatus(orderId, "Chờ xác nhận");
        if (updated) {
            // COD: tiền chỉ thu khi giao hàng -> payment_status vẫn "Chưa thanh toán"
            Payment payment = new Payment(orderId, "COD", 0);
            paymentDAO.insertPayment(payment);
        }
        return updated;
    }

    /**
     * Xử lý cập nhật đơn MoMo thành công
     */
    public boolean processMomoSuccess(String orderId) {
        if (orderId == null) return false;
        boolean updated = orderDAO.updateOrderStatus(orderId, "Đã xác nhận");
        if (updated) {
            // MoMo: thanh toán online thành công ngay lúc redirect về
            paymentDAO.updatePaymentStatus(orderId, "Đã thanh toán", true);
        }
        return updated;
    }

    /**
     * Tạo URL cổng thanh toán MoMo Sandbox
     */
    public String createMomoUrl(OrderResponse orderResponse, String contextPath, String baseReturnUrl) throws Exception {
        long totalAmount = 0;
        if (orderResponse.getOrderDetails() != null) {
            for (OrderDetailResponse item : orderResponse.getOrderDetails()) {
                totalAmount += item.getSubTotal();
            }
        }

        if (totalAmount <= 0) {
            throw new Exception("Tổng số tiền đơn hàng không hợp lệ.");
        }

        String momoOrderId  = "MOMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String requestId    = UUID.randomUUID().toString();
        // Lấy orderId thật từ orderRef (VD: "ORD-ABCD1234")
        // getOrderId() = 0 vì đã bỏ hashCode
        String realOrderId = (orderResponse.getShippingInfo() != null
                && orderResponse.getShippingInfo().getOrderRef() != null)
                ? orderResponse.getShippingInfo().getOrderRef()
                : "ORD-UNKNOWN";
        String orderInfo = "Thanh toan don hang " + realOrderId;
        String redirectUrl  = baseReturnUrl + contextPath + "/payment?action=momo-return";
        String ipnUrl       = redirectUrl;

        String rawSignature = "accessKey="   + MOMO_ACCESS_KEY
                + "&amount="      + totalAmount
                + "&extraData="
                + "&ipnUrl="      + ipnUrl
                + "&orderId="     + momoOrderId
                + "&orderInfo="   + orderInfo
                + "&partnerCode=" + MOMO_PARTNER_CODE
                + "&redirectUrl=" + redirectUrl
                + "&requestId="   + requestId
                + "&requestType=payWithMethod";

        String signature = hmacSHA256(rawSignature, MOMO_SECRET_KEY);

        // Lưu bản ghi Payment trạng thái "Chưa thanh toán", kèm momoOrderId/requestId để đối soát ở bước callback
        Payment payment = new Payment(realOrderId, "MOMO", totalAmount);
        payment.setMomoOrderId(momoOrderId);
        payment.setRequestId(requestId);
        paymentDAO.insertPayment(payment);

        String body = "{"
                + "\"partnerCode\":\""  + MOMO_PARTNER_CODE + "\","
                + "\"accessKey\":\""    + MOMO_ACCESS_KEY   + "\","
                + "\"requestId\":\""    + requestId         + "\","
                + "\"amount\":"         + totalAmount       + ","
                + "\"orderId\":\""      + momoOrderId       + "\","
                + "\"orderInfo\":\""    + orderInfo         + "\","
                + "\"redirectUrl\":\""  + redirectUrl       + "\","
                + "\"ipnUrl\":\""       + ipnUrl            + "\","
                + "\"extraData\":\"\","
                + "\"requestType\":\"payWithMethod\","
                + "\"lang\":\"vi\","
                + "\"signature\":\""    + signature         + "\""
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MOMO_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String resp = httpResponse.body();

        String key = "\"payUrl\":\"";
        int start = resp.indexOf(key);
        if (start == -1) {
            throw new Exception("MoMo không trả về payUrl. Response: " + resp);
        }
        start += key.length();
        int end = resp.indexOf("\"", start);
        return resp.substring(start, end);
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        Formatter formatter = new Formatter();
        for (byte b : hash) formatter.format("%02x", b);
        return formatter.toString();
    }

    /**
     * Hủy đơn hàng tạm thời nếu người dùng hủy thanh toán MoMo hoặc giao dịch lỗi
     */
    public boolean cancelPendingOrder(String orderId) {
        if (orderId == null) return false;
        boolean updated = orderDAO.updateOrderStatus(orderId, "Đã hủy");
        if (updated) {
            paymentDAO.updatePaymentStatus(orderId, "Đã hủy", false);
        }
        return updated;
    }
}