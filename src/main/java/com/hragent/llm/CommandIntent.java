package com.hragent.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;


// CommandIntent: untuk hasil ekstraksi LLM

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

    // Constructors
    public CommandIntent() {}

    // Getters & Setters
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

    @Override
    public String toString() {
        return "CommandIntent{" +
                "intent='" + intent + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", leaveType='" + leaveType + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}
