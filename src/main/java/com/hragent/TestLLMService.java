package com.hragent;

import com.hragent.llm.CommandIntent;
import com.hragent.llm.LLMService;

public class TestLLMService {

    public static void main(String[] args) {
        System.out.println("=== 🧠 Testing LLM Service ===\n");
        System.out.println("🔍 Pastikan Ollama sedang berjalan di http://localhost:11434\n");

        try (LLMService llm = new LLMService()) {

            String[] commands = {
                "tolong apply cuti tahunan buat budi dari tgl 3 oktober sampai 5 oktober",
                "jadwalkan review performa utk rina dgn bu santi jumat depan",
                "ajukan cuti sakit gw dong besok",
                "apply cuti tahunan untuk leo mulai 15 desember sampai 20 desember"
            };

            for (String cmd : commands) {
                System.out.println("💬 COMMAND: " + cmd);
                CommandIntent intent = llm.extractIntent(cmd);

                if (intent != null) {
                    System.out.println("📦 EXTRACTED: " + intent);
                } else {
                    System.out.println("⚠️  Gagal mengekstrak intent!");
                }
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("❌ Error during LLM test: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Semua pengujian LLM selesai!\n");
        System.exit(0);
    }
}
