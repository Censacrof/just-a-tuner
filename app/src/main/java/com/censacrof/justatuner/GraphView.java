package com.censacrof.justatuner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphView
        extends SurfaceView
        implements Runnable, SurfaceHolder.Callback {
    final String TAG = "GraphView";
    private int sampleRate;

    private Paint linePaint;
    private Paint axisPaint;

    private Thread drawingThread;
    private SurfaceHolder surfaceHolder;
    private boolean canDraw = false;
    private double[] samples;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setAlpha(255);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setARGB(255, 0, 0, 0);
        axisPaint.setStrokeWidth(10);
    }

    private void init(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    public void setSamples(double[] chunk) {
        samples = chunk;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        this.surfaceHolder = holder;

        // if drawingThread is already running make it stop
        if (drawingThread != null) {
            canDraw = false;
            try {
                drawingThread.join();
            } catch (InterruptedException ignored) {}
        }

        canDraw = true;
        drawingThread = new Thread(this);
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
               int format,
               int width,
               int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        canDraw = false;
    }

    private float angle = 0f;
    @Override
    public void run() {

        while (canDraw && !Thread.currentThread().isInterrupted()) {
            try {
                Canvas canvas;
                while (!surfaceHolder.getSurface().isValid());
                canvas = surfaceHolder.lockCanvas();
                if (canvas == null)
                    return;

                canvas.drawARGB(255, 255, 255, 255);

                float WIDTH = canvas.getWidth();
                float HEIGHT = canvas.getHeight();

                float d = 50f;
                float px = WIDTH / 2f + d * (float) Math.cos(angle);
                float py = HEIGHT / 2f + d * (float) Math.sin(angle);

                float r1 = 20f;
                float r2 = 5f;

                canvas.drawCircle(px, py, r1, linePaint);

                // x axis
                canvas.drawLine(
                        0,
                        HEIGHT / 2,
                        WIDTH,
                        HEIGHT / 2,
                        axisPaint
                );

                // samples
                ArrayList<Float> list = new ArrayList<>(Arrays.asList(
                        -0.5f,
                        1.0f,
                        -1.0f,
                        0.5f
                ));

                float maxY = 0.9f * HEIGHT / 2f;
                if (samples != null && samples.length > 1) {
                    float dx = WIDTH / (samples.length - 1);

                    canvas.translate(0, HEIGHT/2f);
                    Path path = new Path();
                    path.moveTo(0, maxY * (float) samples[0]);
                    for (int i = 1; i < samples.length; i++) {
                        path.lineTo(dx * i, maxY * (float) samples[i]);
                    }
                    canvas.drawPath(path, linePaint);
                }

                angle += 0.1f;
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (NullPointerException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}
