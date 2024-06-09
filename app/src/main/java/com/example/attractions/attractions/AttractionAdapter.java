package com.example.attractions.attractions;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.HttpException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.attractions.AttractionInfoActivity;
import com.example.attractions.ChatActivity;
import com.example.attractions.R;

import java.util.ArrayList;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder> {
    private final ArrayList<Attraction> attractionList;

    public AttractionAdapter(ArrayList<Attraction> attractionsList){
        this.attractionList = attractionsList;
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attraction_item_rv, parent, false);
        return new AttractionViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(AttractionViewHolder holder, int position) {
        Attraction attraction = attractionList.get(position);

        holder.title.setText(attraction.getAttraction_name());
        holder.rating.setText("⭐ " + attraction.getAttraction_rating());

        String imgUri = String.valueOf(Uri.parse(attraction.attraction_imgPoster()).buildUpon().scheme("https").build());
        Glide.with(holder.itemView.getContext()).load(imgUri).apply(RequestOptions.circleCropTransform()).into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AttractionInfoActivity.class);
            intent.putExtra("attractionId", attractionList.get(position).getAttraction_id());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public static class AttractionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView title;
        TextView rating;

        public AttractionViewHolder(View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster_iv);
            title = itemView.findViewById(R.id.att_name_tv);
            rating = itemView.findViewById(R.id.rating_tv);
        }
    }
}