package ir.mahozad.multiplatform.wavyslider;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.view.MotionEvent;
import android.view.View;

public class WavySlider extends View {
    public interface OnValueChangeListener {
        Object onValueChange(float oldValue, float newValue);
    }

    private final Paint inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float value = 0f;
    private OnValueChangeListener listener;
    private float waveAmplitude;
    private float waveLength;
    private float wavePhase;
    private float trackHeight;
    private float thumbRadius;
    private float waveStroke;
    private ValueAnimator waveAnimator;
    private boolean waveAnimating = false;
    private static final float TWO_PI = (float) (Math.PI * 2);

    public WavySlider(Context context) {
        this(context, null);
    }

    public WavySlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WavySlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        waveStroke = dp(3f);
        waveAmplitude = dp(8f);
        waveLength = dp(42f);
        trackHeight = dp(8f);
        thumbRadius = dp(10f);
        inactivePaint.setStyle(Paint.Style.FILL);
        inactivePaint.setColor(Color.parseColor("#444444"));
        activePaint.setStyle(Paint.Style.FILL);
        activePaint.setColor(Color.parseColor("#03DAC6"));
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(activePaint.getColor());
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(waveStroke);
        wavePaint.setColor(activePaint.getColor());
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);
        wavePaint.setPathEffect(cornerEffect(dp(12f)));
        waveAnimator = ValueAnimator.ofFloat(0f, 1f);
        waveAnimator.setDuration(1200L);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setRepeatMode(ValueAnimator.RESTART);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(a -> {
            wavePhase = (float) a.getAnimatedValue();
            invalidate();
        });
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public void setValue(float newValue) {
        float clamped = Math.max(0f, Math.min(1f, newValue));
        if (clamped == value) return;
        value = clamped;
        invalidate();
    }

    public float getValue() {
        return value;
    }

    public void setWaveColor(int color) {
        wavePaint.setColor(color);
        invalidate();
    }

    public void setWaveAmplitude(float amplitudePx) {
        waveAmplitude = Math.max(dp(2f), amplitudePx);
        invalidate();
    }

    public void setWaveLength(float lengthPx) {
        waveLength = Math.max(dp(12f), lengthPx);
        invalidate();
    }

    public void setActiveTrackColor(int color) {
        activePaint.setColor(color);
        invalidate();
    }

    public void setInactiveTrackColor(int color) {
        inactivePaint.setColor(color);
        invalidate();
    }

    public void setThumbColor(int color) {
        thumbPaint.setColor(color);
        invalidate();
    }

    public void setWaveStroke(float strokePx) {
        waveStroke = Math.max(dp(1.5f), strokePx);
        wavePaint.setStrokeWidth(waveStroke);
        invalidate();
    }

    public void setTrackHeight(float heightPx) {
        trackHeight = Math.max(dp(4f), heightPx);
        invalidate();
    }

    public void setThumbRadius(float radiusPx) {
        thumbRadius = Math.max(dp(6f), radiusPx);
        invalidate();
    }

    public void setWaveAnimating(boolean running) {
        waveAnimating = running;
        if (waveAnimator == null) return;
        if (running) {
            if (!waveAnimator.isStarted()) waveAnimator.start();
        } else {
            if (waveAnimator.isStarted()) waveAnimator.cancel();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cy = h / 2f;
        float trackH = Math.max(dp(4f), Math.min(trackHeight, h * 0.4f));
        float thumbR = Math.max(dp(6f), Math.min(thumbRadius, h * 0.5f));
        float amplitude = Math.max(dp(2f), Math.min(waveAmplitude, h * 0.45f));
        RectF trackRect = new RectF(0f, cy - trackH / 2f, w, cy + trackH / 2f);
        canvas.drawRoundRect(trackRect, trackH / 2f, trackH / 2f, inactivePaint);
        RectF activeRect = new RectF(0f, cy - trackH / 2f, value * w, cy + trackH / 2f);
        canvas.drawRoundRect(activeRect, trackH / 2f, trackH / 2f, activePaint);
        float waveWidth = Math.max(dp(32f), value * w);
        if (waveWidth > 0f && waveAnimating) {
            Path path = new Path();
            float step = Math.max(dp(2f), waveLength / 16f);
            int steps = Math.max(32, (int) (waveWidth / step));
            for (int i = 0; i <= steps; i++) {
                float x = (i / (float) steps) * waveWidth;
                float theta = (x / waveLength + wavePhase) * TWO_PI;
                float y = cy + (float) Math.sin(theta) * amplitude;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            canvas.drawPath(path, wavePaint);
        }
        canvas.drawCircle(value * w, cy, thumbR, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float newValue = event.getX() / Math.max(1f, getWidth());
            newValue = Math.max(0f, Math.min(1f, newValue));
            float old = value;
            if (old != newValue) {
                value = newValue;
                invalidate();
                if (listener != null) listener.onValueChange(old, newValue);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private PathEffect cornerEffect(float radius) {
        return new CornerPathEffect(radius);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (waveAnimator != null && waveAnimating && !waveAnimator.isStarted()) waveAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (waveAnimator != null) waveAnimator.cancel();
        super.onDetachedFromWindow();
    }
}
