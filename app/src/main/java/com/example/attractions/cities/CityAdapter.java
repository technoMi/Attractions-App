package com.example.attractions.cities;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.attractions.R;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final Context context;
    private final List<City> cityList;
    private final ClickListener itemClickListener;

    public CityAdapter(Context context, List<City> cityList, ClickListener itemClickListener) {
        this.context = context;
        this.cityList = cityList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.city_item, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        City city = cityList.get(position);
        holder.title.setText(city.title);
        //String imgUri = city.imgPoster.toUri().buildUpon().scheme("https").build().toString();
        String imgUri = String.valueOf(Uri.parse(city.imgPoster).buildUpon().scheme("https").build());

        Glide.with(context).load(imgUri).apply(RequestOptions.circleCropTransform()).into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> itemClickListener.onClick(city));
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public interface ClickListener {
        void onClick(City city);
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView title;

        public CityViewHolder(View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            title = itemView.findViewById(R.id.title);
        }
    }
}