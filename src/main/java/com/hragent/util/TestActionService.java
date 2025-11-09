package com.hragent.util;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.llm.*;

public class TestActionService {
    public static void main(String[] args) {
        System.out.println("=== Testing Action Service Integration ===\n");

        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");

            LLMService llmService = new LLMService();
            HRFunctions hrFunctions = new MockHRFunctions();
            ActionService actionService = new ActionService(llmService, hrFunctions, store);

            System.out.println("\n" + "=".repeat(70));
            System.out.println("Testing Commands Execution");
            System.out.println("=".repeat(70) + "\n");

            String[] commands = {
                "tolong apply cuti tahunan buat budi dari tgl 3 oktober sampai 5 oktober",
                "jadwalkan review performa utk rina dgn bu santi jumat depan",
                "ajukan cuti sakit untuk leo besok",
                "cek status cuti terakhir dewi"
            };

            for (int i = 0; i < commands.length; i++) {
                System.out.println((i+1) + ". USER: " + commands[i]);
                String result = actionService.execute(commands[i]);
                System.out.println("   AGENT: " + result);
                System.out.println();
            }

            System.out.println("=".repeat(70));
            System.out.println("✅ All action tests completed!");

            // Shutdown explicit untuk LLMService dan SQLiteDataStore agar proses bersih
            llmService.shutdown();
            store.close();

        } catch (Exception e) {
            System.err.println("Error during test setup or execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
