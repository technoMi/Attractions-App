package com.example.attractions.bottomnav.new_chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attractions.R;
import com.example.attractions.cities.City;
import com.example.attractions.cities.CityAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.attractions.databinding.FragmentNewChatBinding;
import com.example.attractions.users.User;
import com.example.attractions.users.UsersAdapter;

public class NewChatFragment extends Fragment {
    private FragmentNewChatBinding binding;

    private List<User> userList;
    private List<User> filteredList;

    private UsersAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewChatBinding.inflate(inflater, container, false);

        SearchView searchView = binding.searchView;

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();

        loadUsers();

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
                    for (User user : userList) {
                        assert user.username!= null;
                        if (user.username.toLowerCase().contains(searchText)) {
                            filteredList.add(user);
                        }
                    }
                } else {
                    filteredList.addAll(userList);
                }
                userAdapter.notifyDataSetChanged();
                return false;
            }
        });

        return binding.getRoot();
    }

    private void loadUsers(){
        ArrayList<User> users = new ArrayList<User>();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    userList.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()){
                        if (userSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            continue;
                        }

                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue().toString();
                        String profileImage = userSnapshot.child("profileImage").getValue().toString();

                        users.add(new User(uid, username, profileImage));
                        userList.add(new User(uid, username, profileImage));
                    }

                    filteredList.addAll(userList);
                    binding.usersRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.usersRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                    userAdapter = new UsersAdapter(getContext(), filteredList);
                    binding.usersRv.setAdapter(userAdapter);
                    if (users.isEmpty()) {
                        binding.thereIsNothingLabel.setVisibility(View.VISIBLE);
                    }
                } catch (NullPointerException ignored) {
                    showToast(getString(R.string.some_error));
                }
                binding.progressBar2.setVisibility(View.INVISIBLE);
                binding.content.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            private void showToast(String text) {
                try {
                    if (isAdded()) Toast.makeText(requireActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
        });
    }
}
