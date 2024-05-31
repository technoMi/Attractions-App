package com.example.attractions.bottomnav.attractions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.attractions.databinding.FragmentAttractionsBinding;


public class AttractionsFragment extends Fragment {
    private FragmentAttractionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttractionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
