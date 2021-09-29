package com.censacrof.justatuner;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;

import java.time.Duration;
import java.time.Instant;

public class TunerView extends View {
    private final String TAG = "TunerView";

    private ValueAnimator merryGoRoundAnimator;

    private final TextPaint textPaint;
    private final TextPaint sharpPaint;
    private final Paint linePaint;
    private final Paint precisionLinePaint;
    private final Paint precisionCirclePaint;
    private final Paint precisionArrowPaint;

    private Paint backgroundPaint;
    float stepsFromA4 = 0;

    public TunerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        Resources res = getResources();

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        sharpPaint = new TextPaint(textPaint);
        sharpPaint.setColor(Color.argb(255, 125, 125, 125));

        linePaint = new Paint();
        linePaint.setStrokeWidth(5);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(Color.BLACK);

        precisionLinePaint = new Paint(linePaint);
        precisionLinePaint.setColor(Color.argb(255, 125, 125, 125));

        precisionArrowPaint = new Paint();
        precisionArrowPaint.setAntiAlias(true);
        precisionArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        precisionArrowPaint.setColor(Color.BLACK);

        precisionCirclePaint = new Paint();
        precisionCirclePaint.setColor(Color.BLACK);

        merryGoRoundAnimator = ValueAnimator.ofFloat(0f, Tuner.SCALE_NOTE_NAMES.length);
        merryGoRoundAnimator.setDuration(10000);
        merryGoRoundAnimator.setRepeatCount(Animation.INFINITE);
        merryGoRoundAnimator.addUpdateListener((ValueAnimator updatedAnimation) -> {
            stepsFromA4 = (float) updatedAnimation.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        merryGoRoundAnimator.start();

        if (isInEditMode())
            return;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawARGB(255, 255, 255, 255);

        drawNotesCircle(canvas);
        drawPrecisionLevel(canvas);

        if (!isInEditMode())
            stepsFromA4 += 0.0025;
    }

    private void drawNotesCircle(Canvas canvas) {
        float WIDTH = getWidth();
        float HEIGHT = getHeight();

        float WIDTH_HALF = WIDTH / 2f;
        float HEIGHT_HALF = HEIGHT / 2f;

        float maxTextSize = WIDTH / 4f;
        float minTextSize = WIDTH / 10f;
        float diffHalfTextSize = (maxTextSize - minTextSize) / 2.0f;

        float radius = WIDTH_HALF * 1.2f;
        float dTheta = ((float) Math.PI * 2f) / Tuner.SCALE_NOTE_NAMES.length;
        float tiltFactor = 0.3f;
        float phase = (float) stepsFromA4 * dTheta;
        for (int i = 0; i < Tuner.SCALE_NOTE_NAMES.length; i++) {
            //
            float px = WIDTH_HALF + (float) Math.sin(i * dTheta + phase) * radius;

            float yCoefficient = (float) Math.cos(i * dTheta+ phase);
            float py = HEIGHT_HALF + yCoefficient * radius * tiltFactor;

            // canvas.drawCircle(px, py, 10, textPaint);
            int sharpIndex = Tuner.SCALE_NOTE_NAMES[i].indexOf("#");
            boolean isSharp = sharpIndex != -1;
            String noteName = isSharp
                    ? Tuner.SCALE_NOTE_NAMES[i].substring(0, sharpIndex)
                    : Tuner.SCALE_NOTE_NAMES[i];

            float textSize = minTextSize + diffHalfTextSize + diffHalfTextSize * yCoefficient;
            textPaint.setTextSize(textSize);
            canvas.drawText(noteName, px, py, textPaint);
            if (isSharp) {
                sharpPaint.setTextSize(textSize / 2f);
                canvas.drawText("#", px + textSize / 2.5f, py - textSize / 2f, sharpPaint);
            }

            float y1 = py + textSize * 0.25f;
            float y2 = y1 + textSize * 0.1f;
            canvas.drawLine(px, y1, px, y2, precisionLinePaint);
        }

        Path path = new Path();
        path.moveTo(WIDTH_HALF, HEIGHT_HALF + maxTextSize * 1.3f);
        path.lineTo(WIDTH_HALF - 10f, HEIGHT_HALF + maxTextSize * 1.6f);
        path.lineTo(WIDTH_HALF + 10f, HEIGHT_HALF + maxTextSize * 1.6f);
        path.close();
        canvas.drawPath(path, precisionArrowPaint);
    }

    private void drawPrecisionLevel(Canvas canvas) {
        float WIDTH = getWidth();
        float HEIGHT = getHeight();

        float WIDTH_HALF = WIDTH / 2f;
        float HEIGHT_HALF = HEIGHT / 2f;

        float lineLength = HEIGHT / 5f;
        float lineCenter = HEIGHT / 5f;
        float lineTop = lineCenter - lineLength / 2f;
        float linetBottom = lineCenter + lineLength / 2f;

        canvas.drawLine(WIDTH_HALF, lineTop, WIDTH_HALF, linetBottom, precisionLinePaint);
        float fractStepsFromA4 = 0.5f + stepsFromA4 - (float) Math.floor(0.5 + stepsFromA4);

        float py = lineTop;
        if (fractStepsFromA4 < 0.5) {
            py += fractStepsFromA4 * lineLength;
        } else {
            py += fractStepsFromA4 * lineLength;
        }


        float alpha = 255 * (1 - 2 * Math.abs(fractStepsFromA4 - 0.5f));
        Log.d(TAG, "Alpha " + alpha);
        precisionCirclePaint.setAlpha((int) alpha);
        canvas.drawCircle(WIDTH_HALF, py, 20, precisionCirclePaint);

        Path arrowPath = new Path();
        arrowPath.moveTo(WIDTH_HALF - 40, lineCenter);
        arrowPath.lineTo(WIDTH_HALF - 70, lineCenter - 10);
        arrowPath.lineTo(WIDTH_HALF - 70, lineCenter + 10);
        arrowPath.close();
        canvas.drawPath(arrowPath, precisionArrowPaint);

        arrowPath = new Path();
        arrowPath.moveTo(WIDTH_HALF + 40, lineCenter);
        arrowPath.lineTo(WIDTH_HALF + 70, lineCenter - 10);
        arrowPath.lineTo(WIDTH_HALF + 70, lineCenter + 10);
        arrowPath.close();
        canvas.drawPath(arrowPath, precisionArrowPaint);
    }
}
