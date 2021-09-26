package com.censacrof.justatuner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.github.psambit9791.jdsp.signal.CrossCorrelation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class AudioFetcher {
    private final String TAG = "AudioFetcher";

    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.UNPROCESSED;
    public final static int SAMPLE_RATE = 44100;
    private final static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    final static int BUFFER_SIZE = 4 * AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
    );

    public final static int CHUNK_SIZE = 8192;
    private float[] chunk;

    private AudioRecord audioRecord;
    private boolean isRecording;

    public AudioFetcher() throws SecurityException {
        chunk = new float[CHUNK_SIZE];

        this.audioRecord = new AudioRecord(
                AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            throw new IllegalStateException("Culdn't initialize AudioRecord");

        isRecording = false;
    }

    public void start() {
        audioRecord.startRecording();
        isRecording = true;
    }

    public void stop() {
        isRecording = false;
        audioRecord.stop();
    }

    public double[] getChunk() {
        if (!isRecording)
            throw new IllegalStateException("AudioRecord is not recording");

        int nRead = audioRecord.read(chunk, 0, chunk.length, AudioRecord.READ_BLOCKING);
        if (nRead < 0)
            throw new RuntimeException("AudioRecord read returned error number " + nRead);

        /*StringBuilder str = new StringBuilder();
        for (float f: chunk)
            str.append(f).append(", ");
        Log.i(TAG, chunk.length + " samples: " + str.toString());*/

        double[] chunkDouble = new double[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            chunkDouble[i] = (double) chunk[i];
        }

        return chunkDouble;
    }

    @Override
    public void finalize() {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }
}
