package com.hragent;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.llm.CommandIntent;
import com.hragent.llm.LLMService;
import com.hragent.query.QueryService;

import java.util.ArrayList;
import java.util.List;

public class SystemTest {

    private static List<TestResult> results = new ArrayList<>();
    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          🧪 SYSTEM TEST - HR Agent SmartHR                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Test 1: Database Connection
        testDatabaseConnection();

        // Test 2: Ollama Connection
        testOllamaConnection();

        // Test 3: All Intents (Query & Actions)
        testAllIntents();

        // Print Summary Report
        printSummaryReport();

        // Cleanup
        System.exit(0);
    }

    // ============ TEST 1: DATABASE CONNECTION ============
    private static void testDatabaseConnection() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: Database Connection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();
        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");

            int employeeCount = store.getAllEmployees().size();
            long duration = System.currentTimeMillis() - startTime;

            if (employeeCount > 0) {
                results.add(new TestResult("Database Connection", true, duration, "✅ Loaded " + employeeCount + " employees"));
                passCount++;
                System.out.println("✅ PASS - Database connection successful (" + duration + "ms)");
                System.out.println("   Loaded " + employeeCount + " employees from CSV\n");
            } else {
                results.add(new TestResult("Database Connection", false, duration, "❌ No employees loaded"));
                failCount++;
                System.out.println("❌ FAIL - No employees loaded\n");
            }

            store.close();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            results.add(new TestResult("Database Connection", false, duration, "❌ Error: " + e.getMessage()));
            failCount++;
            System.out.println("❌ FAIL - Database connection failed: " + e.getMessage() + "\n");
        }
    }

    // ============ TEST 2: OLLAMA CONNECTION ============
    private static void testOllamaConnection() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: Ollama LLM Connection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();
        try {
            LLMService llmService = new LLMService();
            
            // Test extractIntent method instead of generate
            CommandIntent testIntent = llmService.extractIntent("siapa manajer budi?");
            long duration = System.currentTimeMillis() - startTime;

            if (testIntent != null) {
                results.add(new TestResult("Ollama Connection", true, duration, "✅ Intent extracted successfully"));
                passCount++;
                System.out.println("✅ PASS - Ollama connection successful (" + duration + "ms)");
                System.out.println("   Intent extracted: " + testIntent.getIntent() + "\n");
            } else {
                results.add(new TestResult("Ollama Connection", false, duration, "❌ No response from Ollama"));
                failCount++;
                System.out.println("❌ FAIL - No response from Ollama\n");
            }

            llmService.shutdown();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            results.add(new TestResult("Ollama Connection", false, duration, "❌ Error: " + e.getMessage()));
            failCount++;
            System.out.println("❌ FAIL - Ollama connection failed: " + e.getMessage() + "\n");
        }
    }


    // ============ TEST 3: ALL INTENTS ============
    private static void testAllIntents() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: All Intents");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");

            LLMService llmService = new LLMService();
            HRFunctions hrFunctions = new MockHRFunctions();
            QueryService queryService = new QueryService(store);
            ActionService actionService = new ActionService(llmService, hrFunctions, store);

            // Query Intents
            testQueryIntent(queryService, "siapa manajer budi?", "Santi");
            testQueryIntent(queryService, "sisa cuti budi berapa?", "12");
            testQueryIntent(queryService, "jabatan dewi apa?", "Sales Executive");
            testQueryIntent(queryService, "email santi?", "santi.p@examplecorp.com");

            // Action Intents
            testActionIntent(actionService, "tolong apply cuti tahunan buat budi dari tgl 2025-10-03 sampai 2025-10-05", "✅");
            testActionIntent(actionService, "jadwalkan review performa utk rina dgn bu santi tanggal 2025-11-15", "✅");

            llmService.shutdown();
            store.close();
        } catch (Exception e) {
            System.out.println("❌ FAIL - Intent test setup failed: " + e.getMessage() + "\n");
        }
    }

    private static void testQueryIntent(QueryService queryService, String query, String expectedKeyword) {
        long startTime = System.currentTimeMillis();
        try {
            String response = queryService.answer(query);
            long duration = System.currentTimeMillis() - startTime;

            boolean passed = response.toLowerCase().contains(expectedKeyword.toLowerCase());
            
            if (passed) {
                results.add(new TestResult("Query: " + query, true, duration, "✅ Response contains '" + expectedKeyword + "'"));
                passCount++;
                System.out.println("✅ PASS - " + query + " (" + duration + "ms)");
                System.out.println("   Response: " + response.substring(0, Math.min(80, response.length())) + "...\n");
            } else {
                results.add(new TestResult("Query: " + query, false, duration, "❌ Expected '" + expectedKeyword + "' not found"));
                failCount++;
                System.out.println("❌ FAIL - " + query + " (" + duration + "ms)");
                System.out.println("   Expected keyword '" + expectedKeyword + "' not found in response\n");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            results.add(new TestResult("Query: " + query, false, duration, "❌ Error: " + e.getMessage()));
            failCount++;
            System.out.println("❌ FAIL - " + query + ": " + e.getMessage() + "\n");
        }
    }

    private static void testActionIntent(ActionService actionService, String command, String expectedKeyword) {
        long startTime = System.currentTimeMillis();
        try {
            String response = actionService.execute(command);
            long duration = System.currentTimeMillis() - startTime;

            boolean passed = response.contains(expectedKeyword);
            
            if (passed) {
                results.add(new TestResult("Action: " + command, true, duration, "✅ Action executed successfully"));
                passCount++;
                System.out.println("✅ PASS - " + command + " (" + duration + "ms)");
                System.out.println("   Response: " + response.substring(0, Math.min(80, response.length())) + "...\n");
            } else {
                results.add(new TestResult("Action: " + command, false, duration, "❌ Action failed"));
                failCount++;
                System.out.println("❌ FAIL - " + command + " (" + duration + "ms)");
                System.out.println("   Response: " + response + "\n");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            results.add(new TestResult("Action: " + command, false, duration, "❌ Error: " + e.getMessage()));
            failCount++;
            System.out.println("❌ FAIL - " + command + ": " + e.getMessage() + "\n");
        }
    }

    // ============ SUMMARY REPORT ============
    private static void printSummaryReport() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📊 SUMMARY REPORT                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        System.out.println("Total Tests: " + (passCount + failCount));
        System.out.println("✅ Passed: " + passCount);
        System.out.println("❌ Failed: " + failCount);
        System.out.println("Success Rate: " + String.format("%.2f%%", (passCount * 100.0 / (passCount + failCount))) + "\n");

        if (failCount > 0) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("FAILED TESTS:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (TestResult result : results) {
                if (!result.passed) {
                    System.out.println("❌ " + result.testName);
                    System.out.println("   Duration: " + result.duration + "ms");
                    System.out.println("   Reason: " + result.message + "\n");
                }
            }
        } else {
            System.out.println("🎉 All tests passed! System is ready to use.\n");
        }

        // Performance Summary
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("PERFORMANCE SUMMARY:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        long totalDuration = results.stream().mapToLong(r -> r.duration).sum();
        long avgDuration = totalDuration / results.size();

        System.out.println("Total Duration: " + totalDuration + "ms");
        System.out.println("Average Duration: " + avgDuration + "ms\n");
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
