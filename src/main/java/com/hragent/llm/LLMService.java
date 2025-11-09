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
            //System.out.println("LLM Response: " + jsonStr);

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
                        "  \"intent\": \"apply_leave | schedule_review | check_status | approve_reject_cuti | batalkan_cuti | " +
                        "cek_status_cuti | list_cuti_pending | riwayat_cuti | history_cuti | submit_expense | lookup_colleague | " +
                        "list_karyawan_departemen | list_karyawan_jabatan | list_karyawan_status | update_data_karyawan | " +
                        "tambah_karyawan | cek_status_cuti | list_cuti_pending | approve_reject_cuti | batalkan_cuti | " +
                        "update_sisa_cuti | history_cuti | list_review_terjadwal | update_skor_review | batalkan_review | " +
                        "submit_hasil_review | history_review\",\n" +
                        "  \"employee_name\": \"nama karyawan\",\n" +
                        "  \"leave_type\": \"Tahunan | Sakit | Cuti Melahirkan\",\n" +
                        "  \"start_date\": \"YYYY-MM-DD\",\n" +
                        "  \"end_date\": \"YYYY-MM-DD\",\n" +
                        "  \"reviewer_name\": \"nama reviewer (optional)\",\n" +
                        "  \"category\": \"kategori expense (optional)\",\n" +
                        "  \"amount\": jumlah_nominal (optional),\n" +
                        "  \"department\": \"nama departemen (optional)\",\n" +
                        "  \"position\": \"jabatan (optional)\",\n" +
                        "  \"status\": \"status karyawan (optional)\",\n" +
                        "  \"leave_id\": \"ID cuti (optional)\",\n" +
                        "  \"review_id\": \"ID review (optional)\",\n" +
                        "  \"score\": \"skor performa (optional)\",\n" +
                        "  \"new_balance\": \"sisa cuti baru (optional)\"\n" +
                        "}\n\n" +
                        "PANDUAN:\n" +
                        "- \"besok\" = %s\n" +
                        "- \"lusa\" = %s\n" +
                        "- \"Senin depan\", \"Jumat depan\" = tanggal hari tersebut minggu depan\n" +
                        "- \"tahunan\" = Tahunan, \"sakit\" = Sakit\n" +
                        "- Jika start_date tidak disebutkan eksplisit, gunakan context \"besok\", \"lusa\", dll\n" +
                        "- Jika hanya 1 tanggal disebutkan, end_date = start_date\n\n" +
                        "PANDUAN INTENT:\n" +
                        "- 'approve cuti [ID]' atau 'setujui cuti [ID]' = approve_reject_cuti dengan status=Disetujui\n" +
                        "- 'reject cuti [ID]' atau 'tolak cuti [ID]' = approve_reject_cuti dengan status=Ditolak\n" +
                        "- 'batalkan cuti [ID]' atau 'cancel cuti [ID]' = batalkan_cuti\n" +
                        "- 'cek status cuti [ID]' = cek_status_cuti (QUERY only)\n" +
                        "- 'riwayat cuti [nama]' = riwayat_cuti atau history_cuti\n" +

                        "CONTOH INTENT:\n\n" +
                        
                        "1. LEAVE MANAGEMENT:\n" +
                        "User: \"tolong apply cuti tahunan buat budi dari tgl 3 oktober sampai 5 oktober\"\n" +
                        "JSON: {\"intent\":\"apply_leave\",\"employee_name\":\"Budi Santoso\",\"leave_type\":\"Tahunan\",\"start_date\":\"2025-10-03\",\"end_date\":\"2025-10-05\"}\n\n" +
                        
                        "User: \"cek status cuti dengan ID LR001\"\n" +
                        "JSON: {\"intent\":\"cek_status_cuti\",\"leave_id\":\"LEAVE-001\"}\n\n" +
                        
                        "User: \"list semua cuti yang pending\"\n" +
                        "JSON: {\"intent\":\"list_cuti_pending\"}\n\n" +
                        
                        "User: \"approve cuti LR001\"\n" +
                        "JSON: {\"intent\":\"approve_reject_cuti\",\"leave_id\":\"LEAVE-001\",\"status\":\"Disetujui\"}\n\n" +
                        
                        "User: \"batalkan cuti LR002\"\n" +
                        "JSON: {\"intent\":\"batalkan_cuti\",\"leave_id\":\"LEAVE-002\"}\n\n" +
                        
                        "User: \"riwayat cuti budi\"\n" +
                        "JSON: {\"intent\":\"history_cuti\",\"employee_name\":\"Budi Santoso\"}\n\n" +

                        "User: \"reject cuti LR003\"\n" +
                        "JSON: {\"intent\":\"approve_reject_cuti\",\"leave_id\":\"LR003\",\"status\":\"Ditolak\"}\n\n" +

                        "User: \"batalkan cuti LR006\"\n" +
                        "JSON: {\"intent\":\"batalkan_cuti\",\"leave_id\":\"LR006\"}\n\n" +
                        
                        "2. EMPLOYEE MANAGEMENT:\n" +
                        "User: \"list karyawan di departemen engineering\"\n" +
                        "JSON: {\"intent\":\"list_karyawan_departemen\",\"department\":\"Engineering\"}\n\n" +
                        
                        "User: \"siapa saja yang jabatannya software engineer\"\n" +
                        "JSON: {\"intent\":\"list_karyawan_jabatan\",\"position\":\"Software Engineer\"}\n\n" +
                        
                        "User: \"list karyawan yang statusnya aktif\"\n" +
                        "JSON: {\"intent\":\"list_karyawan_status\",\"status\":\"Aktif\"}\n\n" +
                        
                        "User: \"pindahkan budi ke departemen sales jadi sales manager\"\n" +
                        "JSON: {\"intent\":\"update_data_karyawan\",\"employee_name\":\"Budi Santoso\",\"department\":\"Sales\",\"position\":\"Sales Manager\"}\n\n" +
                        
                        "User: \"tambah karyawan baru nama john doe email john@example.com jabatan developer departemen engineering\"\n" +
                        "JSON: {\"intent\":\"tambah_karyawan\",\"employee_name\":\"John Doe\",\"category\":\"john@example.com\",\"position\":\"Developer\",\"department\":\"Engineering\"}\n\n" +
                        
                        "3. PERFORMANCE REVIEW:\n" +
                        "User: \"jadwalkan review performa utk rina dgn bu santi jumat depan\"\n" +
                        "JSON: {\"intent\":\"schedule_review\",\"employee_name\":\"Rina Wijaya\",\"reviewer_name\":\"Santi Putri\",\"start_date\":\"%s\"}\n\n" +
                        
                        "User: \"list review yang terjadwal\"\n" +
                        "JSON: {\"intent\":\"list_review_terjadwal\"}\n\n" +
                        
                        "User: \"update skor review REV-001 jadi 85\"\n" +
                        "JSON: {\"intent\":\"update_skor_review\",\"review_id\":\"REV-001\",\"score\":85}\n\n" +
                        
                        "User: \"batalkan review REV-002\"\n" +
                        "JSON: {\"intent\":\"batalkan_review\",\"review_id\":\"REV-002\"}\n\n" +
                        
                        "User: \"submit hasil review REV-001 dengan skor 90\"\n" +
                        "JSON: {\"intent\":\"submit_hasil_review\",\"review_id\":\"REV-001\",\"score\":90}\n\n" +
                        
                        "User: \"riwayat review rina\"\n" +
                        "JSON: {\"intent\":\"history_review\",\"employee_name\":\"Rina Wijaya\"}\n\n" +
                        
                        "RESPONS: Berikan HANYA JSON, tanpa penjelasan atau teks lain.",
                today,
                userCommand,
                today.plusDays(1),
                today.plusDays(2),
                getNextFriday(today)
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
