package com.hragent;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.llm.LLMService;
import com.hragent.query.QueryService;
import com.hragent.intent.*;
import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {
        printHeader();

        SQLiteDataStore dataStore;
        try {
            dataStore = new SQLiteDataStore();
            dataStore.loadEmployeesFromCsv("src/main/resources/employees.csv");
            dataStore.loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");
            // Load other CSVs into SQLiteDataStore if needed
            System.out.println("\n✅ Loading complete. Agent ready!\n");
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n👋 Shutting down Agent SmartHR...");
            try {
                dataStore.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("✅ Cleanup complete. Goodbye!");
        }));

        LLMService llmService = new LLMService();
        HRFunctions hrFunctions = new MockHRFunctions();

        QueryService queryService = new QueryService(dataStore);
        ActionService actionService = new ActionService(llmService, hrFunctions, dataStore);
        IntentDetector intentDetector = new IntentDetector();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("\n👋 Terima kasih! Sampai jumpa lagi.");
                break;
            }

            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }

            IntentType intent = intentDetector.detect(input);

            String response;
            switch (intent) {
                case QUESTION -> {
                    System.out.println("[Mode: QUERY]");
                    response = queryService.answer(input);
                }
                case COMMAND -> {
                    System.out.println("[Mode: ACTION]");
                    response = actionService.execute(input);
                }
                default -> response = "Maaf, saya tidak yakin maksud Anda. Ketik 'help' untuk melihat contoh pertanyaan dan perintah.";
            }

            System.out.println("Agent: " + response + "\n");
        }

        scanner.close();
    }

    private static void printHeader() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          🤖 HR AGENT - Asisten HR Cerdas                  ║");
        System.out.println("║                  Powered by Qwen2.5:3b                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printHelp() {
        System.out.println("\n📖 PANDUAN PENGGUNAAN\n");
        System.out.println("PERTANYAAN (Query Data):");
        System.out.println("  • siapa manajer budi?");
        System.out.println("  • sisa cuti rina berapa?");
        System.out.println("  • jabatan dewi apa?");
        System.out.println("  • email santi?");
        System.out.println("  • status cuti leo?");
        System.out.println();
        System.out.println("PERINTAH (Execute Action):");
        System.out.println("  • tolong apply cuti tahunan buat budi dari tgl 3 okt sampai 5 okt");
        System.out.println("  • jadwalkan review performa utk rina dgn bu santi jumat depan");
        System.out.println("  • ajukan cuti sakit untuk leo besok");
        System.out.println("  • cek status cuti terakhir dewi");
        System.out.println();
    }
}
