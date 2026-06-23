    package org.example.webquanao.utils;

    import javax.crypto.Mac;
    import javax.crypto.spec.SecretKeySpec;
    import java.nio.charset.StandardCharsets;
    import java.util.Formatter;

    /**
     * Utility class — tạo chữ ký HMAC-SHA256 cho MoMo API
     */
    public class MomoEncoder {

        private MomoEncoder() {}

        public static String hmacSHA256(String data, String secretKey) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }

        public static String buildRawSignature(String accessKey, long amount,
                                               String extraData, String ipnUrl,
                                               String orderId, String orderInfo,
                                               String partnerCode, String redirectUrl,
                                               String requestId, String requestType) {
            return "accessKey="   + accessKey
                    + "&amount="     + amount
                    + "&extraData="  + extraData // <--- Đảm bảo biến này nhận chuỗi Base64 thay vì để trống
                    + "&ipnUrl="     + ipnUrl
                    + "&orderId="    + orderId   // <--- Nhận chuỗi ORD-XXXX độc nhất mỗi lượt bấm
                    + "&orderInfo="  + orderInfo
                    + "&partnerCode="+ partnerCode
                    + "&redirectUrl="+ redirectUrl
                    + "&requestId="  + requestId
                    + "&requestType="+ requestType;
        }
    }