package org.example.webquanao.dto.request;

/**
 * DTO chứa thông tin gửi lên MoMo API để tạo link thanh toán
 * Theo tài liệu: https://developers.momo.vn/v3/docs/payment/api/payment-api
 */
public class MomoPaymentRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private long   amount;
    private String orderId;       // Mã đơn hàng phía MoMo (khác order_id trong DB)
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String extraData;
    private String requestType;
    private String lang;
    private String signature;

    public MomoPaymentRequest() {}

    public MomoPaymentRequest(String partnerCode, String accessKey, String requestId,
                              long amount, String orderId, String orderInfo,
                              String redirectUrl, String ipnUrl,
                              String requestType, String signature) {
        this.partnerCode = partnerCode;
        this.accessKey   = accessKey;
        this.requestId   = requestId;
        this.amount      = amount;
        this.orderId     = orderId;
        this.orderInfo   = orderInfo;
        this.redirectUrl = redirectUrl;
        this.ipnUrl      = ipnUrl;
        this.extraData   = "";
        this.requestType = requestType;
        this.lang        = "vi";
        this.signature   = signature;
    }

    // Chuyển sang JSON string để gọi API (không dùng Gson để tránh thêm dep)
    public String toJson() {
        return "{"
                + "\"partnerCode\":\""  + partnerCode + "\","
                + "\"accessKey\":\""    + accessKey   + "\","
                + "\"requestId\":\""    + requestId   + "\","
                + "\"amount\":"         + amount      + ","
                + "\"orderId\":\""      + orderId     + "\","
                + "\"orderInfo\":\""    + orderInfo   + "\","
                + "\"redirectUrl\":\""  + redirectUrl + "\","
                + "\"ipnUrl\":\""       + ipnUrl      + "\","
                + "\"extraData\":\""    + extraData   + "\","
                + "\"requestType\":\""  + requestType + "\","
                + "\"lang\":\""         + lang        + "\","
                + "\"signature\":\""    + signature   + "\""
                + "}";
    }

    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    public String getIpnUrl() { return ipnUrl; }
    public void setIpnUrl(String ipnUrl) { this.ipnUrl = ipnUrl; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}