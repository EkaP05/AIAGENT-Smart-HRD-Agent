package com.hragent.action;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


// Implementasi mock dari HRFunctions

public class MockHRFunctions implements HRFunctions {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));

    @Override
    public String applyForLeave(String employeeName, String leaveType, LocalDate startDate, LocalDate endDate) {
        return String.format(
            "✅ KONFIRMASI: Pengajuan cuti untuk %s (jenis: %s) dari tanggal %s hingga %s telah dicatat.",
            employeeName, leaveType, startDate.format(FORMATTER), endDate.format(FORMATTER)
        );
    }

    @Override
    public String schedulePerformanceReview(String employeeName, String reviewerName, LocalDate reviewDate) {
        DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        return String.format(
            "✅ KONFIRMASI: Sesi review performa untuk %s dengan %s telah dijadwalkan pada %s.",
            employeeName, reviewerName, reviewDate.format(longFormatter)
        );
    }

    @Override
    public String checkLeaveRequestStatus(String employeeName) {
        // Mock: random status untuk demo
        String[] statuses = {"Disetujui", "Ditolak", "Menunggu Persetujuan"};
        int randomIndex = (int) (Math.random() * statuses.length);
        return String.format(
            "ℹ️ INFO: Status pengajuan cuti terakhir untuk %s adalah: %s.",
            employeeName, statuses[randomIndex]
        );
    }
    
    @Override
    public String submitExpenseReport(String employeeName, String category, double amount) {
        return String.format(
            "✅ KONFIRMASI: Laporan pengeluaran untuk %s sebesar Rp%,.2f (kategori: %s) telah diajukan untuk diproses.",
            employeeName, amount, category
        );
    }

    @Override
    public String lookupColleagueInfo(String colleagueName) {
        return String.format(
            "ℹ️ INFO: Informasi untuk %s - Silakan gunakan query 'siapa' atau 'jabatan' untuk info lebih detail.",
            colleagueName
        );
    }
}
