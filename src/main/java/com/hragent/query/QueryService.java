package com.hragent.query;

import com.hragent.data.SQLiteDataStore;
import com.hragent.domain.Employee;
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