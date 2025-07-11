package evoplan.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProfanityFilter {
    private static final Set<String> PROFANITY_LIST = new HashSet<>(Arrays.asList(
            // English Profanity
            "fuck", "shit", "damn", "ass", "bitch", "bastard", "cunt", "dick", "piss", "cock", "prick",
            "motherfucker", "whore", "slut", "twat", "wanker", "fucker", "nigger", "chink", "spic", "kike",
            "asshole", "bullshit", "dumbass", "jackass", "son of a bitch", "retard", "pussy", "dipshit",

            // French Profanity
            "merde", "putain", "salope", "connard", "con", "enculé", "bordel", "pute", "bâtard", "chier",
            "nique", "fils de pute", "branleur", "ta gueule", "dégage", "cul", "enculée", "couille",
            "trou du cul", "fdp", "ntm", "pd", "sac à merde", "gouine", "tapette", "salo", "bite", "zizi",

            // Tunisian Dialect Profanity
            "zebi", "tabouna", "nik", "ahbal", "hayem", "zorra", "mkassar", "mleh", "boussakh", "hallouf",
            "khra", "ikssir", "kosom", "ta9foun", "tzezni", "mchichi", "sorri", "3ahira", "3aouja",
            "yazit", "zamel", "kouni", "rkhis", "meds", "nik omek", "fekrek", "etla3", "dharba", "hmar",

            // Variations and Leetspeak
            "f*ck", "sh1t", "d4mn", "a$$", "b!tch", "c0ck", "fuk", "b4stard", "m0therfucker", "p1ss",
            "p3nis", "n!gger", "ch!nk", "sp!c", "k!ke", "b!te", "encul3", "put1n", "s@lope",
            "tr0u du cul", "z3bi", "t@bouna", "t9foun", "nik0mek"
    ));

    public static String filterText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder filtered = new StringBuilder();

        for (String word : words) {
            String lowerWord = word.toLowerCase();
            String cleanWord = word;

            // Check if the word contains any profanity
            for (String profanity : PROFANITY_LIST) {
                if (lowerWord.contains(profanity)) {
                    // Replace the profanity with asterisks
                    cleanWord = "*".repeat(word.length());
                    break;
                }
            }

            filtered.append(cleanWord).append(" ");
        }

        return filtered.toString().trim();
    }
}