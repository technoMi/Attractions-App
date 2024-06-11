package com.example.attractions.bottomnav.attractions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.attractions.CityInfoActivity;
import com.example.attractions.cities.CityAdapter;
import com.example.attractions.databinding.FragmentAttractionsBinding;
import com.example.attractions.cities.City;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AttractionsFragment extends Fragment {
    private FragmentAttractionsBinding binding;

    private RecyclerView cityRecyclerView;
    private List<City> cityList;
    private List<City> filteredList;
    private CityAdapter cityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttractionsBinding.inflate(inflater, container, false);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Cities");
        cityRecyclerView = binding.citiesRecyclerView;
        binding.citiesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cityList = new ArrayList<>();
        filteredList = new ArrayList<>();

        SearchView searchView = binding.searchView;

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    cityList.clear();
                    for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                        City city = citySnapshot.getValue(City.class);
                        if (city != null) {
                            cityList.add(city);
                        }
                    }
                    filteredList.addAll(cityList);
                    cityAdapter = new CityAdapter(getContext(), filteredList, city -> {
                        Intent intent = new Intent(requireActivity(), CityInfoActivity.class);
                        intent.putExtra("city", city.id);
                        startActivity(intent);
                    });
                    cityRecyclerView.setAdapter(cityAdapter);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.searchView.setVisibility(View.VISIBLE);
                    binding.greyLine.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //do nothing
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                String searchText = newText != null ? newText.toLowerCase() : "";
                if (!searchText.isEmpty()) {
                    for (City city : cityList) {
                        assert city.title != null;
                        if (city.title.toLowerCase().contains(searchText)) {
                            filteredList.add(city);
                        }
                    }
                } else {
                    filteredList.addAll(cityList);
                }
                cityAdapter.notifyDataSetChanged();
                return false;
            }
        });

        return binding.getRoot();
    }
}
