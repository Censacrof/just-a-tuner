package com.censacrof.justatuner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.time.Duration;
import java.time.Instant;

public class TunerView extends View {
    private final TextPaint textPaint;
    private final TextPaint sharpPaint;
    private final Paint precisionLinePaint;

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

        precisionLinePaint = new Paint();
        precisionLinePaint.setStrokeWidth(10);
        precisionLinePaint.setStrokeCap(Paint.Cap.ROUND);

        if (isInEditMode())
            return;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        postInvalidateOnAnimation();

        canvas.drawARGB(255, 255, 255, 255);

        drawNotesCircle(canvas);
        drawPrecisionLevel(canvas);

        if (!isInEditMode())
            stepsFromA4 += 0.025;
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
        }
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
        float fractStepsFromA4 = stepsFromA4 - (float) Math.floor(stepsFromA4);

        float py = lineTop;
        if (fractStepsFromA4 < 0.5) {
            py += fractStepsFromA4 * lineLength;
        } else {
            py += fractStepsFromA4 * lineLength;
        }

        canvas.drawCircle(WIDTH_HALF, py, 20, sharpPaint);
    }
}
