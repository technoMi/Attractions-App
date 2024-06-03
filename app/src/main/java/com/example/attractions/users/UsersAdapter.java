package com.example.attractions.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.example.attractions.R;
import com.example.attractions.cities.City;
import com.example.attractions.utils.ChatUtil;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder>{

    //private ArrayList<User> users = new ArrayList<>();
    private final Context context;

    private final List<User> userList;

    public UsersAdapter(Context context, List<User> userList){
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item_rv, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.username_tv.setText(user.username);

        if (!user.profileImage.isEmpty()){
            Glide.with(holder.itemView.getContext()).load(user.profileImage).into(holder.profileImage_iv);
        }

        holder.itemView.setOnClickListener(view -> {
            ChatUtil.createChat(user, context);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
