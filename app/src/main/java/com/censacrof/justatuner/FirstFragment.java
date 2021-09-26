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
            // int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                float[] chunk = audioFetcher.getChunk();
                binding.graphView.setSamples(chunk);

                /*count++;
                if (count > 3);
                    break;*/
            }
            Log.i("viewUpdaterThread", "stopped");
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