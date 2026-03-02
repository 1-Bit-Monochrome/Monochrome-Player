package com.monochrome.monochrome_player;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import java.util.Locale;

public class GenreClassifier {
    public static final String UNKNOWN_GENRE = "Unknown";
    private final LocalModelGenreEngine localModelGenreEngine;
    private final GenreLabelMapper genreLabelMapper = new GenreLabelMapper();

    public GenreClassifier(Context context) {
        this.localModelGenreEngine = new LocalModelGenreEngine(context.getApplicationContext());
    }

    public boolean isLocalModelReady() {
        return localModelGenreEngine != null && localModelGenreEngine.isReady();
    }

    public String classify(Song song) {
        if (song == null) return UNKNOWN_GENRE;

        String modelPrediction = localModelGenreEngine.classify(song);
        if (modelPrediction != null && !modelPrediction.trim().isEmpty()) {
            String mapped = genreLabelMapper.mapToGenre(modelPrediction);
            if (mapped != null && !mapped.trim().isEmpty()) {
                return normalize(mapped);
            }
            return normalize(modelPrediction);
        }

        String embeddedGenre = readEmbeddedGenre(song.getPath());
        if (embeddedGenre != null && !embeddedGenre.isEmpty()) {
            return normalize(embeddedGenre);
        }

        String inferred = inferByKeywords(song);
        if (inferred != null && !inferred.isEmpty()) {
            return inferred;
        }

        return UNKNOWN_GENRE;
    }

    private String readEmbeddedGenre(String path) {
        if (path == null || path.isEmpty()) return null;
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(path);
            return metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                metadataRetriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private String inferByKeywords(Song song) {
        String text = ((safe(song.getTitle()) + " " + safe(song.getArtist()) + " " + safe(song.getAlbum()) + " " + safe(song.getPath()))
                .toLowerCase(Locale.ROOT));

        if (containsAny(text, "metal", "thrash", "deathcore", "black metal", "doom")) return "Metal";
        if (containsAny(text, "hip hop", "hiphop", "rap", "trap", "drill")) return "Hip-Hop";
        if (containsAny(text, "edm", "electro", "techno", "house", "trance", "dnb", "dubstep")) return "Electronic";
        if (containsAny(text, "rock", "grunge", "punk", "indie rock", "hard rock")) return "Rock";
        if (containsAny(text, "pop", "synthpop", "dance pop", "k-pop", "j-pop")) return "Pop";
        if (containsAny(text, "jazz", "blues", "swing", "bebop")) return "Jazz/Blues";
        if (containsAny(text, "classical", "orchestra", "symphony", "piano sonata")) return "Classical";
        if (containsAny(text, "ambient", "lofi", "lo-fi", "chill", "meditation")) return "Ambient/Chill";
        if (containsAny(text, "r&b", "rnb", "soul", "funk")) return "R&B/Soul";
        if (containsAny(text, "country", "folk", "bluegrass")) return "Country/Folk";

        return null;
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) return true;
        }
        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String rawGenre) {
        String trimmed = rawGenre == null ? "" : rawGenre.trim();
        if (trimmed.isEmpty()) return UNKNOWN_GENRE;
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
