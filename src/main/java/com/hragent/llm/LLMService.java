package com.hragent.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.io.Closeable;
import java.time.Duration;
import java.time.LocalDate;

//LLMService: Menggunakan Ollama untuk ekstraksi structured output

public class LLMService implements Closeable {

    private final OllamaChatModel model;
    private final ObjectMapper objectMapper;

    public LLMService() {
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5:3b")
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public CommandIntent extractIntent(String userCommand) {
        try {
            String prompt = buildExtractionPrompt(userCommand);
            String response = model.generate(prompt);

            String jsonStr = extractJsonFromResponse(response);
            System.out.println("LLM Response: " + jsonStr);

            CommandIntent intent = objectMapper.readValue(jsonStr, CommandIntent.class);
            normalizeDates(intent);
            return intent;
        } catch (Exception e) {
            System.err.println("Error extracting intent: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String buildExtractionPrompt(String userCommand) {
        LocalDate today = LocalDate.now();

        return String.format(
                "Kamu adalah asisten HR yang mengekstrak informasi terstruktur dari perintah bahasa Indonesia.\n\n" +
                        "HARI INI: %s\n\n" +
                        "PERINTAH USER: \"%s\"\n\n" +
                        "TUGAS: Ekstrak informasi ke format JSON ini:\n" +
                        "{\n" +
                        "  \"intent\": \"apply_leave | schedule_review | check_status | submit_expense | lookup_colleague\",\n" +
                        "  \"employee_name\": \"nama karyawan\",\n" +
                        "  \"leave_type\": \"Tahunan | Sakit | Cuti Melahirkan\",\n" +
                        "  \"start_date\": \"YYYY-MM-DD\",\n" +
                        "  \"end_date\": \"YYYY-MM-DD\",\n" +
                        "  \"reviewer_name\": \"nama reviewer (optional)\",\n" +
                        "  \"category\": \"kategori expense (optional)\",\n" +
                        "  \"amount\": jumlah_nominal (optional)\n" +
                        "}\n\n" +
                        "PANDUAN:\n" +
                        "- \"besok\" = %s\n" +
                        "- \"lusa\" = %s\n" +
                        "- \"Senin depan\", \"Jumat depan\" = tanggal hari tersebut minggu depan\n" +
                        "- \"tahunan\" = Tahunan, \"sakit\" = Sakit\n" +
                        "- Jika start_date tidak disebutkan eksplisit, gunakan context \"besok\", \"lusa\", dll\n" +
                        "- Jika hanya 1 tanggal disebutkan, end_date = start_date\n\n" +
                        "CONTOH:\n" +
                        "User: \"tolong apply cuti tahunan buat budi dari tgl 3 oktober sampai 5 oktober\"\n" +
                        "JSON: {\"intent\":\"apply_leave\",\"employee_name\":\"Budi Santoso\",\"leave_type\":\"Tahunan\",\"start_date\":\"2025-10-03\",\"end_date\":\"2025-10-05\"}\n\n" +
                        "User: \"jadwalkan review performa utk rina dgn bu santi jumat depan\"\n" +
                        "JSON: {\"intent\":\"schedule_review\",\"employee_name\":\"Rina Wijaya\",\"reviewer_name\":\"Santi Putri\",\"start_date\":\"%s\"}\n\n" +
                        "User: \"ajukan cuti sakit gw dong besok\"\n" +
                        "JSON: {\"intent\":\"apply_leave\",\"employee_name\":\"SELF\",\"leave_type\":\"Sakit\",\"start_date\":\"%s\",\"end_date\":\"%s\"}\n\n" +
                        "RESPONS: Berikan HANYA JSON, tanpa penjelasan atau teks lain.",
                today,
                userCommand,
                today.plusDays(1),
                today.plusDays(2),
                getNextFriday(today),
                today.plusDays(1),
                today.plusDays(1)
        );
    }

    private String extractJsonFromResponse(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response.trim();
    }

    private void normalizeDates(CommandIntent intent) {
        if (intent.getLeaveType() != null) {
            String type = intent.getLeaveType().toLowerCase();
            if (type.contains("tahunan") || type.contains("annual")) {
                intent.setLeaveType("Tahunan");
            } else if (type.contains("sakit") || type.contains("sick")) {
                intent.setLeaveType("Sakit");
            } else if (type.contains("melahirkan") || type.contains("maternity")) {
                intent.setLeaveType("Cuti Melahirkan");
            }
        }
    }

    private LocalDate getNextFriday(LocalDate from) {
        LocalDate next = from;
        while (next.getDayOfWeek().getValue() != 5) {
            next = next.plusDays(1);
        }
        if (next.equals(from)) {
            next = next.plusDays(7);
        }
        return next;
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        try {
            System.out.println("🧹 Shutting down LLMService...");
            if (model instanceof AutoCloseable) {
                ((AutoCloseable) model).close();
            }
            System.out.println("✅ LLMService shutdown complete.");
        } catch (Exception e) {
            System.err.println("⚠️ Error during LLMService shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
