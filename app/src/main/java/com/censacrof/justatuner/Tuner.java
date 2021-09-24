package com.censacrof.justatuner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Tuner {
    private final String TAG = "Tuner";

    static final int AUDIO_SOURCE = MediaRecorder.AudioSource.UNPROCESSED;
    static final int SAMPLE_RATE = 44100;
    static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    static final int BUFFER_SIZE = 4 * AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
    );

    static final float CHUNK_DURATION = 0.2f;
    static final int CHUNK_SIZE = (int) (SAMPLE_RATE * CHUNK_DURATION);

    private final AudioRecord audioRecord;
    private final float[] chunk;

    private Thread audioFetcherThread;

    public Tuner() throws SecurityException {
        audioRecord = new AudioRecord(
                AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            throw new UnsupportedOperationException("Error initializing AudioRecord");

        chunk = new float[CHUNK_SIZE];
    }

    @Override
    public void finalize() {
        audioRecord.stop();
        audioRecord.release();
    }

    public void start() {
        if (audioFetcherThread != null && audioFetcherThread.isAlive())
            return;

        audioFetcherThread = new Thread(new AudioFetcher(audioRecord, chunk));
        audioFetcherThread.start();
    }

    public void stop() {
        if (audioFetcherThread == null)
            return;
        if (!audioFetcherThread.isAlive())
            return;

        audioFetcherThread.interrupt();
        try {
            audioFetcherThread.join();
        }
        catch (InterruptedException e) {
            Log.i(TAG, "AudioFetcher was interrupted");
        }
    }

    private static class AudioFetcher implements Runnable {
        private final String TAG = "AudioFetcher";
        private final AudioRecord audioRecord;
        private final float[] chunk;

        public AudioFetcher(AudioRecord audioRecord, float[] chunk) {
            this.audioRecord = audioRecord;
            this.chunk = chunk;
        }

        public void run() {
            Log.i(TAG,"Started");
            audioRecord.startRecording();

            while (!Thread.currentThread().isInterrupted()) {
                int nRead;
                synchronized (chunk) {
                    nRead = audioRecord.read(
                            chunk,
                            0,
                            chunk.length,
                            AudioRecord.READ_BLOCKING
                    );
                }

                StringBuilder str = new StringBuilder();
                for (float f: chunk) {
                    str.append(f).append(", ");
                }
                Log.v(TAG, "Read " + nRead + " values: " + str.toString());
            }

            audioRecord.stop();
            Log.i(TAG,"Stopped");
        }
    }
}
