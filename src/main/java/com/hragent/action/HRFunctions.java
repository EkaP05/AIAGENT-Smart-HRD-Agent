package com.hragent.action;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// HRFunctions: Interface untuk mock HR operations
public interface HRFunctions {
    String applyForLeave(String employeeName, String leaveType, LocalDate startDate, LocalDate endDate);
    String schedulePerformanceReview(String employeeName, String reviewerName, LocalDate reviewDate);
    String checkLeaveRequestStatus(String employeeName);
    String submitExpenseReport(String employeeName, String category, double amount);
    String lookupColleagueInfo(String colleagueName);
}
