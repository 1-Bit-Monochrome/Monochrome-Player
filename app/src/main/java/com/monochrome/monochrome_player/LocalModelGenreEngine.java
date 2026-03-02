package com.monochrome.monochrome_player;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalModelGenreEngine {
    private static final String TAG = "LocalModelGenreEngine";
    private static final String MODEL_DIR = "models";
    private static final String MODEL_FILE = "genre_small.tflite";
    private static final String LABELS_FILE = "genre_labels.txt";

    private Interpreter interpreter;
    private final List<String> labels = new ArrayList<>();

    public LocalModelGenreEngine(Context context) {
        initialize(context);
    }

    public boolean isReady() {
        return interpreter != null && !labels.isEmpty();
    }

    public String classify(Song song) {
        if (song == null || !isReady()) return null;
        try {
            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();
            if (inputShape.length != 2 || outputShape.length != 2) return null;

            int featureSize = inputShape[1];
            int classCount = outputShape[1];
            if (featureSize <= 0 || classCount <= 0) return null;

            float[][] input = new float[1][featureSize];
            input[0] = buildFeatureVector(song, featureSize);

            float[][] output = new float[1][classCount];
            interpreter.run(input, output);

            int bestIndex = argmax(output[0]);
            if (bestIndex < 0 || bestIndex >= labels.size()) return null;
            return labels.get(bestIndex);
        } catch (Exception e) {
            Log.w(TAG, "Local model classify failed", e);
            return null;
        }
    }

    private void initialize(Context context) {
        try {
            BundledModelInstaller.installIfPresent(context, MODEL_FILE, LABELS_FILE);

            File modelDir = new File(context.getFilesDir(), MODEL_DIR);
            File modelFile = new File(modelDir, MODEL_FILE);
            File labelsFile = new File(modelDir, LABELS_FILE);

            if (!modelFile.exists() || !labelsFile.exists()) {
                Log.i(TAG, "Local genre model not found at " + modelFile.getAbsolutePath());
                return;
            }

            interpreter = new Interpreter(loadModelFile(modelFile));
            labels.addAll(readLabels(labelsFile));

            int[] outputShape = interpreter.getOutputTensor(0).shape();
            if (outputShape.length == 2 && outputShape[1] > 0 && labels.size() != outputShape[1]) {
                labels.clear();
                for (int i = 0; i < outputShape[1]; i++) {
                    labels.add("Genre " + (i + 1));
                }
            }

            if (labels.isEmpty()) {
                interpreter.close();
                interpreter = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Local model init failed", e);
            if (interpreter != null) {
                interpreter.close();
                interpreter = null;
            }
            labels.clear();
        }
    }

    private MappedByteBuffer loadModelFile(File modelFile) throws Exception {
        FileInputStream inputStream = new FileInputStream(modelFile);
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = 0;
        long declaredLength = fileChannel.size();
        MappedByteBuffer mapped = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        fileChannel.close();
        inputStream.close();
        return mapped;
    }

    private List<String> readLabels(File labelsFile) throws Exception {
        List<String> out = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(labelsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        reader.close();
        return out;
    }

    private float[] buildFeatureVector(Song song, int featureSize) {
        float[] vector = new float[featureSize];
        String text = (safe(song.getTitle()) + " " + safe(song.getArtist()) + " " + safe(song.getAlbum()) + " " + safe(song.getPath()))
                .toLowerCase(Locale.ROOT);

        String[] tokens = text.split("[^a-z0-9]+");
        for (String token : tokens) {
            if (token == null || token.isEmpty()) continue;
            int slot = Math.floorMod(token.hashCode(), featureSize);
            vector[slot] += 1.0f;
        }

        float durationSeconds = extractDurationSeconds(song.getPath());
        if (featureSize >= 4) {
            vector[0] = durationSeconds / 600.0f;
            vector[1] = safe(song.getArtist()).isEmpty() ? 0f : 1f;
            vector[2] = safe(song.getAlbum()).isEmpty() ? 0f : 1f;
            vector[3] = safe(song.getTitle()).length() / 120.0f;
        }

        WaveformFeatureExtractor.Features waveform = WaveformFeatureExtractor.extract(song.getPath());
        if (waveform != null && featureSize >= 8) {
            vector[4] = waveform.rms;
            vector[5] = waveform.zcr;
            vector[6] = waveform.roughness;
            vector[7] = waveform.peak;
        }

        return normalizeL2(vector);
    }

    private float extractDurationSeconds(String path) {
        if (path == null || path.isEmpty()) return 0f;
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(path);
            String ms = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (ms == null) return 0f;
            return Float.parseFloat(ms) / 1000f;
        } catch (Exception ignored) {
            return 0f;
        } finally {
            try {
                metadataRetriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private float[] normalizeL2(float[] values) {
        float sumSq = 0f;
        for (float v : values) sumSq += v * v;
        if (sumSq <= 0f) return values;
        float invNorm = (float) (1.0 / Math.sqrt(sumSq));
        for (int i = 0; i < values.length; i++) values[i] *= invNorm;
        return values;
    }

    private int argmax(float[] values) {
        if (values == null || values.length == 0) return -1;
        int best = 0;
        float bestVal = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > bestVal) {
                bestVal = values[i];
                best = i;
            }
        }
        return best;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
