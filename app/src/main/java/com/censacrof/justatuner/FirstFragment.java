package com.censacrof.justatuner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.censacrof.justatuner.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private String[] permissionsRequired = {Manifest.permission.RECORD_AUDIO};
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private Tuner tuner;

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
        tuner = new Tuner();

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

    private void startRecording() {
        requestMicPermission();
        tuner.start();
    }

    private void stopRecording() {
        tuner.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}