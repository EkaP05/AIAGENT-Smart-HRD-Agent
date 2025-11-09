package com.hragent.util;

import com.hragent.data.*;
import com.hragent.query.*;

public class TestQueryService {
    public static void main(String[] args) {
        System.out.println("=== Testing Query Service ===\n");

        try {
            SQLiteDataStore store = new SQLiteDataStore();
            store.loadEmployeesFromCsv("src/main/resources/employees.csv");
            store.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");

            QueryService queryService = new QueryService(store);
            
            // Test queries
            String[] questions = {
                "siapa manajer rina?",
                "siapa manajer budi?",
                "sisa cuti budi berapa?",
                "sisa cuti sakit leo ada berapa?",
                "status cuti rina gimana?",
                "jabatan dewi apa?",
                "email santi?",
                "siapa manajer indra?"
            };
            
            for (String question : questions) {
                System.out.println("Q: " + question);
                System.out.println("A: " + queryService.answer(question));
                System.out.println();
            }
            
            System.out.println("✅ Query tests completed!");
        } catch (Exception e) {
            System.err.println("Error during test setup or execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
        
}
