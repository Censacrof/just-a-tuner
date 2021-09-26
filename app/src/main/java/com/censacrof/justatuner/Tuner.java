package com.censacrof.justatuner;

import com.github.psambit9791.jdsp.signal.CrossCorrelation;
import com.github.psambit9791.jdsp.signal.Decimate;

import java.util.Locale;

public class Tuner {
    private final String TAG = "Tuner";

    private Note note;
    private double[] autoCorrelated;

    private final double A4_FREQ = 440.0;
    private final double BASE = Math.pow(2, 1d/12d);

    private final String[] SCALE_NOTE_NAMES = {
        "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"
    };

    private final int N_OCTAVES = 8;
    private final Note[] NOTES = new Note[N_OCTAVES * SCALE_NOTE_NAMES.length];

    public class Note {
        public final String name;
        public final double frequency;
        public final double error;

        public Note(String name, double frequency) {
            this(name, frequency, 0);
        }

        public Note(Note other, double error) {
            this(other.name, other.frequency, error);
        }

        public Note(String name, double frequency, double error) {
            this.name = name;
            this.frequency = frequency;
            this.error = error;
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

    private double noteToFrequency(int stepsFromA4) {
        return A4_FREQ * Math.pow(BASE, stepsFromA4);
    }

    public Note identifyNote(double frequency) {
        for (int i = 0; i < NOTES.length - 1; i++) {
            Note lower = NOTES[i];
            Note upper = NOTES[i + 1];

            if (lower.frequency <= frequency && frequency <= upper.frequency) {
                double steps = Math.log(frequency / A4_FREQ) / Math.log(BASE);
                double fract = steps - Math.floor(steps);

                if (fract < 0.5)
                    return new Note(lower, fract);
                else
                    return new Note(upper, fract - 1);
            }
        }

        return null;
    }

    public Tuner() {
        for (int i = 0; i < NOTES.length; i++) {
            int stepsFromA4 = i - NOTES.length / 2;

            NOTES[i] =  new Note(
                    SCALE_NOTE_NAMES[Math.floorMod(stepsFromA4, SCALE_NOTE_NAMES.length)]
                            + (4 + (stepsFromA4 - 3)/ SCALE_NOTE_NAMES.length),
                    noteToFrequency(stepsFromA4)
            );
        }
    }

    public Note getNote() {
        return note;
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

        double frequency = 1f / (float) highestPeakPeriod;
        note = identifyNote(frequency);
    }
}
