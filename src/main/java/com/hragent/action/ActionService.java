package com.hragent.action;

import com.hragent.data.SQLiteDataStore;
import com.hragent.domain.Employee;
import com.hragent.llm.CommandIntent;
import com.hragent.llm.LLMService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ActionService {

    private final LLMService llmService;
    private final HRFunctions hrFunctions;
    private final SQLiteDataStore dataStore;

    public ActionService(LLMService llmService, HRFunctions hrFunctions, SQLiteDataStore dataStore) {
        this.llmService = llmService;
        this.hrFunctions = hrFunctions;
        this.dataStore = dataStore;
    }

    public String execute(String command) {
        try {
            CommandIntent intent = llmService.extractIntent(command);

            if (intent == null || intent.getIntent() == null) {
                return "Maaf, saya tidak bisa memahami perintah tersebut.";
            }

            switch (intent.getIntent()) {
                case "apply_leave":
                    return executeApplyLeave(intent);
                case "schedule_review":
                    return executeScheduleReview(intent);
                case "check_status":
                    return executeCheckStatus(intent);
                // Add other intent handlers as needed
                default:
                    return "Intent '" + intent.getIntent() + "' belum didukung.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String executeApplyLeave(CommandIntent intent) {
        try {
            String employeeName = resolveEmployeeName(intent.getEmployeeName());
            if (employeeName == null) return "Nama karyawan tidak valid.";

            String leaveType = intent.getLeaveType() != null ? intent.getLeaveType() : "Tahunan";

            LocalDate startDate = parseDate(intent.getStartDate());
            LocalDate endDate = parseDate(intent.getEndDate());

            if (startDate == null) return "Tanggal mulai tidak valid.";
            if (endDate == null) endDate = startDate;
            if (endDate.isBefore(startDate)) return "Tanggal selesai tidak boleh sebelum mulai.";

            Employee emp = dataStore.getEmployeeByName(employeeName);
            if (emp == null) return "Employee not found.";

            int currentSisa = dataStore.getLeaveBalance(emp.getId(), leaveType);
            int cutiDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

            if (cutiDays > currentSisa)
                return "❌ Saldo cuti tidak cukup. Tersisa " + currentSisa + " hari, diminta " + cutiDays + " hari.";

            dataStore.updateLeaveBalance(emp.getId(), leaveType, currentSisa - cutiDays);

            // Insert leave request
            String requestId = UUID.randomUUID().toString();
            dataStore.insertLeaveRequest(requestId, emp.getId(), leaveType, startDate.toString(), endDate.toString(), "Disetujui");

            return hrFunctions.applyForLeave(employeeName, leaveType, startDate, endDate) +
                    "\n💡 Sisa cuti " + leaveType + " sekarang: " + (currentSisa - cutiDays) + " hari.";
        } catch (Exception e) {
            return "Error in apply leave: " + e.getMessage();
        }
    }

    private String executeScheduleReview(CommandIntent intent) {
        try {
            String employeeName = resolveEmployeeName(intent.getEmployeeName());
            String reviewerName = resolveEmployeeName(intent.getReviewerName());
            if (employeeName == null) return "Nama karyawan tidak valid.";
            if (reviewerName == null) return "Nama reviewer tidak valid.";

            LocalDate reviewDate = parseDate(intent.getStartDate());
            if (reviewDate == null) return "Tanggal review tidak valid.";

            Employee emp = dataStore.getEmployeeByName(employeeName);
            Employee reviewer = dataStore.getEmployeeByName(reviewerName);
            if (emp == null || reviewer == null) return "Karyawan atau reviewer tidak ditemukan.";

            String reviewId = UUID.randomUUID().toString();
            dataStore.insertPerformanceReview(reviewId, emp.getId(), reviewer.getId(), reviewDate.toString(), 0, "Dijadwalkan");

            return hrFunctions.schedulePerformanceReview(employeeName, reviewerName, reviewDate) + "\n🗓 Jadwal review tercatat.";
        } catch (Exception e) {
            return "Error in schedule review: " + e.getMessage();
        }
    }

    private String executeCheckStatus(CommandIntent intent) {
        try {
            String employeeName = resolveEmployeeName(intent.getEmployeeName());
            if (employeeName == null) return "Nama karyawan tidak valid.";

            Employee emp = dataStore.getEmployeeByName(employeeName);
            if (emp == null) return "Karyawan tidak ditemukan.";

            String status = dataStore.getLatestLeaveRequestStatus(emp.getId());
            return "ℹ️ Status pengajuan cuti terakhir untuk " + employeeName + " adalah: " + status;
        } catch (Exception e) {
            return "Error in check status: " + e.getMessage();
        }
    }

    private String resolveEmployeeName(String name) {
        try {
            Employee emp = dataStore.getEmployeeByName(name);
            return emp != null ? emp.getNama() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
