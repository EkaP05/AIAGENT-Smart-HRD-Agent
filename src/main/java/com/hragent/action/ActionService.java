package com.hragent.action;

import com.hragent.data.SQLiteDataStore;
import com.hragent.domain.Employee;
import com.hragent.llm.CommandIntent;
import com.hragent.llm.LLMService;

import java.sql.SQLException;
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

    // ============ EMPLOYEE MANAGEMENT ACTIONS ============

    public String executeUpdateDataKaryawan(int idKaryawan, String newDepartemen, String newJabatan) {
        try {
            dataStore.updateDataKaryawan(idKaryawan, newDepartemen, newJabatan);
            return "✅ Data karyawan berhasil diperbarui. Departemen: " + newDepartemen + ", Jabatan: " + newJabatan;
        } catch (SQLException e) {
            return "❌ Gagal memperbarui data karyawan: " + e.getMessage();
        }
    }

    public String executeTambahKaryawan(int id, String nama, String email, String jabatan, String departemen, Integer idManajer, String tanggalBergabung) {
        try {
            dataStore.tambahKaryawan(id, nama, email, jabatan, departemen, idManajer, tanggalBergabung, "Aktif");
            return "✅ Karyawan baru " + nama + " berhasil ditambahkan dengan ID " + id;
        } catch (SQLException e) {
            return "❌ Gagal menambahkan karyawan baru: " + e.getMessage();
        }
    }

    public String executeUpdateStatusKaryawan(int idKaryawan, String newStatus) {
        try {
            dataStore.updateStatusKaryawan(idKaryawan, newStatus);
            return "✅ Status karyawan berhasil diperbarui menjadi: " + newStatus;
        } catch (SQLException e) {
            return "❌ Gagal memperbarui status karyawan: " + e.getMessage();
        }
    }

    // ============ LEAVE MANAGEMENT ACTIONS ============

    public String executeApproveRejectCuti(String idCuti, String status) {
        try {
            dataStore.approveRejectCuti(idCuti, status);
            return "✅ Cuti dengan ID " + idCuti + " telah " + status.toLowerCase();
        } catch (SQLException e) {
            return "❌ Gagal memperbarui status cuti: " + e.getMessage();
        }
    }

    public String executeBatalkanCuti(String idCuti) {
        try {
            dataStore.batalkanCuti(idCuti);
            return "✅ Cuti dengan ID " + idCuti + " telah dibatalkan";
        } catch (SQLException e) {
            return "❌ Gagal membatalkan cuti: " + e.getMessage();
        }
    }

    public String executeUpdateStatusCuti(String idCuti, String newStatus) {
        try {
            dataStore.updateStatusCuti(idCuti, newStatus);
            return "✅ Status cuti berhasil diperbarui menjadi: " + newStatus;
        } catch (SQLException e) {
            return "❌ Gagal memperbarui status cuti: " + e.getMessage();
        }
    }

    public String executeUpdateSisaCuti(int idKaryawan, String leaveType, int newBalance) {
        try {
            dataStore.updateSisaCuti(idKaryawan, leaveType, newBalance);
            return "✅ Sisa cuti " + leaveType + " berhasil diperbarui menjadi " + newBalance + " hari";
        } catch (SQLException e) {
            return "❌ Gagal memperbarui sisa cuti: " + e.getMessage();
        }
    }

    public String executeResetCutiTahunan(int jumlahDefault) {
        try {
            dataStore.resetCutiTahunan(jumlahDefault);
            return "✅ Cuti tahunan semua karyawan berhasil direset menjadi " + jumlahDefault + " hari";
        } catch (SQLException e) {
            return "❌ Gagal mereset cuti tahunan: " + e.getMessage();
        }
    }

    // ============ PERFORMANCE REVIEW ACTIONS ============

    public String executeUpdateSkorReview(String idReview, int skorPerforma) {
        try {
            dataStore.updateSkorReview(idReview, skorPerforma);
            return "✅ Skor performa untuk review " + idReview + " telah diperbarui menjadi " + skorPerforma;
        } catch (SQLException e) {
            return "❌ Gagal memperbarui skor review: " + e.getMessage();
        }
    }

    public String executeBatalkanReview(String idReview) {
        try {
            dataStore.batalkanReview(idReview);
            return "✅ Review dengan ID " + idReview + " telah dibatalkan";
        } catch (SQLException e) {
            return "❌ Gagal membatalkan review: " + e.getMessage();
        }
    }

    public String executeSubmitHasilReview(String idReview, int skorPerforma) {
        try {
            dataStore.submitHasilReview(idReview, skorPerforma, "Selesai");
            return "✅ Hasil review untuk " + idReview + " telah disubmit dengan skor " + skorPerforma;
        } catch (SQLException e) {
            return "❌ Gagal submit hasil review: " + e.getMessage();
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
