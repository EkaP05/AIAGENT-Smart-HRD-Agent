package com.hragent.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommandIntent {
    
    @JsonProperty("intent")
    private String intent;
    
    @JsonProperty("employee_name")
    private String employeeName;
    
    @JsonProperty("leave_type")
    private String leaveType;
    
    @JsonProperty("start_date")
    private String startDate;
    
    @JsonProperty("end_date")
    private String endDate;
    
    @JsonProperty("reviewer_name")
    private String reviewerName;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("amount")
    private Double amount;
    
    // New fields for 16 intents
    @JsonProperty("department")
    private String department;
    
    @JsonProperty("position")
    private String position;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("leave_id")
    private String leaveId;
    
    @JsonProperty("review_id")
    private String reviewId;
    
    @JsonProperty("score")
    private Integer score;
    
    @JsonProperty("new_balance")
    private Integer newBalance;

    // Getters and Setters
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }
    
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public Integer getNewBalance() { return newBalance; }
    public void setNewBalance(Integer newBalance) { this.newBalance = newBalance; }
}
