package com.monochrome.monochrome_player;

import java.util.Locale;

public class GenreLabelMapper {

    public String mapToGenre(String rawLabel) {
        if (rawLabel == null) return null;
        String value = rawLabel.trim();
        if (value.isEmpty()) return null;

        String lower = value.toLowerCase(Locale.ROOT);

        if (containsAny(lower, "heavy metal", "death metal", "black metal", "thrash", "hard rock")) return "Metal";
        if (containsAny(lower, "rock music", "punk rock", "grunge", "indie rock", "alternative rock", "progressive rock")) return "Rock";
        if (containsAny(lower, "hip hop", "rap", "trap", "drill", "beatboxing")) return "Hip-Hop";
        if (containsAny(lower, "electronic music", "techno", "house music", "trance music", "dubstep", "drum and bass", "synthesizer")) return "Electronic";
        if (containsAny(lower, "pop music", "dance music", "synth-pop", "k-pop", "j-pop")) return "Pop";
        if (containsAny(lower, "jazz", "swing music", "bebop", "blues")) return "Jazz/Blues";
        if (containsAny(lower, "classical music", "orchestra", "violin", "cello", "opera")) return "Classical";
        if (containsAny(lower, "ambient music", "new-age music", "meditation", "drone")) return "Ambient/Chill";
        if (containsAny(lower, "soul music", "r&b", "funk")) return "R&B/Soul";
        if (containsAny(lower, "country", "bluegrass", "folk music")) return "Country/Folk";

        if (containsAny(lower, "music", "song", "singing")) return "Other Music";

        return null;
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) return true;
        }
        return false;
    }
}
