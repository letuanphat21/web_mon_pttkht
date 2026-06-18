package org.example.webquanao.dto.request;

public class StatisticRequest {
    private String type;
    private String startDate;
    private String endDate;
    private String categoryId;

    public StatisticRequest() {
    }

    public StatisticRequest(String type, String startDate, String endDate, String categoryId) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
