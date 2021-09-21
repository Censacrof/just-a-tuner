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
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 4;

    private AudioRecord audioRecord;

    private Thread audioFetcherThread;
    private AudioFetcher audioFetcher;
    private boolean audioFetcherAlive = false;

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
    }

    public void start() {
        if (audioFetcherAlive)
            return;

        audioFetcher = new AudioFetcher(audioRecord);
        audioFetcherThread = new Thread(audioFetcher);
        audioFetcherThread.start();
        audioFetcherAlive = true;
    }

    public void stop() {
        if (!audioFetcherAlive)
            return;

        audioFetcherAlive = false;
        audioFetcherThread.interrupt();
        try {
            audioFetcherThread.join();
        }
        catch (InterruptedException e) {
            return;
        }
    }

    private static class AudioFetcher implements Runnable {
        private boolean shouldStop;
        private final String TAG = "AudioFetcher";
        private AudioRecord audioRecord;
        private final float N_SECONDS = 0.5f;

        public AudioFetcher(AudioRecord audioRecord) {
            shouldStop = false;
            this.audioRecord = audioRecord;
        }

        public void run() {
            audioRecord.startRecording();
            Log.v(TAG,"Started");

            final int nData = (int) ((float)(audioRecord.getSampleRate()) * N_SECONDS);
            float[] audioData = new float[nData];

            try {
                while (true) {
                    int nRead = audioRecord.read(audioData, 0, audioData.length, AudioRecord.READ_BLOCKING);
                    Log.v(TAG, "Read " + nRead + " values");
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                Log.v(TAG,"Stopped");
            }

            audioRecord.stop();
        }
    }
}
