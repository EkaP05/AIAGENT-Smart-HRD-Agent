package com.hragent.util;

import com.hragent.data.*;
import com.hragent.domain.*;

public class TestDataLoader {
    public static void main(String[] args) {
        System.out.println("=== Testing CSV Loader ===\n");
        
        DataStore store = CsvLoader.loadAllData();
        
        System.out.println("\n=== Testing Queries ===");
        
        // Test 1: Cari employee by name
        Employee budi = store.getEmployeeByName("Budi Santoso");
        System.out.println("1. Employee: " + budi);
        
        // Test 2: Cari manajer
        if (budi != null) {
            Employee manager = store.getManagerOf(budi);
            System.out.println("2. Manajer Budi: " + (manager != null ? manager.getNama() : "Tidak ada"));
        }
        
        // Test 3: Cari leave balance
        if (budi != null) {
            LeaveBalance lb = store.getLeaveBalance(budi.getId(), "Tahunan");
            System.out.println("3. Sisa cuti tahunan Budi: " + (lb != null ? lb.getSisaHari() + " hari" : "Tidak ada"));
        }
        
        // Test 4: Cari latest leave request
        Employee rina = store.getEmployeeByName("Rina Wijaya");
        if (rina != null) {
            LeaveRequest lr = store.getLatestLeaveRequest(rina.getId());
            System.out.println("4. Status cuti terakhir Rina: " + (lr != null ? lr.getStatusRequest() : "Tidak ada"));
        }
        
        System.out.println("\n✅ All tests completed!");
    }
}
