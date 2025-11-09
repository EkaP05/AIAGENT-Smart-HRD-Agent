package com.hragent.data;

import com.hragent.domain.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// CsvLoader: Membaca semua CSV files dan populate DataStore

public class CsvLoader {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("[yyyy-MM-dd][M/d/yyyy][d/M/yyyy]", Locale.US);

    public static DataStore loadAllData() {
        DataStore store = new DataStore();
        
        try {
            loadEmployees(store);
            loadLeaveBalances(store);
            loadLeaveRequests(store);
            loadPerformanceReviews(store);
            
            System.out.println("✅ Data loaded successfully:");
            System.out.println("   - Employees: " + store.getTotalEmployees());
            
        } catch (Exception e) {
            System.err.println("❌ Error loading CSV data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return store;
    }

    private static void loadEmployees(DataStore store) throws Exception {
        try (CSVReader reader = createReader("/employees.csv")) {
            List<String[]> rows = reader.readAll();
            rows.remove(0); // Remove header
            
            for (String[] row : rows) {
                int id = Integer.parseInt(row[0]);
                String nama = row[1];
                String email = row[2];
                String jabatan = row[3];
                String departemen = row[4];
                Integer idManajer = row[5].isEmpty() ? null : Integer.parseInt(row[5]);
                LocalDate tanggalBergabung = LocalDate.parse(row[6], DATE_FORMATTER);
                String statusKaryawan = row[7];
                
                Employee emp = new Employee(id, nama, email, jabatan, departemen, 
                                           idManajer, tanggalBergabung, statusKaryawan);
                store.addEmployee(emp);
            }
        }
    }

    private static void loadLeaveBalances(DataStore store) throws Exception {
        try (CSVReader reader = createReader("/leave_balances.csv")) {
            List<String[]> rows = reader.readAll();
            rows.remove(0);
            
            for (String[] row : rows) {
                int idKaryawan = Integer.parseInt(row[0]);
                String tipeCuti = row[1];
                int sisaHari = Integer.parseInt(row[2]);
                
                // Parse sisaCuti berdasarkan tipe cuti dari CSV
                int sisaCutiTahunan = tipeCuti.equalsIgnoreCase("Tahunan") ? sisaHari : 0;
                int sisaCutiSakit = tipeCuti.equalsIgnoreCase("Sakit") ? sisaHari : 0;
                int sisaCutiMelahirkan = tipeCuti.equalsIgnoreCase("Cuti Melahirkan") ? sisaHari : 0;
                
                LeaveBalance lb = new LeaveBalance(idKaryawan, tipeCuti, sisaHari, 
                                                   sisaCutiTahunan, sisaCutiSakit, sisaCutiMelahirkan);
                store.addLeaveBalance(lb);
            }
        }
    }

    private static void loadLeaveRequests(DataStore store) throws Exception {
        try (CSVReader reader = createReader("/leave_requests.csv")) {
            List<String[]> rows = reader.readAll();
            rows.remove(0); 
            
            for (String[] row : rows) {
                String idRequest = row[0];
                int idKaryawan = Integer.parseInt(row[1]);
                String tipeCuti = row[2];
                LocalDate tanggalMulai = LocalDate.parse(row[3], DATE_FORMATTER);
                LocalDate tanggalSelesai = LocalDate.parse(row[4], DATE_FORMATTER);
                String statusRequest = row[5];
                
                // Variabel tambahan untuk LeaveRequest constructor
                String idCuti = idRequest; // idCuti sama dengan idRequest
                String jenisCuti = tipeCuti; // jenisCuti sama dengan tipeCuti
                String statusCuti = statusRequest; // statusCuti sama dengan statusRequest
                
                LeaveRequest lr = new LeaveRequest(idRequest, idKaryawan, tipeCuti,
                                                   tanggalMulai, tanggalSelesai, statusRequest, 
                                                   idCuti, jenisCuti, statusCuti);
                store.addLeaveRequest(lr);
            }
        }
    }

    private static void loadPerformanceReviews(DataStore store) throws Exception {
        try (CSVReader reader = createReader("/performance_reviews.csv")) {
            List<String[]> rows = reader.readAll();
            rows.remove(0); 
            
            for (String[] row : rows) {
                String idReview = row[0];
                int idKaryawan = Integer.parseInt(row[1]);
                int idReviewer = Integer.parseInt(row[2]);
                LocalDate tanggalReview = LocalDate.parse(row[3], DATE_FORMATTER);
                int skorPerforma = Integer.parseInt(row[4]);
                String statusReview = row[5];
                
                PerformanceReview pr = new PerformanceReview(idReview, idKaryawan, idReviewer,
                                                            tanggalReview, skorPerforma, statusReview);
                store.addPerformanceReview(pr);
            }
        }
    }

    private static CSVReader createReader(String resourcePath) {
        InputStream is = CsvLoader.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("File not found: " + resourcePath);
        }
        return new CSVReader(new InputStreamReader(is));
    }
}
