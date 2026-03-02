package com.monochrome.monochrome_player;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public final class WaveformFeatureExtractor {

    public static final class Features {
        public final float rms;
        public final float zcr;
        public final float roughness;
        public final float peak;

        public Features(float rms, float zcr, float roughness, float peak) {
            this.rms = rms;
            this.zcr = zcr;
            this.roughness = roughness;
            this.peak = peak;
        }
    }

    private static final int MAX_SAMPLES = 16000 * 6;
    private static final long TIMEOUT_US = 10000;

    private WaveformFeatureExtractor() {
    }

    public static Features extract(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;

        MediaExtractor extractor = new MediaExtractor();
        MediaCodec codec = null;

        try {
            extractor.setDataSource(filePath);
            int audioTrackIndex = findAudioTrack(extractor);
            if (audioTrackIndex < 0) return null;

            extractor.selectTrack(audioTrackIndex);
            MediaFormat format = extractor.getTrackFormat(audioTrackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime == null) return null;

            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            float sumSq = 0f;
            float diffSq = 0f;
            float peak = 0f;
            int zeroCrossings = 0;
            int total = 0;
            float prev = 0f;
            boolean hasPrev = false;

            boolean inputDone = false;
            boolean outputDone = false;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (!outputDone && total < MAX_SAMPLES) {
                if (!inputDone) {
                    int inputIndex = codec.dequeueInputBuffer(TIMEOUT_US);
                    if (inputIndex >= 0) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                        if (inputBuffer == null) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                inputDone = true;
                            } else {
                                long presentationTimeUs = extractor.getSampleTime();
                                codec.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0);
                                extractor.advance();
                            }
                        }
                    }
                }

                int outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                if (outputIndex >= 0) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                        int bytesToProcess = Math.min(outputBuffer.remaining(), (MAX_SAMPLES - total) * 2);
                        int sampleCount = bytesToProcess / 2;

                        for (int i = 0; i < sampleCount; i++) {
                            short sampleShort = outputBuffer.getShort();
                            float sample = sampleShort / 32768f;

                            sumSq += sample * sample;
                            peak = Math.max(peak, Math.abs(sample));

                            if (hasPrev) {
                                if ((prev >= 0f && sample < 0f) || (prev < 0f && sample >= 0f)) {
                                    zeroCrossings++;
                                }
                                float d = sample - prev;
                                diffSq += d * d;
                            }

                            prev = sample;
                            hasPrev = true;
                            total++;

                            if (total >= MAX_SAMPLES) {
                                break;
                            }
                        }
                    }

                    codec.releaseOutputBuffer(outputIndex, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED || outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                }
            }

            if (total <= 1) return null;

            float rms = (float) Math.sqrt(sumSq / total);
            float zcr = zeroCrossings / (float) (total - 1);
            float roughness = (float) Math.sqrt(diffSq / (total - 1));

            return new Features(rms, zcr, roughness, peak);
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                extractor.release();
            } catch (Exception ignored) {
            }
            if (codec != null) {
                try {
                    codec.stop();
                } catch (Exception ignored) {
                }
                try {
                    codec.release();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static int findAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }
}
