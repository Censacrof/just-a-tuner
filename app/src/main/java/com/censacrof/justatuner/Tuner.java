package com.censacrof.justatuner;

import android.util.Log;

import com.github.psambit9791.jdsp.signal.CrossCorrelation;
import com.github.psambit9791.jdsp.signal.Decimate;

public class Tuner {
    private final String TAG = "Tuner";

    private float frequency;
    private double[] autoCorrelated;

    public float getFrequency() {
        return frequency;
    }

    public double[] getAutoCorrelated() {
        return autoCorrelated;
    }

    public void analyzeChunk(double[] chunk, int sampleRate, int downSamplingFactor) {
        chunk = new Decimate(chunk, sampleRate, true)
                .decimate(downSamplingFactor);

        CrossCorrelation cc = new CrossCorrelation(chunk);
        autoCorrelated = cc.crossCorrelate("full");

        double highestPeak = 0;
        double highestPeakPeriod = 0;
        int zeroIndex = autoCorrelated.length / 2;
        for (int i = autoCorrelated.length / 2 + 10; i < autoCorrelated.length - 1; i++) {
            if (autoCorrelated[i - 1] < autoCorrelated[i]
                    && autoCorrelated[i + 1]  < autoCorrelated[i]) {
                
                if (autoCorrelated[i] > highestPeak) {
                    highestPeak = autoCorrelated[i];
                    highestPeakPeriod = (i - zeroIndex) * downSamplingFactor / (float) sampleRate;
                }
            }
        }

        frequency = 1f / (float) highestPeakPeriod;
    }
}
