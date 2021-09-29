package com.censacrof.justatuner;

import com.github.psambit9791.jdsp.signal.CrossCorrelation;
import com.github.psambit9791.jdsp.signal.Decimate;

import java.util.Locale;

public class Tuner {
    private final String TAG = "Tuner";

    private Tone tone;
    private double[] autoCorrelated;

    private static final double A4_FREQ = 440.0;
    private static final double BASE = Math.pow(2, 1d/12d);

    public static final String[] SCALE_NOTE_NAMES = {
        "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"
    };

    private final int N_OCTAVES = 8;
    private final Tone[] NOTES = new Tone[N_OCTAVES * SCALE_NOTE_NAMES.length];

    public class Tone {
        public final String name;
        public final double stepsFromA4;
        public final double frequency;
        public final double error;

        public Tone(String name, double frequency) {
            this.name = name;
            this.frequency = frequency;
            this.stepsFromA4 = frequencyToSteps(frequency);
            this.error = stepsFromA4 >= 0
                    ? stepsFromA4 - Math.floor(stepsFromA4)
                    : stepsFromA4 + Math.ceil(stepsFromA4);
        }

        @Override
        public String toString() {
            return String.format(
                    Locale.ENGLISH,
                    "(%s%s: %.2fHz)",
                    this.name,
                    error < -0.25 ? "--":
                            error < -0.1 ? "-":
                            error < 0.1 ? "":
                            error < 0.25 ? "+": "++",
                    this.frequency
            );
        }
    };

    public static double stepsToFrequency(double stepsFromA4) {
        return A4_FREQ * Math.pow(BASE, stepsFromA4);
    }

    public static double frequencyToSteps(double frequency) {
        return Math.log(frequency/A4_FREQ) / Math.log(BASE);
    }

    public Tone identifyNote(double frequency) {
        for (int i = 0; i < NOTES.length - 1; i++) {
            Tone lower = NOTES[i];
            Tone upper = NOTES[i + 1];

            if (lower.frequency <= frequency && frequency <= upper.frequency) {
                double steps = Tuner.frequencyToSteps(frequency);
                double fract = steps - Math.floor(steps);

                if (fract < 0.5)
                    return new Tone(lower.name, frequency);
                else
                    return new Tone(upper.name, frequency);
            }
        }

        return null;
    }

    public Tuner() {
        for (int i = 0; i < NOTES.length; i++) {
            int stepsFromA4 = i - NOTES.length / 2;

            NOTES[i] =  new Tone(
                    SCALE_NOTE_NAMES[Math.floorMod(stepsFromA4, SCALE_NOTE_NAMES.length)]
                            + (4 + (stepsFromA4 - 3)/ SCALE_NOTE_NAMES.length),
                    stepsToFrequency(stepsFromA4)
            );
        }
    }

    public Tone getNote() {
        return tone;
    }

    public double[] getAutoCorrelated() {
        return autoCorrelated;
    }

    public void analyzeChunk(double[] chunk, int sampleRate, int downSamplingFactor) {
        chunk = new Decimate(chunk, sampleRate, true)
                .decimate(downSamplingFactor);

        CrossCorrelation cc = new CrossCorrelation(chunk);
        autoCorrelated = cc.crossCorrelate("full");

        int nPeaksToCheck = 5;
        double highestPeak = 0;
        double highestPeakPeriod = 0;
        int zeroIndex = autoCorrelated.length / 2;
        for (int i = autoCorrelated.length / 2 + 10; i < autoCorrelated.length - 1; i++) {
            if (autoCorrelated[i - 1] < autoCorrelated[i]
                    && autoCorrelated[i + 1]  < autoCorrelated[i]) {
                
                if (autoCorrelated[i] > highestPeak) {
                    highestPeak = autoCorrelated[i];
                    highestPeakPeriod = (i - zeroIndex) * downSamplingFactor / (float) sampleRate;
                    nPeaksToCheck--;
                    if (nPeaksToCheck <= 0)
                        break;
                }
            }
        }

        if (highestPeak < autoCorrelated[autoCorrelated.length / 2] * 0.8)
            return;

        double frequency = 1f / (float) highestPeakPeriod;
        tone = identifyNote(frequency);
    }
}
