package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripDetailFragment extends Fragment {

    private static final String ARG_TRIP_ID = "trip_id";

    private TripModel trip;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;

    private ImageView ivImage, ivFavorite, ivBack;
    private TextView tvDestination, tvCountry, tvRating, tvPrice, tvDuration, tvDescription;
    private Button btnReserve;

    public static TripDetailFragment newInstance(int tripId) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, tripId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("TravelPlannerPrefs", requireContext().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        ivImage = view.findViewById(R.id.ivDetailImage);
        ivFavorite = view.findViewById(R.id.ivDetailFavorite);
        ivBack = view.findViewById(R.id.ivBack);
        tvDestination = view.findViewById(R.id.tvDetailDestination);
        tvCountry = view.findViewById(R.id.tvDetailCountry);
        tvRating = view.findViewById(R.id.tvDetailRating);
        tvPrice = view.findViewById(R.id.tvDetailPrice);
        tvDuration = view.findViewById(R.id.tvDetailDuration);
        tvDescription = view.findViewById(R.id.tvDetailDescription);
        btnReserve = view.findViewById(R.id.btnDetailReserve);

        if (getArguments() != null) {
            int tripId = getArguments().getInt(ARG_TRIP_ID, -1);
            trip = dbHelper.getTripById(tripId);
        }

        if (trip == null) {
            Toast.makeText(requireContext(), "Trip not found", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        bindData();
        setupClickListeners();
    }

    private void bindData() {
        tvDestination.setText(trip.getDestination());
        tvCountry.setText(trip.getCountry());
        tvRating.setText("★ " + trip.getRating());
        tvPrice.setText("$" + (int) trip.getPrice());
        tvDuration.setText(String.valueOf(trip.getDurationDays()));
        tvDescription.setText(trip.getDescription());

        refreshFavoriteIcon();
        loadImage(trip.getImage(), ivImage);
    }

    private void refreshFavoriteIcon() {
        boolean isFav = dbHelper.isFavorite(loggedInEmail, trip.getId());
        ivFavorite.setColorFilter(isFav ? Color.parseColor("#FFD700") : Color.parseColor("#888780"));
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        ivFavorite.setOnClickListener(v -> {
            boolean isFav = dbHelper.isFavorite(loggedInEmail, trip.getId());
            if (isFav) {
                dbHelper.removeFavorite(loggedInEmail, trip.getId());
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(loggedInEmail, trip.getId());
                Toast.makeText(requireContext(), "Added to favorites ♡", Toast.LENGTH_SHORT).show();
            }
            refreshFavoriteIcon();
        });

        btnReserve.setOnClickListener(v -> showReservationDialog());
    }

    private void showReservationDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reservation, null);

        TextView tvTripName = dialogView.findViewById(R.id.tvTripName);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        TextView tvTotalPrice = dialogView.findViewById(R.id.tvTotalPrice);
        TextView btnMinus = dialogView.findViewById(R.id.btnMinus);
        TextView btnPlus = dialogView.findViewById(R.id.btnPlus);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerReservationType);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        if (tvTripName != null) tvTripName.setText(trip.getDestination() + " · " + trip.getCountry());

        String[] types = {"Standard", "Premium", "VIP"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        final int[] quantity = {1};

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
                tvTotalPrice.setText("$" + (int) (trip.getPrice() * quantity[0]));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity[0] < 20) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
                tvTotalPrice.setText("$" + (int) (trip.getPrice() * quantity[0]));
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem().toString();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            double total = trip.getPrice() * quantity[0];

            boolean ok = dbHelper.insertReservation(
                    loggedInEmail, trip.getId(), trip.getDestination(),
                    quantity[0], type, date, total, "Confirmed");

            Toast.makeText(requireContext(),
                    ok ? "Reservation confirmed for " + trip.getDestination() + "!" : "Failed. Try again.",
                    Toast.LENGTH_SHORT).show();
            if (ok) dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_plane);
        if (imageUrl == null || imageUrl.isEmpty()) return;
        imageView.setTag(imageUrl);
        new Thread(() -> {
            Bitmap bmp = downloadBitmap(imageUrl);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (imageUrl.equals(imageView.getTag()) && bmp != null) {
                    imageView.setImageBitmap(bmp);
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
}