package org.example.webquanao.dto.response;

/**
 * DTO chứa response từ MoMo API sau khi tạo link thanh toán
 * Các field quan trọng: resultCode=0 là thành công, payUrl để redirect user
 */
public class MomoPaymentResponse {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private long   amount;
    private long   responseTime;
    private String message;
    private int    resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;

    public MomoPaymentResponse() {}

    public boolean isSuccess() {
        return resultCode == 0 && payUrl != null && !payUrl.isEmpty();
    }

    // Parse thủ công từ JSON string (không dùng Gson)
    public static MomoPaymentResponse fromJson(String json) {
        MomoPaymentResponse r = new MomoPaymentResponse();
        r.resultCode = parseIntField(json, "resultCode");
        r.payUrl     = parseStrField(json, "payUrl");
        r.message    = parseStrField(json, "message");
        r.orderId    = parseStrField(json, "orderId");
        r.requestId  = parseStrField(json, "requestId");
        return r;
    }

    private static String parseStrField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    private static int parseIntField(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return -1;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return -1; }
    }

    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getResultCode() { return resultCode; }
    public void setResultCode(int resultCode) { this.resultCode = resultCode; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
}