package com.example.scholartrack.ui.coordinator;

// Unit 2: CoordinatorHelpFragment – simple help and logout for coordinator role.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.ui.CoordinatorHomeActivity;
import com.google.android.material.button.MaterialButton;

public class CoordinatorHelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coordinator_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialButton btnLogout = view.findViewById(R.id.btnCoordLogout);
        btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof CoordinatorHomeActivity) {
                ((CoordinatorHomeActivity) getActivity()).logout();
            }
        });
    }
}
