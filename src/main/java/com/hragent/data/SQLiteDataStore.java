package com.hragent.data;

import java.sql.*;
import java.io.*;
import com.opencsv.CSVReader;
import com.hragent.domain.Employee;
import com.hragent.domain.LeaveBalance;
import com.hragent.domain.LeaveRequest;
import com.hragent.domain.PerformanceReview;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDataStore {

    private Connection conn;

    public SQLiteDataStore() throws SQLException {
        System.out.println("Loading Agent SmartHR...");
        System.out.println("Loading CSV data into SQLite database...");
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        createTables();
    }

    private void createTables() throws SQLException {
        String sqlEmp = """
            CREATE TABLE employees (
                id INTEGER PRIMARY KEY, 
                nama TEXT, 
                email TEXT, 
                jabatan TEXT, 
                departemen TEXT, 
                id_manajer INTEGER, 
                tanggal_bergabung TEXT, 
                status_karyawan TEXT
            )
        """;

        String sqlLeave = """
            CREATE TABLE leave_balances (
                id_karyawan INTEGER, 
                tipe_cuti TEXT, 
                sisa_hari INTEGER,
                PRIMARY KEY (id_karyawan, tipe_cuti)
            )
        """;

        String sqlLeaveReq = """
            CREATE TABLE leave_requests (
                id_request TEXT PRIMARY KEY, 
                id_karyawan INTEGER, 
                tipe_cuti TEXT, 
                tanggal_mulai TEXT, 
                tanggal_selesai TEXT, 
                status_request TEXT
            )
        """;

        String sqlReviews = """
            CREATE TABLE performance_reviews (
                id_review TEXT PRIMARY KEY, 
                id_karyawan INTEGER, 
                id_reviewer INTEGER, 
                tanggal_review TEXT, 
                skor_performa INTEGER, 
                status_review TEXT
            )
        """;

        String sqlExpenses = """
            CREATE TABLE expenses (
                id_expense TEXT PRIMARY KEY,
                id_karyawan INTEGER,
                kategori TEXT,
                jumlah REAL,
                status TEXT
            )
        """;

        conn.createStatement().execute(sqlEmp);
        conn.createStatement().execute(sqlLeave);
        conn.createStatement().execute(sqlLeaveReq);
        conn.createStatement().execute(sqlReviews);
        conn.createStatement().execute(sqlExpenses);
    }

    public void loadEmployeesFromCsv(String path) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            reader.skip(1);
            String[] line;
            String sql = "INSERT INTO employees (id, nama, email, jabatan, departemen, id_manajer, tanggal_bergabung, status_karyawan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readNext()) != null) {
                ps.setInt(1, Integer.parseInt(line[0]));
                ps.setString(2, line[1]);
                ps.setString(3, line[2]);
                ps.setString(4, line[3]);
                ps.setString(5, line[4]);
                if (line[5].isEmpty()) ps.setNull(6, Types.INTEGER);
                else ps.setInt(6, Integer.parseInt(line[5]));
                ps.setString(7, line[6]);
                ps.setString(8, line[7]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("✅ Employees loaded.");
        }
    }

    public void loadLeaveBalancesFromCsv(String path) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            reader.skip(1);
            String[] line;
            String sql = "INSERT INTO leave_balances (id_karyawan, tipe_cuti, sisa_hari) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readNext()) != null) {
                ps.setInt(1, Integer.parseInt(line[0]));
                ps.setString(2, line[1]);
                ps.setInt(3, Integer.parseInt(line[2]));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("✅ Leave balances loaded.");
        }
    }

    public Employee getEmployeeByName(String name) throws SQLException {
        String sql = "SELECT * FROM employees WHERE LOWER(nama) LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + name.toLowerCase() + "%");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Employee(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("email"),
                rs.getString("jabatan"),
                rs.getString("departemen"),
                rs.getObject("id_manajer") != null ? rs.getInt("id_manajer") : null,
                LocalDate.parse(rs.getString("tanggal_bergabung")),
                rs.getString("status_karyawan")
            );
        }
        return null;
    }

    public int getLeaveBalance(int idKaryawan, String tipeCuti) throws SQLException {
        String sql = "SELECT sisa_hari FROM leave_balances WHERE id_karyawan = ? AND tipe_cuti = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idKaryawan);
        ps.setString(2, tipeCuti);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("sisa_hari");
        }
        return 0;
    }

    public void updateLeaveBalance(int idKaryawan, String tipeCuti, int newSisaHari) throws SQLException {
        String sql = "UPDATE leave_balances SET sisa_hari = ? WHERE id_karyawan = ? AND tipe_cuti = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, newSisaHari);
        ps.setInt(2, idKaryawan);
        ps.setString(3, tipeCuti);
        ps.executeUpdate();
    }

    public void insertLeaveRequest(String idRequest, int idKaryawan, String tipeCuti, String mulai, String selesai, String status) throws SQLException {
        String sql = "INSERT INTO leave_requests (id_request, id_karyawan, tipe_cuti, tanggal_mulai, tanggal_selesai, status_request) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRequest);
        ps.setInt(2, idKaryawan);
        ps.setString(3, tipeCuti);
        ps.setString(4, mulai);
        ps.setString(5, selesai);
        ps.setString(6, status);
        ps.executeUpdate();
    }

    public String getLatestLeaveRequestStatus(int idKaryawan) throws SQLException {
        String sql = "SELECT status_request FROM leave_requests WHERE id_karyawan = ? ORDER BY tanggal_mulai DESC LIMIT 1";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idKaryawan);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("status_request");
        }
        return "Not found";
    }

    public void insertPerformanceReview(String idReview, int empId, int reviewerId, String tanggalReview, int skor, String status) throws SQLException {
        String sql = "INSERT INTO performance_reviews (id_review, id_karyawan, id_reviewer, tanggal_review, skor_performa, status_review) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idReview);
            ps.setInt(2, empId);
            ps.setInt(3, reviewerId);
            ps.setString(4, tanggalReview);
            ps.setInt(5, skor);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }

    public void insertExpense(String idExpense, int idKaryawan, String kategori, double jumlah, String status) throws SQLException {
        String sql = "INSERT INTO expenses (id_expense, id_karyawan, kategori, jumlah, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idExpense);
            ps.setInt(2, idKaryawan);
            ps.setString(3, kategori);
            ps.setDouble(4, jumlah);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }

    public String getLatestExpenseStatus(int idKaryawan) throws SQLException {
        String sql = "SELECT status FROM expenses WHERE id_karyawan = ? ORDER BY id_expense DESC LIMIT 1";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idKaryawan);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("status");
        }
        return null;
    }


    public void close() throws SQLException {
        System.out.println("🧹 Cleaning up SQLite DB...");
        conn.close();
    }

    // For extended functionalities:

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Employee(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("email"),
                    rs.getString("jabatan"),
                    rs.getString("departemen"),
                    rs.getObject("id_manajer") != null ? rs.getInt("id_manajer") : null,
                    LocalDate.parse(rs.getString("tanggal_bergabung")),
                    rs.getString("status_karyawan")
            ));
        }
        return list;
    }

    public Employee getManagerOf(Employee emp) throws SQLException {
        if (emp.getIdManajer() == null) return null;
        String sql = "SELECT * FROM employees WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, emp.getIdManajer());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Employee(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("email"),
                    rs.getString("jabatan"),
                    rs.getString("departemen"),
                    rs.getObject("id_manajer") != null ? rs.getInt("id_manajer") : null,
                    LocalDate.parse(rs.getString("tanggal_bergabung")),
                    rs.getString("status_karyawan")
            );
        }
        return null;
    }

        // ============ EMPLOYEE MANAGEMENT ============

    public void updateDataKaryawan(int idKaryawan, String newDepartemen, String newJabatan) throws SQLException {
        String sql = "UPDATE employees SET departemen = ?, jabatan = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newDepartemen);
            ps.setString(2, newJabatan);
            ps.setInt(3, idKaryawan);
            ps.executeUpdate();
        }
    }

    public void tambahKaryawan(int id, String nama, String email, String jabatan, String departemen, 
                            Integer idManajer, String tanggalBergabung, String statusKaryawan) throws SQLException {
        String sql = "INSERT INTO employees (id, nama, email, jabatan, departemen, id_manajer, tanggal_bergabung, status_karyawan) " + 
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nama);
            ps.setString(3, email);
            ps.setString(4, jabatan);
            ps.setString(5, departemen);
            if (idManajer == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, idManajer);
            ps.setString(7, tanggalBergabung);
            ps.setString(8, statusKaryawan);
            ps.executeUpdate();
        }
    }

    public void updateStatusKaryawan(int idKaryawan, String newStatus) throws SQLException {
        String sql = "UPDATE employees SET status_karyawan = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, idKaryawan);
            ps.executeUpdate();
        }
    }

    public List<Employee> getKaryawanByDepartemen(String departemen) throws SQLException {
        String sql = "SELECT * FROM employees WHERE LOWER(departemen) = LOWER(?)";
        List<Employee> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, departemen);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToEmployee(rs));
            }
        }
        return list;
    }

    public List<Employee> getKaryawanByJabatan(String jabatan) throws SQLException {
        String sql = "SELECT * FROM employees WHERE LOWER(jabatan) = LOWER(?)";
        List<Employee> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jabatan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToEmployee(rs));
            }
        }
        return list;
    }

    public List<Employee> getKaryawanByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM employees WHERE LOWER(status_karyawan) = LOWER(?)";
        List<Employee> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToEmployee(rs));
            }
        }
        return list;
    }

    private Employee mapToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("id"),
            rs.getString("nama"),
            rs.getString("email"),
            rs.getString("jabatan"),
            rs.getString("departemen"),
            rs.getObject("id_manajer") != null ? rs.getInt("id_manajer") : null,
            LocalDate.parse(rs.getString("tanggal_bergabung")),
            rs.getString("status_karyawan")
        );
    }

    // ============ LEAVE MANAGEMENT ============

    public List<LeaveRequest> getCutiPending() throws SQLException {
        String sql = "SELECT * FROM leave_requests WHERE status_cuti = 'Pending'";
        List<LeaveRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapToLeaveRequest(rs));
            }
        }
        return list;
    }

    public void approveRejectCuti(String idCuti, String newStatus) throws SQLException {
        String sql = "UPDATE leave_requests SET status_cuti = ? WHERE id_cuti = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, idCuti);
            ps.executeUpdate();
        }
    }

    public void batalkanCuti(String idCuti) throws SQLException {
        String sql = "UPDATE leave_requests SET status_cuti = 'Dibatalkan' WHERE id_cuti = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCuti);
            ps.executeUpdate();
        }
    }

    public void updateStatusCuti(String idCuti, String newStatus) throws SQLException {
        approveRejectCuti(idCuti, newStatus);
    }

    public List<LeaveBalance> getAllLeaveBalances() throws SQLException {
        String sql = "SELECT * FROM leave_balances";
        List<LeaveBalance> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idKaryawan = rs.getInt("id_karyawan");
                String tipeCuti = rs.getString("tipe_cuti");
                int sisaHari = rs.getInt("sisa_hari");
                
                // Assign values based on leave type
                int sisaCutiTahunan = tipeCuti.equalsIgnoreCase("Tahunan") ? sisaHari : 0;
                int sisaCutiSakit = tipeCuti.equalsIgnoreCase("Sakit") ? sisaHari : 0;
                int sisaCutiMelahirkan = tipeCuti.equalsIgnoreCase("Cuti Melahirkan") ? sisaHari : 0;
                
                list.add(new LeaveBalance(
                    idKaryawan,
                    tipeCuti,
                    sisaHari,
                    sisaCutiTahunan,
                    sisaCutiSakit,
                    sisaCutiMelahirkan
                ));
            }
        }
        return list;
    }

    public void updateSisaCuti(int idKaryawan, String leaveType, int newBalance) throws SQLException {
        String column = switch (leaveType) {
            case "Tahunan" -> "sisa_cuti_tahunan";
            case "Sakit" -> "sisa_cuti_sakit";
            case "Cuti Melahirkan" -> "sisa_cuti_melahirkan";
            default -> throw new IllegalArgumentException("Invalid leave type");
        };
        String sql = "UPDATE leave_balances SET " + column + " = ? WHERE id_karyawan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newBalance);
            ps.setInt(2, idKaryawan);
            ps.executeUpdate();
        }
    }

    public void resetCutiTahunan(int jumlahCutiDefault) throws SQLException {
        String sql = "UPDATE leave_balances SET sisa_cuti_tahunan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlahCutiDefault);
            ps.executeUpdate();
        }
    }

    public List<LeaveRequest> getHistoryCuti(int idKaryawan) throws SQLException {
        String sql = "SELECT * FROM leave_requests WHERE id_karyawan = ? ORDER BY tanggal_mulai DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKaryawan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToLeaveRequest(rs));
            }
        }
        return list;
    }

    private LeaveRequest mapToLeaveRequest(ResultSet rs) throws SQLException {
        String idRequest = rs.getString("id_request");
        int idKaryawan = rs.getInt("id_karyawan");
        String tipeCuti = rs.getString("tipe_cuti");
        LocalDate tanggalMulai = LocalDate.parse(rs.getString("tanggal_mulai"));
        LocalDate tanggalSelesai = LocalDate.parse(rs.getString("tanggal_selesai"));
        String statusRequest = rs.getString("status_request");
        
        // Additional fields for LeaveRequest constructor (9 params)
        String idCuti = idRequest; // idCuti same as idRequest
        String jenisCuti = tipeCuti; // jenisCuti same as tipeCuti
        String statusCuti = statusRequest; // statusCuti same as statusRequest
        
        return new LeaveRequest(
            idRequest,
            idKaryawan,
            tipeCuti,
            tanggalMulai,
            tanggalSelesai,
            statusRequest,
            idCuti,
            jenisCuti,
            statusCuti
        );
    }

    // ============ PERFORMANCE REVIEW MANAGEMENT ============

    public List<PerformanceReview> getReviewTerjadwal() throws SQLException {
        String sql = "SELECT * FROM performance_reviews WHERE status_review = 'Terjadwal' ORDER BY tanggal_review ASC";
        List<PerformanceReview> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapToPerformanceReview(rs));
            }
        }
        return list;
    }

    public void updateSkorReview(String idReview, int skorPerforma) throws SQLException {
        String sql = "UPDATE performance_reviews SET skor_performa = ?, status_review = 'Selesai' WHERE id_review = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skorPerforma);
            ps.setString(2, idReview);
            ps.executeUpdate();
        }
    }

    public void batalkanReview(String idReview) throws SQLException {
        String sql = "UPDATE performance_reviews SET status_review = 'Dibatalkan' WHERE id_review = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idReview);
            ps.executeUpdate();
        }
    }

    public void submitHasilReview(String idReview, int skorPerforma, String status) throws SQLException {
        String sql = "UPDATE performance_reviews SET skor_performa = ?, status_review = ? WHERE id_review = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skorPerforma);
            ps.setString(2, status);
            ps.setString(3, idReview);
            ps.executeUpdate();
        }
    }

    public List<PerformanceReview> getHistoryReview(int idKaryawan) throws SQLException {
        String sql = "SELECT * FROM performance_reviews WHERE id_karyawan = ? ORDER BY tanggal_review DESC";
        List<PerformanceReview> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKaryawan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToPerformanceReview(rs));
            }
        }
        return list;
    }

    private PerformanceReview mapToPerformanceReview(ResultSet rs) throws SQLException {
        return new PerformanceReview(
            rs.getString("id_review"),
            rs.getInt("id_karyawan"),
            rs.getInt("id_reviewer"),
            LocalDate.parse(rs.getString("tanggal_review")),
            rs.getInt("skor_performa"),
            rs.getString("status_review")
        );
    }

    public Employee getEmployeeById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToEmployee(rs);
            }
        }
        return null;
    }
    
    public String getLatestLeaveRequestStatus(String idCuti) throws SQLException {
        String sql = "SELECT status_cuti FROM leave_requests WHERE id_cuti = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCuti);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("status_cuti");
            }
        }
        return "Not found";
    }
    


}
