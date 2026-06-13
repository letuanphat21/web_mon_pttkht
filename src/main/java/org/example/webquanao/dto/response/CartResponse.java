package org.example.webquanao.dto.response;

public class CartResponse {
    private String message;
    private int totalCount;

    public CartResponse() {}

    public CartResponse(String message, int totalCount) {
        this.message = message;
        this.totalCount = totalCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}