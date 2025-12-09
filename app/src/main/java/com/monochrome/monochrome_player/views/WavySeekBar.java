package com.monochrome.monochrome_player.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Simple WavySeekBar implemented in Java.
 * - Draws a background track
 * - Draws an animated sine wave
 * - Draws a thumb representing progress
 * - Supports touch to seek and listener callbacks
 */
public class WavySeekBar extends View {

    private float progress = 0f; // 0..1
    private Paint trackPaint;
    private Paint wavePaint;
    private Paint thumbPaint;
    private Path wavePath = new Path();

    private float waveLengthPx;
    private float waveHeightPx;
    private float waveThicknessPx;
    private float trackThicknessPx;

    private ValueAnimator phaseAnimator;
    private float phase = 0f;

    private OnSeekChangeListener listener;

    public WavySeekBar(Context context) { this(context, null); }

    public WavySeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(0xFF2B2B2B);

        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setColor(0xFFE91E63); // accent pinkish as default

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(0xFFE91E63);

        waveLengthPx = dpToPx(14);
        waveHeightPx = dpToPx(34);
        waveThicknessPx = dpToPx(4);
        trackThicknessPx = dpToPx(13);
        wavePaint.setStrokeWidth(waveThicknessPx);

        phaseAnimator = ValueAnimator.ofFloat(0f, 1f);
        phaseAnimator.setDuration(1200);
        phaseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        phaseAnimator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
        phaseAnimator.start();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (phaseAnimator != null) phaseAnimator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float cy = h / 2f;

        // draw track
        float halfTrack = trackThicknessPx / 2f;
        canvas.drawRoundRect(0, cy - halfTrack, w, cy + halfTrack, halfTrack, halfTrack, trackPaint);

        // draw wave path
        wavePath.reset();
        int steps = Math.max(50, w / 4);
        float phaseShift = phase * waveLengthPx * 2f;
        for (int i = 0; i <= steps; i++) {
            float x = i * (w / (float) steps);
            float theta = (x + phaseShift) / waveLengthPx * 2f * (float) Math.PI;
            float y = cy + (float) Math.sin(theta) * (waveHeightPx / 2f);
            if (i == 0) wavePath.moveTo(x, y); else wavePath.lineTo(x, y);
        }
        canvas.drawPath(wavePath, wavePaint);

        // draw thumb
        float thumbX = progress * w;
        float thumbRadius = Math.max(8f, trackThicknessPx * 0.9f);
        canvas.drawCircle(thumbX, cy, thumbRadius, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                float x = event.getX();
                float frac = (getWidth() > 0) ? (x / getWidth()) : 0f;
                frac = Math.max(0f, Math.min(1f, frac));
                setProgress(frac, true);
                if (event.getActionMasked() == MotionEvent.ACTION_UP) performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setProgress(float p) { setProgress(p, false); }

    public void setProgress(float p, boolean fromUser) {
        this.progress = Math.max(0f, Math.min(1f, p));
        if (listener != null) listener.onProgressChanged(this.progress, fromUser);
        invalidate();
    }

    public float getProgress() { return progress; }

    public void setOnSeekChangeListener(OnSeekChangeListener l) { this.listener = l; }

    public interface OnSeekChangeListener {
        void onProgressChanged(float progress, boolean fromUser);
    }
}
