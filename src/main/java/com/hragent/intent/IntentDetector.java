package com.hragent.intent;

import java.util.Arrays;
import java.util.List;

// IntentDetector: Menentukan apakah input adalah QUESTION atau COMMAND
public class IntentDetector {
    
    private static final List<String> QUESTION_KEYWORDS = Arrays.asList(
        "siapa", "apa", "berapa", "kapan", "dimana", "mana", 
        "bagaimana", "kenapa", "sisa", "status", "email", "jabatan"
    );
    
    private static final List<String> COMMAND_KEYWORDS = Arrays.asList(
        "ajukan", "apply", "tolong", "jadwalkan", "schedule", "buat", 
        "bikinin", "set", "atur", "submit", "cek status", "check"
    );

    public IntentType detect(String input) {
        String lower = input.toLowerCase().trim();
        
        // Hitung keyword hits
        int questionHits = countHits(lower, QUESTION_KEYWORDS);
        int commandHits = countHits(lower, COMMAND_KEYWORDS);
        
        // Special case: "cek status" adalah command, bukan question
        if (lower.contains("cek") || lower.contains("check")) {
            return IntentType.COMMAND;
        }
        
        // Jika ada tanda tanya, kemungkinan besar question
        if (lower.contains("?")) {
            return IntentType.QUESTION;
        }
        
        // Decision based on hits
        if (questionHits > commandHits) {
            return IntentType.QUESTION;
        } else if (commandHits > 0) {
            return IntentType.COMMAND;
        }
        
        // Default: jika mengandung kata kerja aksi, anggap command
        if (containsActionVerb(lower)) {
            return IntentType.COMMAND;
        }
        
        // Default: question jika mengandung question word
        if (questionHits > 0) {
            return IntentType.QUESTION;
        }
        
        return IntentType.UNKNOWN;
    }
    
    private int countHits(String text, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }
    
    private boolean containsActionVerb(String text) {
        String[] actionVerbs = {"ajukan", "apply", "jadwalkan", "schedule", "buat", "submit"};
        for (String verb : actionVerbs) {
            if (text.contains(verb)) {
                return true;
            }
        }
        return false;
    }
}
