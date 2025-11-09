package com.hragent.query;

import com.hragent.data.SQLiteDataStore;
import com.hragent.domain.Employee;
import com.hragent.domain.LeaveBalance;
import com.hragent.domain.LeaveRequest;
import com.hragent.domain.PerformanceReview;

import java.sql.SQLException;
import java.util.List;

public class QueryService {

    private final SQLiteDataStore store;

    public QueryService(SQLiteDataStore store) {
        this.store = store;
    }

    public String answer(String question) {
        String q = question.toLowerCase().trim();

        try {
            if (q.contains("siapa") && (q.contains("manajer") || q.contains("manager"))) {
                return answerManagerQuery(q);
            }
            if ((q.contains("sisa") || q.contains("berapa")) && q.contains("cuti")) {
                return answerLeaveBalanceQuery(q);
            }
            if (q.contains("status") && q.contains("cuti")) {
                return answerLeaveStatusQuery(q);
            }
            if (q.contains("jabatan")) {
                return answerJobTitleQuery(q);
            }
            if (q.contains("email")) {
                return answerEmailQuery(q);
            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }

        return "Maaf, saya tidak tahu bagaimana menjawab pertanyaan ini.";
    }

    // ============ EMPLOYEE QUERIES ============

    public String answerListKaryawanDepartemen(String departemen) {
        try {
            List<Employee> list = store.getKaryawanByDepartemen(departemen);
            if (list.isEmpty()) return "Tidak ada karyawan di departemen " + departemen;
            
            StringBuilder result = new StringBuilder("Daftar karyawan di departemen " + departemen + ":\n");
            for (Employee emp : list) {
                result.append("- ").append(emp.getNama()).append(" (").append(emp.getJabatan()).append(")\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data karyawan: " + e.getMessage();
        }
    }

    public String answerListKaryawanJabatan(String jabatan) {
        try {
            List<Employee> list = store.getKaryawanByJabatan(jabatan);
            if (list.isEmpty()) return "Tidak ada karyawan dengan jabatan " + jabatan;
            
            StringBuilder result = new StringBuilder("Daftar karyawan dengan jabatan " + jabatan + ":\n");
            for (Employee emp : list) {
                result.append("- ").append(emp.getNama()).append(" (").append(emp.getDepartemen()).append(")\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data karyawan: " + e.getMessage();
        }
    }

    public String answerListKaryawanStatus(String status) {
        try {
            List<Employee> list = store.getKaryawanByStatus(status);
            if (list.isEmpty()) return "Tidak ada karyawan dengan status " + status;
            
            StringBuilder result = new StringBuilder("Daftar karyawan dengan status " + status + ":\n");
            for (Employee emp : list) {
                result.append("- ").append(emp.getNama()).append(" (").append(emp.getDepartemen()).append(" - ").append(emp.getJabatan()).append(")\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data karyawan: " + e.getMessage();
        }
    }

    // ============ LEAVE QUERIES ============

    public String answerCekStatusCuti(String idCuti) {
        try {
            String status = store.getLatestLeaveRequestStatus(idCuti);
            return "Status cuti dengan ID " + idCuti + ": " + status;
        } catch (SQLException e) {
            return "Gagal mengecek status cuti: " + e.getMessage();
        }
    }

    public String answerListCutiPending() {
        try {
            List<LeaveRequest> list = store.getCutiPending();
            if (list.isEmpty()) return "Tidak ada pengajuan cuti yang pending";
            
            StringBuilder result = new StringBuilder("Daftar cuti yang menunggu approval:\n");
            for (LeaveRequest req : list) {
                result.append("- ID: ").append(req.getIdCuti())
                    .append(", Karyawan ID: ").append(req.getIdKaryawan())
                    .append(", Jenis: ").append(req.getJenisCuti())
                    .append(", Tanggal: ").append(req.getTanggalMulai()).append(" s/d ").append(req.getTanggalSelesai())
                    .append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data cuti pending: " + e.getMessage();
        }
    }

    public String answerListSisaCutiSemua() {
        try {
            List<LeaveBalance> list = store.getAllLeaveBalances();
            if (list.isEmpty()) return "Tidak ada data sisa cuti";
            
            StringBuilder result = new StringBuilder("Daftar sisa cuti semua karyawan:\n");
            for (LeaveBalance lb : list) {
                Employee emp = store.getEmployeeById(lb.getIdKaryawan());
                String nama = emp != null ? emp.getNama() : "ID " + lb.getIdKaryawan();
                result.append("- ").append(nama)
                    .append(": Tahunan=").append(lb.getSisaCutiTahunan())
                    .append(", Sakit=").append(lb.getSisaCutiSakit())
                    .append(", Melahirkan=").append(lb.getSisaCutiMelahirkan())
                    .append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data sisa cuti: " + e.getMessage();
        }
    }

    public String answerHistoryCuti(String namaKaryawan) {
        try {
            String name = extractName(namaKaryawan);
            if (name == null) return "Tidak dapat mengenali nama karyawan";
            
            Employee emp = store.getEmployeeByName(name);
            if (emp == null) return "Karyawan tidak ditemukan";
            
            List<LeaveRequest> list = store.getHistoryCuti(emp.getId());
            if (list.isEmpty()) return "Tidak ada riwayat cuti untuk " + name;
            
            StringBuilder result = new StringBuilder("Riwayat cuti " + name + ":\n");
            for (LeaveRequest req : list) {
                result.append("- ").append(req.getJenisCuti())
                    .append(" (").append(req.getTanggalMulai()).append(" s/d ").append(req.getTanggalSelesai()).append(")")
                    .append(" - Status: ").append(req.getStatusCuti())
                    .append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil riwayat cuti: " + e.getMessage();
        }
    }

    // ============ PERFORMANCE REVIEW QUERIES ============

    public String answerListReviewTerjadwal() {
        try {
            List<PerformanceReview> list = store.getReviewTerjadwal();
            if (list.isEmpty()) return "Tidak ada review yang terjadwal";
            
            StringBuilder result = new StringBuilder("Daftar review yang terjadwal:\n");
            for (PerformanceReview pr : list) {
                Employee emp = store.getEmployeeById(pr.getIdKaryawan());
                Employee reviewer = store.getEmployeeById(pr.getIdReviewer());
                String namaKaryawan = emp != null ? emp.getNama() : "ID " + pr.getIdKaryawan();
                String namaReviewer = reviewer != null ? reviewer.getNama() : "ID " + pr.getIdReviewer();
                
                result.append("- ID: ").append(pr.getIdReview())
                    .append(", Karyawan: ").append(namaKaryawan)
                    .append(", Reviewer: ").append(namaReviewer)
                    .append(", Tanggal: ").append(pr.getTanggalReview())
                    .append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil data review terjadwal: " + e.getMessage();
        }
    }

    public String answerHistoryReview(String namaKaryawan) {
        try {
            String name = extractName(namaKaryawan);
            if (name == null) return "Tidak dapat mengenali nama karyawan";
            
            Employee emp = store.getEmployeeByName(name);
            if (emp == null) return "Karyawan tidak ditemukan";
            
            List<PerformanceReview> list = store.getHistoryReview(emp.getId());
            if (list.isEmpty()) return "Tidak ada riwayat review untuk " + name;
            
            StringBuilder result = new StringBuilder("Riwayat review " + name + ":\n");
            for (PerformanceReview pr : list) {
                result.append("- Tanggal: ").append(pr.getTanggalReview())
                    .append(", Skor: ").append(pr.getSkorPerforma())
                    .append(", Status: ").append(pr.getStatusReview())
                    .append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "Gagal mengambil riwayat review: " + e.getMessage();
        }
    }


        private String extractName(String question) {
            try {
                List<Employee> employees = store.getAllEmployees();
                String qLower = question.toLowerCase().replaceAll("[^a-z ]", " ");
            for (Employee emp : employees) {
                String namaFull = emp.getNama().toLowerCase();
                String[] namaTokens = namaFull.split(" ");
                for (String token : namaTokens) {
                    if (token.length() >= 3 && qLower.contains(token)) {
                        return emp.getNama();
                    }
                }

                if (qLower.contains(namaFull)) return emp.getNama();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    

    private String answerJobTitleQuery(String question) throws SQLException {
        String name = extractName(question);
        if (name == null) return "Saya tidak bisa mengenali nama karyawan dari pertanyaan tersebut.";
        Employee emp = store.getEmployeeByName(name);
        if (emp == null) return "Karyawan tidak ditemukan.";
        return "Jabatan " + emp.getNama() + " adalah " + emp.getJabatan() + ".";
    }
    
    private String answerEmailQuery(String question) throws SQLException {
        String name = extractName(question);
        if (name == null) return "Saya tidak bisa mengenali nama karyawan dari pertanyaan tersebut.";
        Employee emp = store.getEmployeeByName(name);
        if (emp == null) return "Karyawan tidak ditemukan.";
        return "Email " + emp.getNama() + " adalah " + emp.getEmail() + ".";
    }
    

    private String answerManagerQuery(String question) throws SQLException {
        String name = extractName(question);
        if (name == null) {
            return "Saya tidak bisa mengenali nama karyawan dari pertanyaan tersebut.";
        }
        Employee emp = store.getEmployeeByName(name);
        if (emp == null) {
            return "Karyawan dengan nama '" + name + "' tidak ditemukan.";
        }
        Employee manajer = store.getManagerOf(emp);
        if (manajer == null) {
            return emp.getNama() + " tidak memiliki manajer.";
        }
        return "Manajer " + emp.getNama() + " adalah " + manajer.getNama() +
                " (" + manajer.getJabatan() + ").";
    }

    private String answerLeaveBalanceQuery(String question) throws SQLException {
        String name = extractName(question);
        if (name == null) {
            return "Saya tidak bisa mengenali nama karyawan dari pertanyaan tersebut.";
        }
        Employee emp = store.getEmployeeByName(name);
        if (emp == null) {
            return "Karyawan dengan nama '" + name + "' tidak ditemukan.";
        }

        String leaveType = "Tahunan";
        if (question.contains("sakit")) leaveType = "Sakit";
        else if (question.contains("melahirkan")) leaveType = "Cuti Melahirkan";

        int sisaHari = store.getLeaveBalance(emp.getId(), leaveType);
        return "Sisa cuti " + leaveType.toLowerCase() + " " + emp.getNama() + " adalah " + sisaHari + " hari.";
    }

    private String answerLeaveStatusQuery(String question) throws SQLException {
        String name = extractName(question);
        if (name == null) return "Saya tidak bisa mengenali nama karyawan.";
        Employee emp = store.getEmployeeByName(name);
        if (emp == null) return "Karyawan tidak ditemukan.";
        String status = store.getLatestLeaveRequestStatus(emp.getId());
        return "Status cuti terakhir " + emp.getNama() + ": " + status;
    }
}