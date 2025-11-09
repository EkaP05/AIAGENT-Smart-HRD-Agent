package com.hragent.util;

import com.hragent.llm.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class TestModelComparison {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        System.out.println("=== Comparison: Model Performance for HR Commands ===\n");
        
        String[] models = {
            "qwen2.5:3b",
            "mistral:7b-instruct-q4_K_M",
            "qwen3:8b",
            "deepseek-r1:1.5b"
        };
        
        String[] testCommands = {
            "tolong apply cuti tahunan buat budi dari tgl 3 oktober sampai 5 oktober",
            "jadwalkan review performa utk rina dgn bu santi jumat depan",
            "ajukan cuti sakit gw dong besok",
            "apply cuti tahunan untuk leo mulai 15 desember sampai 20 desember",
            "cek status cuti terakhir dewi"
        };
        
        // Test setiap model
        for (String modelName : models) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("MODEL: " + modelName);
            System.out.println("=".repeat(70));
            
            testModel(modelName, testCommands);
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ Model comparison completed!");
    }
    
    private static void testModel(String modelName, String[] commands) {
        try {
            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName(modelName)
                    .temperature(0.1)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            int successCount = 0;
            long totalTime = 0;
            
            for (int i = 0; i < commands.length; i++) {
                String cmd = commands[i];
                System.out.println("\nTest " + (i+1) + ": " + cmd);
                
                long start = System.currentTimeMillis();
                String prompt = buildPrompt(cmd);
                String response = model.generate(prompt);
                long elapsed = System.currentTimeMillis() - start;
                totalTime += elapsed;
                
                // Extract JSON
                String json = extractJson(response);
                
                // Validate
                boolean valid = validateJson(json);
                if (valid) {
                    successCount++;
                    System.out.println("  ✅ Valid JSON (" + elapsed + "ms)");
                    System.out.println("  Result: " + json);
                } else {
                    System.out.println("  ❌ Invalid JSON (" + elapsed + "ms)");
                    System.out.println("  Response: " + response.substring(0, Math.min(200, response.length())));
                }
            }
            
            System.out.println("\n--- SUMMARY ---");
            System.out.println("Success rate: " + successCount + "/" + commands.length);
            System.out.println("Avg response time: " + (totalTime / commands.length) + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ Error testing model: " + e.getMessage());
        }
    }
    
    private static String buildPrompt(String userCommand) {
        LocalDate today = LocalDate.now();
        return """
            Kamu adalah asisten HR yang mengekstrak informasi dari perintah bahasa Indonesia.
            
            HARI INI: %s
            PERINTAH: "%s"
            
            Ekstrak ke JSON format ini:
            {
              "intent": "apply_leave | schedule_review | check_status",
              "employee_name": "nama",
              "leave_type": "Tahunan | Sakit",
              "start_date": "YYYY-MM-DD",
              "end_date": "YYYY-MM-DD",
              "reviewer_name": "nama reviewer (optional)"
            }
            
            Aturan:
            - "besok" = %s
            - "Jumat depan" = Jumat minggu depan
            - Jika hanya 1 tanggal, end_date = start_date
            
            Respons: HANYA JSON, tanpa teks lain.
            """.formatted(today, userCommand, today.plusDays(1));
    }
    
    private static String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response.trim();
    }
    
    private static boolean validateJson(String json) {
        try {
            CommandIntent intent = objectMapper.readValue(json, CommandIntent.class);
            return intent.getIntent() != null && !intent.getIntent().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
