package org.example.webquanao.action;

import java.util.Map;

public class Result {
    private boolean success;
    private String message;
    private Map<String, Object> data;

    public Result(boolean success, String message, Map<String, Object> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Result ok(String message, Map<String, Object> data) {
        return new Result(true, message, data);
    }

    public static Result fail(String message) {
        return new Result(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }
}