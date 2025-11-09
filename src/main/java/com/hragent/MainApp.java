package com.hragent;

import com.hragent.action.*;
import com.hragent.data.SQLiteDataStore;
import com.hragent.llm.LLMService;
import com.hragent.query.*;
import com.hragent.intent.*;

import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {
        printHeader();
    
        final SQLiteDataStore[] dataStoreRef = new SQLiteDataStore[1];
        final LLMService[] llmServiceRef = new LLMService[1];
    
        try {
            dataStoreRef[0] = new SQLiteDataStore();
            dataStoreRef[0].loadEmployeesFromCsv("src/main/resources/employees.csv");
            dataStoreRef[0].loadLeaveBalancesFromCsv("src/main/resources/leave_balances.csv");
            System.out.println("\n✅ Loading complete. Agent ready!\n");
    
            llmServiceRef[0] = new LLMService();
    
            HRFunctions hrFunctions = new MockHRFunctions();
            QueryService queryService = new QueryService(dataStoreRef[0]);
            ActionService actionService = new ActionService(llmServiceRef[0], hrFunctions, dataStoreRef[0]);
            IntentDetector intentDetector = new IntentDetector();
    
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n👋 Shutting down Agent SmartHR...");
                try {
                    if (llmServiceRef[0] != null) {
                        llmServiceRef[0].shutdown();
                    }
                    if (dataStoreRef[0] != null) {
                        dataStoreRef[0].close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("✅ Cleanup complete. Goodbye!");
            }));
    
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
                    case QUESTION:
                        System.out.println("[Mode: QUERY]");
                        response = queryService.answer(input);
                        break;
                    case COMMAND:
                        System.out.println("[Mode: ACTION]");
                        response = actionService.execute(input);
                        break;
                    default:
                        response = "Maaf, saya tidak yakin maksud Anda. Ketik 'help' untuk melihat contoh pertanyaan dan perintah.";
                        break;
                }
    
                System.out.println("Agent: " + response + "\n");
            }
    
            scanner.close();
    
            // Manual shutdown juga bisa dipanggil di sini
            if (llmServiceRef[0] != null) {
                llmServiceRef[0].shutdown();
            }
            if (dataStoreRef[0] != null) {
                dataStoreRef[0].close();
            }
    
        } catch (Exception e) {
            System.err.println("Failed to load data or initialize services: " + e.getMessage());
            e.printStackTrace();
        }
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
