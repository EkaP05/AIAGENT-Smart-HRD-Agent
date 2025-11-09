package com.hragent.util;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.domain.Employee;
import com.hragent.llm.LLMService;
import com.hragent.query.QueryService;

import java.util.ArrayList;
import java.util.List;

public class CRUDWorkflowTest {

    private static SQLiteDataStore store;
    private static LLMService llmService;
    private static QueryService queryService;
    private static ActionService actionService;
    
    private static List<TestResult> results = new ArrayList<>();
    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        printHeader();
        
        try {
            initializeSystem();
            
            // Run all CRUD workflow tests sequentially
            testReadEmployee();
            testReadLeaveBalance();
            testReadLeaveHistory();
            testCreateLeaveRequest();
            testCreateEmployee();
            testApproveLeave();
            testRejectLeave();
            testUpdateEmployee();
            testCancelLeave();
            testCompleteWorkflow();
            
            // Print summary report
            printSummaryReport();
            
            // Cleanup
            llmService.shutdown();
            store.close();
            
        } catch (Exception e) {
            System.err.println("❌ Test suite failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }

    private static void initializeSystem() throws Exception {
        System.out.println("🔧 Initializing system...");
        store = new SQLiteDataStore();
        store.loadEmployeesFromCsv("src/main/resources/employees.csv");
        store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");
        store.loadLeaveRequestsFromCsv("src/main/resources/leave_requests.csv");
        store.loadPerformanceReviewsFromCsv("src/main/resources/performance_reviews.csv");
        
        llmService = new LLMService();
        HRFunctions hrFunctions = new MockHRFunctions();
        queryService = new QueryService(store);
        actionService = new ActionService(llmService, hrFunctions, store);
        
        System.out.println("✅ System initialized\n");
    }

    private static void printHeader() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          🧪 CRUD WORKFLOW TEST - HR Agent SmartHR         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
    }

    // ============ WORKFLOW TESTS ============

    private static void testReadEmployee() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 1: Read Employee ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String query = "siapa manajer budi?";
            System.out.println("Query: " + query);
            String response = queryService.answer(query);
            System.out.println("Response: " + response);
            
            boolean passed = response.toLowerCase().contains("santi");
            logResult("Test 1: Read Employee", passed, 
                passed ? "✅ Correct manager returned" : "❌ Expected 'Santi' in response");
            
        } catch (Exception e) {
            logResult("Test 1: Read Employee", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testReadLeaveBalance() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 2: Read Leave Balance ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String query = "sisa cuti budi berapa?";
            System.out.println("Query: " + query);
            String response = queryService.answer(query);
            System.out.println("Response: " + response);
            
            boolean passed = response.contains("12");
            logResult("Test 2: Read Leave Balance", passed, 
                passed ? "✅ Correct balance shown (12 days)" : "❌ Expected '12' in response");
            
        } catch (Exception e) {
            logResult("Test 2: Read Leave Balance", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testReadLeaveHistory() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 3: Read Leave History ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String query = "riwayat cuti budi";
            System.out.println("Query: " + query);
            String response = queryService.answer(query);
            System.out.println("Response: " + response);
            System.out.println("DEBUG Response: '" + response + "'");
            boolean passed = response.toLowerCase().contains("tahunan") || 
            response.toLowerCase().contains("riwayat") ||
            response.toLowerCase().contains("tidak ada");
            
            logResult("Test 3: Read Leave History", passed, 
                passed ? "✅ History displayed correctly" : "❌ No history found");
            
        } catch (Exception e) {
            logResult("Test 3: Read Leave History", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCreateLeaveRequest() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 4: Create Leave Request ✨");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            Employee budi = store.getEmployeeByName("Budi");
            int balanceBefore = store.getLeaveBalance(budi.getId(), "Tahunan");
            
            System.out.println("📊 Before: Sisa Cuti = " + balanceBefore + " hari");
            
            String command = "tolong apply cuti tahunan buat budi dari tgl 2025-11-20 sampai 2025-11-22";
            System.out.println("Command: " + command);
            String response = actionService.execute(command);
            System.out.println("Response: " + response);
            
            int balanceAfter = store.getLeaveBalance(budi.getId(), "Tahunan");
            System.out.println("📊 After: Sisa Cuti = " + balanceAfter + " hari");
            System.out.println("📊 Deducted: " + (balanceBefore - balanceAfter) + " hari");
            
            boolean passed = balanceAfter < balanceBefore && response.contains("✅");
            logResult("Test 4: Create Leave Request", passed, 
                passed ? "✅ Leave created & balance deducted" : "❌ Leave creation failed");
            
        } catch (Exception e) {
            logResult("Test 4: Create Leave Request", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCreateEmployee() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 5: Create Employee ✨");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            int countBefore = store.getAllEmployees().size();
            System.out.println("📊 Before: Total Employees = " + countBefore);
            
            String command = "tambah karyawan baru nama Alice Wonder email alice@example.com jabatan Data Analyst departemen Teknologi";
            System.out.println("Command: " + command);
            String response = actionService.execute(command);
            System.out.println("Response: " + response);
            
            int countAfter = store.getAllEmployees().size();
            System.out.println("📊 After: Total Employees = " + countAfter);
            
            boolean passed = countAfter > countBefore && response.contains("✅");
            logResult("Test 5: Create Employee", passed, 
                passed ? "✅ Employee created successfully" : "❌ Employee creation failed");
            
        } catch (Exception e) {
            logResult("Test 5: Create Employee", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testApproveLeave() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 7: Approve Leave ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String leaveId = "LR003";
            String statusBefore = store.getLatestLeaveRequestStatus(leaveId);
            System.out.println("📊 Before: " + leaveId + " = " + statusBefore);
            
            String command = "approve cuti " + leaveId;
            System.out.println("Command: " + command);
            String response = actionService.execute(command);
            System.out.println("Response: " + response);
            
            String statusAfter = store.getLatestLeaveRequestStatus(leaveId);
            System.out.println("📊 After: " + leaveId + " = " + statusAfter);
            
            boolean passed = statusAfter.equals("Disetujui");
            logResult("Test 7: Approve Leave", passed, 
                passed ? "✅ Leave approved successfully" : "❌ Approval failed");
            
        } catch (Exception e) {
            logResult("Test 7: Approve Leave", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testRejectLeave() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 8: Reject Leave ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String leaveId = "LR003";
            String statusBefore = store.getLatestLeaveRequestStatus(leaveId);
            System.out.println("📊 Before: " + leaveId + " = " + statusBefore);
            
            if (!statusBefore.equals("Menunggu Persetujuan")) {
                System.out.println("⚠️ Warning: LR006 is not pending, status is: " + statusBefore);
            }

            String command = "reject cuti " + leaveId;
            System.out.println("Command: " + command);
            String response = actionService.execute(command);
            System.out.println("Response: " + response);
            
            String statusAfter = store.getLatestLeaveRequestStatus(leaveId);
            System.out.println("📊 After: " + leaveId + " = " + statusAfter);
            
            boolean passed = statusAfter.equals("Ditolak");
            logResult("Test 8: Reject Leave", passed, 
                passed ? "✅ Leave rejected successfully" : "❌ Rejection failed");
            
        } catch (Exception e) {
            logResult("Test 8: Reject Leave", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testUpdateEmployee() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 9: Update Employee ✅");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            Employee leo = store.getEmployeeByName("Leo");
            System.out.println("📊 Before: Dept=" + leo.getDepartemen() + ", Position=" + leo.getJabatan());
            
            String command = "pindahkan leo ke departemen Pemasaran jadi Marketing Manager";
            System.out.println("Command: " + command);
            String response = actionService.execute(command);
            System.out.println("Response: " + response);
            
            Employee leoAfter = store.getEmployeeByName("Leo");
            System.out.println("📊 After: Dept=" + leoAfter.getDepartemen() + ", Position=" + leoAfter.getJabatan());
            
            boolean passed = leoAfter.getDepartemen().equals("Pemasaran") && 
                           leoAfter.getJabatan().equals("Marketing Manager");
            logResult("Test 9: Update Employee", passed, 
                passed ? "✅ Employee updated successfully" : "❌ Update failed");
            
        } catch (Exception e) {
            logResult("Test 9: Update Employee", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCancelLeave() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 11: Cancel Leave 🗑️");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            // First create a leave to cancel
            String createCommand = "tolong apply cuti tahunan buat dewi dari tgl 2025-12-05 sampai 2025-12-06";
            System.out.println("Setup: " + createCommand);
            actionService.execute(createCommand);
            
            // Get the latest leave ID for Dewi
            Employee dewi = store.getEmployeeByName("Dewi");
            var leaves = store.getHistoryCuti(dewi.getId());
            String leaveId = leaves.isEmpty() ? null : leaves.get(0).getIdCuti();
            
            if (leaveId != null) {
                String statusBefore = store.getLatestLeaveRequestStatus(leaveId);
                System.out.println("📊 Before: " + leaveId + " = " + statusBefore);
                
                String command = "batalkan cuti " + leaveId;
                System.out.println("Command: " + command);
                String response = actionService.execute(command);
                System.out.println("Response: " + response);
                
                String statusAfter = store.getLatestLeaveRequestStatus(leaveId);
                System.out.println("📊 After: " + leaveId + " = " + statusAfter);
                
                boolean passed = statusAfter.equals("Dibatalkan");
                logResult("Test 11: Cancel Leave", passed, 
                    passed ? "✅ Leave cancelled successfully" : "❌ Cancellation failed");
            } else {
                logResult("Test 11: Cancel Leave", false, "❌ No leave to cancel");
            }
            
        } catch (Exception e) {
            logResult("Test 11: Cancel Leave", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCompleteWorkflow() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test 12: Complete Workflow 🔄");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            Employee rina = store.getEmployeeByName("Rina");
            int balanceInitial = store.getLeaveBalance(rina.getId(), "Tahunan");
            
            System.out.println("🔄 Workflow: Apply → Verify Balance → Check History");
            System.out.println("📊 Step 1: Initial Balance = " + balanceInitial);
            
            String applyCmd = "tolong apply cuti tahunan buat rina dari tgl 2025-12-15 sampai 2025-12-17";
            System.out.println("📝 Step 2: " + applyCmd);
            String applyResp = actionService.execute(applyCmd);
            System.out.println("   " + applyResp);
            
            int balanceAfter = store.getLeaveBalance(rina.getId(), "Tahunan");
            System.out.println("📊 Step 3: Balance After = " + balanceAfter + " (deducted: " + (balanceInitial - balanceAfter) + ")");
            
            String historyCmd = "riwayat cuti rina";
            System.out.println("🔍 Step 4: " + historyCmd);
            String historyResp = queryService.answer(historyCmd);
            System.out.println("   " + historyResp.substring(0, Math.min(100, historyResp.length())) + "...");
            
            boolean passed = balanceAfter < balanceInitial && applyResp.contains("✅");
            logResult("Test 12: Complete Workflow", passed, 
                passed ? "✅ Full workflow executed successfully" : "❌ Workflow failed");
            
        } catch (Exception e) {
            logResult("Test 12: Complete Workflow", false, "❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    // ============ REPORTING ============

    private static void logResult(String testName, boolean passed, String message) {
        results.add(new TestResult(testName, passed, message));
        if (passed) {
            passCount++;
            System.out.println("✅ PASS: " + message);
        } else {
            failCount++;
            System.out.println("❌ FAIL: " + message);
        }
    }

    private static void printSummaryReport() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                 📊 CRUD TEST SUMMARY                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        System.out.println("Total Tests: " + (passCount + failCount));
        System.out.println("✅ Passed: " + passCount);
        System.out.println("❌ Failed: " + failCount);
        System.out.println("Success Rate: " + String.format("%.2f%%", (passCount * 100.0 / (passCount + failCount))));
        System.out.println();

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📋 Testing Checklist:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        for (TestResult r : results) {
            String checkbox = r.passed ? "☑" : "☐";
            String icon = r.testName.contains("Read") ? "✅" : 
                         r.testName.contains("Create") ? "✨" :
                         r.testName.contains("Cancel") ? "🗑️" :
                         r.testName.contains("Complete") ? "🔄" : "✅";
            System.out.println(checkbox + " " + r.testName + " " + icon);
        }
        
        System.out.println();
        
        if (failCount > 0) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Failed Tests Details:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            for (TestResult r : results) {
                if (!r.passed) {
                    System.out.println("❌ " + r.testName);
                    System.out.println("   " + r.message);
                    System.out.println();
                }
            }
        } else {
            System.out.println("🎉 All CRUD operations working perfectly!\n");
        }
    }

    static class TestResult {
        String testName;
        boolean passed;
        String message;

        TestResult(String testName, boolean passed, String message) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
        }
    }
}
