package com.hragent.util;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.llm.LLMService;
import com.hragent.llm.CommandIntent;
import com.hragent.query.QueryService;

import java.util.ArrayList;
import java.util.List;

public class SystemTest {

    private static List<TestResult> integrationResults = new ArrayList<>();
    private static List<TestResult> intentResults = new ArrayList<>();
    
    private static int integrationPassCount = 0;
    private static int integrationFailCount = 0;
    private static int intentPassCount = 0;
    private static int intentFailCount = 0;
    
    private static boolean ollamaSuccess = false;
    private static boolean sqliteSuccess = false;
    
    private static int ollamaCallCount = 0;
    private static long totalOllamaDuration = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          🧪 SYSTEM TEST - HR Agent SmartHR                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // INTEGRATION TESTS
        testDatabaseConnection();
        testOllamaConnection();

        // INTENT TESTS
        testAllIntents();

        // PRINT SUMMARY REPORT
        printSummaryReport();

        System.exit(0);
    }

    // ============ INTEGRATION TEST 1: DATABASE ============
    private static void testDatabaseConnection() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("INTEGRATION TEST 1: SQLite Database Connection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();
        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");
            store.loadLeaveRequestsFromCsv("src/main/resources/leave_requests.csv"); 
            store.loadPerformanceReviewsFromCsv("src/main/resources/performance_reviews.csv");

            int employeeCount = store.getAllEmployees().size();
            long duration = System.currentTimeMillis() - startTime;

            if (employeeCount > 0) {
                integrationResults.add(new TestResult("SQLite Connection", true, duration, "✅ Loaded " + employeeCount + " employees"));
                integrationPassCount++;
                sqliteSuccess = true;
                System.out.println("✅ PASS - SQLite connection successful (" + duration + "ms)");
                System.out.println("   Loaded " + employeeCount + " employees from CSV\n");
            } else {
                integrationResults.add(new TestResult("SQLite Connection", false, duration, "❌ No employees loaded"));
                integrationFailCount++;
                sqliteSuccess = false;
                System.out.println("❌ FAIL - No employees loaded\n");
            }

            store.close();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            integrationResults.add(new TestResult("SQLite Connection", false, duration, "❌ Error: " + e.getMessage()));
            integrationFailCount++;
            sqliteSuccess = false;
            System.out.println("❌ FAIL - SQLite connection failed: " + e.getMessage() + "\n");
        }
    }

    // ============ INTEGRATION TEST 2: OLLAMA ============
    private static void testOllamaConnection() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("INTEGRATION TEST 2: Ollama LLM Connection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();
        try {
            LLMService llmService = new LLMService();
            
            CommandIntent testIntent = llmService.extractIntent("siapa manajer budi?");
            long duration = System.currentTimeMillis() - startTime;
            ollamaCallCount++;
            totalOllamaDuration += duration;

            if (testIntent != null) {
                integrationResults.add(new TestResult("Ollama Connection", true, duration, "✅ Intent extracted successfully"));
                integrationPassCount++;
                ollamaSuccess = true;
                System.out.println("✅ PASS - Ollama connection successful (" + duration + "ms)");
                System.out.println("   Intent extracted: " + testIntent.getIntent() + "\n");
            } else {
                integrationResults.add(new TestResult("Ollama Connection", false, duration, "❌ No response from Ollama"));
                integrationFailCount++;
                ollamaSuccess = false;
                System.out.println("❌ FAIL - No response from Ollama\n");
            }

            llmService.shutdown();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            integrationResults.add(new TestResult("Ollama Connection", false, duration, "❌ Error: " + e.getMessage()));
            integrationFailCount++;
            ollamaSuccess = false;
            System.out.println("❌ FAIL - Ollama connection failed: " + e.getMessage() + "\n");
        }
    }

    // ============ INTENT TESTS ============
    private static void testAllIntents() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("INTENT TESTS: Testing 17 Intents");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");
            store.loadLeaveRequestsFromCsv("src/main/resources/leave_requests.csv"); 
            store.loadPerformanceReviewsFromCsv("src/main/resources/performance_reviews.csv");

            LLMService llmService = new LLMService();
            HRFunctions hrFunctions = new MockHRFunctions();
            QueryService queryService = new QueryService(store);
            ActionService actionService = new ActionService(llmService, hrFunctions, store);

            // EMPLOYEE MANAGEMENT QUERIES
            System.out.println("📋 EMPLOYEE MANAGEMENT QUERIES:\n");
            testQueryIntentViaLLM(llmService, queryService, "EmployeeManagement-1_QUERY_EMPLOYEE_INFO", 
                "siapa manajer budi?", "Santi");
            testQueryIntentViaLLM(llmService, queryService, "LeaveManagement-2_LIST_CUTI_PENDING", 
                "list semua cuti yang pending", "LR003");
            testQueryIntentViaLLM(llmService, queryService, "EmployeeManagement-3_LIST_KARYAWAN_JABATAN", 
                "siapa saja yang jabatannya software engineer", "Software Engineer");
            testQueryIntentViaLLM(llmService, queryService, "EmployeeManagement-4_LIST_KARYAWAN_STATUS", 
                "list karyawan yang statusnya aktif", "Aktif");

            // LEAVE MANAGEMENT QUERIES
            System.out.println("📋 LEAVE MANAGEMENT QUERIES:\n");
            testQueryIntentViaLLM(llmService, queryService, "LeaveManagement-1_CHECK_LEAVE_BALANCE", 
                "sisa cuti budi berapa?", "12");
            testQueryIntentViaLLM(llmService, queryService, "LeaveManagement-2_LIST_CUTI_PENDING", 
                "list semua cuti yang pending", "Menunggu");
            testQueryIntentViaLLM(llmService, queryService, "LeaveManagement-3_HISTORY_CUTI", 
                "riwayat cuti budi", "Status");

            // PERFORMANCE REVIEW QUERIES
            System.out.println("📋 PERFORMANCE REVIEW QUERIES:\n");
            testQueryIntentViaLLM(llmService, queryService, "PerformanceReview-1_LIST_REVIEW_TERJADWAL", 
                "list review yang terjadwal", "review");
            testQueryIntentViaLLM(llmService, queryService, "PerformanceReview-2_HISTORY_REVIEW", 
                "riwayat review rina", "Rina");

            // EMPLOYEE MANAGEMENT ACTIONS
            System.out.println("⚙️ EMPLOYEE MANAGEMENT ACTIONS:\n");
            testActionIntentViaLLM(llmService, actionService, "EmployeeManagement-5_UPDATE_DATA_KARYAWAN", 
                "pindahkan budi ke departemen sales jadi sales manager", "✅");
            testActionIntentViaLLM(llmService, actionService, "EmployeeManagement-6_TAMBAH_KARYAWAN", 
                "tambah karyawan baru nama john doe email john@example.com jabatan developer departemen engineering", "✅");

            // LEAVE MANAGEMENT ACTIONS
            System.out.println("⚙️ LEAVE MANAGEMENT ACTIONS:\n");
            testActionIntentViaLLM(llmService, actionService, "LeaveManagement-4_APPLY_LEAVE", 
                "tolong apply cuti tahunan buat budi dari tgl 2025-10-03 sampai 2025-10-05", "✅");
            testActionIntentViaLLM(llmService, actionService, "LeaveManagement-5_APPROVE_REJECT_CUTI", 
                "approve cuti LR003", "✅");
            testActionIntentViaLLM(llmService, actionService, "LeaveManagement-6_BATALKAN_CUTI", 
                "batalkan cuti LR006", "✅");

            // PERFORMANCE REVIEW ACTIONS
            System.out.println("⚙️ PERFORMANCE REVIEW ACTIONS:\n");
            testActionIntentViaLLM(llmService, actionService, "PerformanceReview-3_SCHEDULE_REVIEW", 
                "jadwalkan review performa utk rina dgn bu santi tanggal 2025-11-15", "✅");
            testActionIntentViaLLM(llmService, actionService, "PerformanceReview-4_UPDATE_SKOR_REVIEW", 
                "update skor review REV-001 jadi 85", "✅");
            testActionIntentViaLLM(llmService, actionService, "PerformanceReview-5_SUBMIT_HASIL_REVIEW", 
                "submit hasil review REV-001 dengan skor 90", "✅");

            llmService.shutdown();
            store.close();
        } catch (Exception e) {
            System.out.println("❌ FAIL - Intent test setup failed: " + e.getMessage() + "\n");
        }
    }

    private static void testQueryIntentViaLLM(LLMService llmService, QueryService queryService, String testId, String query, String expectedKeyword) {
        long startTime = System.currentTimeMillis();
        try {
            CommandIntent intent = llmService.extractIntent(query);
            ollamaCallCount++;
            
            String response = "";
            if (intent != null && isQueryIntent(intent.getIntent())) {
                response = queryService.answerByIntent(intent.getIntent(), intent);
            } else {
                response = queryService.answer(query);
            }

            if (testId.contains("HISTORY_CUTI")) {
                System.out.println("🔍 DEBUG [" + testId + "]");
                System.out.println("   Query: " + query);
                System.out.println("   Intent: " + (intent != null ? intent.getIntent() : "N/A"));
                System.out.println("   Response: " + response);
                System.out.println();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            totalOllamaDuration += duration;
            
            boolean passed = response.toLowerCase().contains(expectedKeyword.toLowerCase());
            
            if (passed) {
                intentResults.add(new TestResult(testId, true, duration, "✅ Response contains '" + expectedKeyword + "'"));
                intentPassCount++;
                System.out.println("✅ PASS - [" + testId + "] (" + duration + "ms)");
            } else {
                intentResults.add(new TestResult(testId, false, duration, "❌ Expected '" + expectedKeyword + "' not found"));
                intentFailCount++;
                System.out.println("❌ FAIL - [" + testId + "] (" + duration + "ms)");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            intentResults.add(new TestResult(testId, false, duration, "❌ Error: " + e.getMessage()));
            intentFailCount++;
            System.out.println("❌ FAIL - [" + testId + "]: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testActionIntentViaLLM(LLMService llmService, ActionService actionService, String testId, String command, String expectedKeyword) {
        long startTime = System.currentTimeMillis();
        try {
            String response = actionService.execute(command);
            long duration = System.currentTimeMillis() - startTime;
            totalOllamaDuration += duration;
            ollamaCallCount++;
            
            boolean passed = response.contains(expectedKeyword);
            
            if (passed) {
                intentResults.add(new TestResult(testId, true, duration, "✅ Action executed successfully"));
                intentPassCount++;
                System.out.println("✅ PASS - [" + testId + "] (" + duration + "ms)");
            } else {
                intentResults.add(new TestResult(testId, false, duration, "❌ Action failed"));
                intentFailCount++;
                System.out.println("❌ FAIL - [" + testId + "] (" + duration + "ms)");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            intentResults.add(new TestResult(testId, false, duration, "❌ Error: " + e.getMessage()));
            intentFailCount++;
            System.out.println("❌ FAIL - [" + testId + "]: " + e.getMessage());
        }
        System.out.println();
    }

    private static boolean isQueryIntent(String intentName) {
        return intentName != null && (
            intentName.equals("list_karyawan_departemen") ||
            intentName.equals("list_karyawan_jabatan") ||
            intentName.equals("list_karyawan_status") ||
            intentName.equals("cek_status_cuti") ||
            intentName.equals("list_cuti_pending") ||
            intentName.equals("history_cuti") ||
            intentName.equals("riwayat_cuti") ||
            intentName.equals("list_review_terjadwal") ||
            intentName.equals("history_review")
        );
    }

    // ============ NEW SUMMARY REPORT STRUCTURE ============
    private static void printSummaryReport() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📊 SUMMARY REPORT                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // INTEGRATION TEST SUMMARY
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("INTEGRATION TEST");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Test Ollama:  " + (ollamaSuccess ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println("Test SQLite:  " + (sqliteSuccess ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println();

        // DETAIL FAILED INTEGRATION TEST
        if (integrationFailCount > 0) {
            System.out.println("Detail Failed Integration Test:");
            System.out.println("════════════════════════════════");
            for (TestResult result : integrationResults) {
                if (!result.passed) {
                    System.out.println("❌ " + result.testName);
                    System.out.println("   Duration: " + result.duration + "ms");
                    System.out.println("   Reason: " + result.message);
                    System.out.println();
                }
            }
        }

        // INTENT TEST SUMMARY
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("INTENT TEST");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        int totalIntentTests = intentPassCount + intentFailCount;
        double successRate = totalIntentTests > 0 ? (intentPassCount * 100.0 / totalIntentTests) : 0.0;
        
        System.out.println("Total Test: " + totalIntentTests);
        System.out.println("Passed:     " + intentPassCount);
        System.out.println("Failed:     " + intentFailCount);
        System.out.println("Success Rate: " + String.format("%.2f%%", successRate));
        System.out.println();

        // DETAIL FAILED INTENT TEST
        if (intentFailCount > 0) {
            System.out.println("Detail Failed Intent Test:");
            System.out.println("══════════════════════════");
            for (TestResult result : intentResults) {
                if (!result.passed) {
                    System.out.println("❌ " + result.testName);
                    System.out.println("   Duration: " + result.duration + "ms");
                    System.out.println("   Reason: " + result.message);
                    System.out.println();
                }
            }
        }

        // PERFORMANCE SUMMARY
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("PERFORMANCE SUMMARY");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Ollama Model Usage
        long avgOllama = ollamaCallCount > 0 ? totalOllamaDuration / ollamaCallCount : 0;
        System.out.println("Ollama Model Usage:");
        System.out.println("  - Total Calls:    " + ollamaCallCount);
        System.out.println("  - Total Duration: " + totalOllamaDuration + "ms");
        System.out.println("  - Average:        " + avgOllama + "ms/call");
        System.out.println();

        // Database Performance
        long dbDuration = integrationResults.stream()
            .filter(r -> r.testName.equals("SQLite Connection"))
            .mapToLong(r -> r.duration)
            .findFirst()
            .orElse(0);
        System.out.println("Database Performance:");
        System.out.println("  - Load Time:      " + dbDuration + "ms");
        System.out.println();

        // Processing Time Stats (for all intent tests)
        if (!intentResults.isEmpty()) {
            long maxDuration = intentResults.stream().mapToLong(r -> r.duration).max().orElse(0);
            long minDuration = intentResults.stream().mapToLong(r -> r.duration).min().orElse(0);
            long avgDuration = intentResults.stream().mapToLong(r -> r.duration).sum() / intentResults.size();
            
            TestResult slowest = intentResults.stream()
                .filter(r -> r.duration == maxDuration)
                .findFirst()
                .orElse(null);
            
            TestResult fastest = intentResults.stream()
                .filter(r -> r.duration == minDuration)
                .findFirst()
                .orElse(null);
            
            System.out.println("Processing Time:");
            System.out.println("  - Highest:        " + maxDuration + "ms [" + (slowest != null ? slowest.testName : "N/A") + "]");
            System.out.println("  - Lowest:         " + minDuration + "ms [" + (fastest != null ? fastest.testName : "N/A") + "]");
            System.out.println("  - Average:        " + avgDuration + "ms");
        }
        
        System.out.println("\n" + (integrationFailCount == 0 && intentFailCount == 0 ? "🎉 All tests passed! System is ready to use." : "⚠️ Some tests failed. Please review the details above.") + "\n");
    }

    // ============ TEST RESULT CLASS ============
    static class TestResult {
        String testName;
        boolean passed;
        long duration;
        String message;

        TestResult(String testName, boolean passed, long duration, String message) {
            this.testName = testName;
            this.passed = passed;
            this.duration = duration;
            this.message = message;
        }
    }
}
