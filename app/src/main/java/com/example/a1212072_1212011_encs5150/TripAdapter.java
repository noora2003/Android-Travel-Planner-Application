package com.example.a1212072_1212011_encs5150;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private ArrayList<Trip> trips;
    private Context context;
    private OnReserveClickListener reserveListener;

    public interface OnReserveClickListener {
        void onReserveClick(int tripId);
    }

    public TripAdapter(ArrayList<Trip> trips, Context context, OnReserveClickListener listener) {
        this.trips = trips;
        this.context = context;
        this.reserveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.tvDestination.setText(trip.getDestination());
        holder.tvCountry.setText(trip.getCountry());
        holder.tvDuration.setText(trip.getDurationDays() + " days");
        holder.tvPrice.setText("$" + trip.getPrice());
        holder.tvRating.setText("★ " + trip.getRating());

        holder.btnFavorite.setImageResource(trip.isFavorite() ?
                android.R.drawable.btn_star_big_on :
                android.R.drawable.btn_star_big_off);

        holder.btnFavorite.setOnClickListener(v -> {
            trip.setFavorite(!trip.isFavorite());
            notifyItemChanged(position);
            // TODO: حفظ المفضلة في SharedPreferences أو DB
        });

        holder.btnReserve.setOnClickListener(v -> {
            if (reserveListener != null) {
                reserveListener.onReserveClick(trip.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            // TODO: فتح TripDetailFragment
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDestination, tvCountry, tvDuration, tvPrice, tvRating;
        ImageButton btnFavorite;
        Button btnReserve;

        ViewHolder(View itemView) {
            super(itemView);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvCountry = itemView.findViewById(R.id.tvCountry);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}