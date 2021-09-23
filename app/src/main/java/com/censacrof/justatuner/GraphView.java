package com.censacrof.justatuner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphView extends View {
    private ArrayList<Float> samples;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        samples = new ArrayList<>();

        if (isInEditMode()) {
            samples.add(0.1f);
            samples.add(-0.5f);
            samples.add(0.3f);
            samples.add(0.0f);
            samples.add(1.0f);
            samples.add(0.4f);
            samples.add(0.3f);
            samples.add(0.4f);
            samples.add(1f);
            samples.add(-0.4f);
        }
    }

    private Paint linePaint;
    private Paint axisPaint;
    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setARGB(255, 0, 50, 125);
        linePaint.setStrokeWidth(5);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setARGB(255, 0, 0, 0);
        axisPaint.setStrokeWidth(10);
    }

    public void setSamples(ArrayList<Float> samples) {
        this.samples = samples;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int WIDTH = getWidth();
        final int HEIGHT = getHeight();
        final float MAX_Y = (HEIGHT * 0.9f) / 2f;

        float dx = WIDTH / (float) (samples.size() - 1);
        canvas.translate(0, HEIGHT / 2f);
        for (int i = 0; i < samples.size() - 1; i++) {
            canvas.drawLine(
                    dx * i, -samples.get(i) * MAX_Y,
                    dx * (i + 1), -samples.get(i + 1) * MAX_Y,
                    linePaint
            );
        }

        canvas.drawLine(0, 0, WIDTH, 0, axisPaint);
    }
}
