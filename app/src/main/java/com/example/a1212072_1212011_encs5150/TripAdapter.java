package com.example.a1212072_1212011_encs5150;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final Context context;
    private List<TripModel> tripList;
    private final OnTripClickListener listener;

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

        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvDestination.setText(trip.getDestination());
        holder.tvCountry.setText(trip.getCountry());
        holder.tvRating.setText("★ " + trip.getRating() + "  ·  reviews");
        holder.tvPriceDuration.setText("$" + (int) trip.getPrice()
                + "  |  " + trip.getDurationDays() + " Days");

        // ── FIX 1: No Glide — load image with plain Thread + Handler ──────────
        loadImage(trip.getImage(), holder.ivTripImage);

        // ── FIX 2: No ic_favorite_filled — tint the single existing drawable ──
        // ic_favorite_border exists in your drawable folder (confirmed from XML).
        // Gold tint = favorited, white tint = not favorited. No second file needed.
        holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
        holder.ivFavorite.setColorFilter(
                trip.isFavorite() ? Color.parseColor("#FFD700")   // gold = active
                        : Color.parseColor("#EEEEEE")); // white = inactive

        // ── FIX 3: Null-safe click listeners ─────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTripClick(trip);
        });
        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(trip, holder.getAdapterPosition());
        });
        holder.btnReserve.setOnClickListener(v -> {
            if (listener != null) listener.onReserveClick(trip);
        });
    }

    @Override
    public int getItemCount() {
        return tripList != null ? tripList.size() : 0;
    }

    public void updateList(List<TripModel> newList) {
        tripList = newList;
        notifyDataSetChanged();
    }

    // ── Image loader (replaces Glide) ─────────────────────────────────────────

    private void loadImage(String imageUrl, ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_plane); // placeholder
        imageView.clearColorFilter();                    // don't tint placeholder gold

        if (imageUrl == null || imageUrl.isEmpty()) return;

        imageView.setTag(imageUrl); // tag used to cancel stale loads on recycled rows

        new Thread(() -> {
            Bitmap bitmap = downloadBitmap(imageUrl);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (imageUrl.equals(imageView.getTag()) && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }).start();
    }

    private Bitmap downloadBitmap(String imageUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                return BitmapFactory.decodeStream(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTripImage, ivFavorite;
        TextView  tvNumber, tvDestination, tvCountry, tvRating, tvPriceDuration;
        Button    btnReserve;

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