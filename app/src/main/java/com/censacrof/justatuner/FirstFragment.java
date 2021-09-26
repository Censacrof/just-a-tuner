package com.censacrof.justatuner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.censacrof.justatuner.databinding.FragmentFirstBinding;
import com.github.psambit9791.jdsp.signal.CrossCorrelation;
import com.github.psambit9791.jdsp.signal.Decimate;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private final String[] permissionsRequired = {Manifest.permission.RECORD_AUDIO};
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private AudioFetcher audioFetcher;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestMicPermission();
        audioFetcher = new AudioFetcher();

        binding.buttonStart.setOnClickListener(event -> startRecording());
        binding.buttonStop.setOnClickListener(event -> stopRecording());
    }

    private boolean requestMicPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            ActivityCompat.requestPermissions(getActivity(), permissionsRequired, REQUEST_RECORD_AUDIO_PERMISSION);

            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }

    Thread viewUpdaterThread;
    private void startRecording() {
        if (viewUpdaterThread != null && viewUpdaterThread.isAlive())
            return;

        requestMicPermission();
        audioFetcher.start();
        for (int i = 0; i < 3; i++)
            audioFetcher.getChunk();

        viewUpdaterThread = new Thread(() -> {
            final String TAG = "viewUpdaterThread";

            while (!Thread.currentThread().isInterrupted()) {
                double[] chunk = audioFetcher.getChunk();

                final int downSamplingFactor = 8;
                chunk = new Decimate(chunk, AudioFetcher.SAMPLE_RATE, true)
                        .decimate(downSamplingFactor);

                Log.d(TAG, "Decimated size: " + chunk.length);

                CrossCorrelation cc = new CrossCorrelation(chunk);
                double[] autoCorrelated = cc.crossCorrelate("full");
                binding.graphView.setSamples(autoCorrelated);

                double highestPeak = 0;
                double highestPeakPeriod = 0;
                int zeroIndex = autoCorrelated.length / 2;
                for (int i = autoCorrelated.length / 2 + 10; i < autoCorrelated.length - 1; i++) {
                    if (autoCorrelated[i - 1] < autoCorrelated[i]
                            && autoCorrelated[i + 1]  < autoCorrelated[i]) {
                        //Log.d(TAG, "Peak at i = " + i);
                        if (autoCorrelated[i] > highestPeak) {
                            highestPeak = autoCorrelated[i];
                            highestPeakPeriod = (i - zeroIndex) * downSamplingFactor / (float) AudioFetcher.SAMPLE_RATE ;
                        }
                    }
                }

                Log.i(TAG, "Highest peak period: " + highestPeakPeriod + " (" + (1/highestPeakPeriod) + "hz)");
            }
            Log.i(TAG, "stopped");
        });

        viewUpdaterThread.start();
    }

    private void stopRecording() {
        if (viewUpdaterThread == null)
            return;
        if (!viewUpdaterThread.isAlive())
            return;

        viewUpdaterThread.interrupt();
        audioFetcher.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}