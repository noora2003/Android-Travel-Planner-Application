package com.example.a1212072_1212011_encs5150;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private Context context;
    private List<TripModel> tripList;
    private OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(TripModel trip);
        void onFavoriteClick(TripModel trip, int position);
        void onReserveClick(TripModel trip);
    }

    public TripAdapter(Context context, List<TripModel> tripList, OnTripClickListener listener) {
        this.context  = context;
        this.tripList = tripList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        TripModel trip = tripList.get(position);

        // Number badge (1, 2, 3...)
        holder.tvNumber.setText(String.valueOf(position + 1));

        // Text fields
        holder.tvDestination.setText(trip.getDestination());
        holder.tvCountry.setText(trip.getCountry());
        holder.tvRating.setText("★ " + trip.getRating() + "  ·  reviews");
        holder.tvPriceDuration.setText("$" + (int) trip.getPrice()
                + "  |  " + trip.getDurationDays() + " Days");

        // Load image using Glide
        com.bumptech.glide.Glide.with(context)
                .load(trip.getImage())
                .centerCrop()
                .placeholder(R.drawable.ic_plane)
                .into(holder.ivTripImage);

        // Favorite icon
        holder.ivFavorite.setImageResource(
                trip.isFavorite()
                        ? R.drawable.ic_favorite_filled
                        : R.drawable.ic_favorite_border);

        // Clicks
        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
        holder.ivFavorite.setOnClickListener(v ->
                listener.onFavoriteClick(trip, holder.getAdapterPosition()));
        holder.btnReserve.setOnClickListener(v -> listener.onReserveClick(trip));
    }

    @Override
    public int getItemCount() { return tripList.size(); }

    public void updateList(List<TripModel> newList) {
        tripList = newList;
        notifyDataSetChanged();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTripImage, ivFavorite;
        TextView tvNumber, tvDestination, tvCountry,
                tvRating, tvPriceDuration;
        Button btnReserve;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTripImage     = itemView.findViewById(R.id.ivTripImage);
            ivFavorite      = itemView.findViewById(R.id.ivFavorite);
            tvNumber        = itemView.findViewById(R.id.tvNumber);
            tvDestination   = itemView.findViewById(R.id.tvDestination);
            tvCountry       = itemView.findViewById(R.id.tvCountry);
            tvRating        = itemView.findViewById(R.id.tvRating);
            tvPriceDuration = itemView.findViewById(R.id.tvPriceDuration);
            btnReserve      = itemView.findViewById(R.id.btnReserve);
        }
    }
}