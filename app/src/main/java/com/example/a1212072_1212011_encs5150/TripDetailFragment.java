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
    private int currentUserId;

    private ImageView ivImage, ivFavorite, ivBack;
    private TextView tvDestination, tvCountry, tvRating, tvPrice, tvDuration, tvDescription;
    private Button btnReserve;

    // Factory method — يستخدم Bundle بدل constructor مباشر
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
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        // Init views
        ivImage       = view.findViewById(R.id.ivDetailImage);
        ivFavorite    = view.findViewById(R.id.ivDetailFavorite);
        ivBack        = view.findViewById(R.id.ivBack);
        tvDestination = view.findViewById(R.id.tvDetailDestination);
        tvCountry     = view.findViewById(R.id.tvDetailCountry);
        tvRating      = view.findViewById(R.id.tvDetailRating);
        tvPrice       = view.findViewById(R.id.tvDetailPrice);
        tvDuration    = view.findViewById(R.id.tvDetailDuration);
        tvDescription = view.findViewById(R.id.tvDetailDescription);
        btnReserve    = view.findViewById(R.id.btnDetailReserve);

        // جلب بيانات الرحلة من الـ arguments
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

        // Favorite state
        refreshFavoriteIcon();

        // Load image
        loadImage(trip.getImage(), ivImage);
    }

    private void refreshFavoriteIcon() {
        boolean isFav = dbHelper.isFavorite(currentUserId, trip.getId());
        ivFavorite.setColorFilter(isFav
                ? Color.parseColor("#FFD700")
                : Color.parseColor("#888780"));
    }

    private void setupClickListeners() {
        // Back
        ivBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Favorite toggle
        ivFavorite.setOnClickListener(v -> {
            boolean isFav = dbHelper.isFavorite(currentUserId, trip.getId());
            if (isFav) {
                dbHelper.removeFavorite(currentUserId, trip.getId());
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(currentUserId, trip.getId());
                Toast.makeText(requireContext(), "Added to favorites ♡", Toast.LENGTH_SHORT).show();
            }
            refreshFavoriteIcon();
        });

        // Reserve
        btnReserve.setOnClickListener(v -> showReservationDialog());
    }

    // ── Reservation Dialog ────────────────────────────────────────────────────
    private void showReservationDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reserve_form, null);

        EditText etQuantity      = dialogView.findViewById(R.id.etQuantity);
        Spinner  spinnerType     = dialogView.findViewById(R.id.spinnerReserveType);
        Button   btnConfirm      = dialogView.findViewById(R.id.btnConfirmReserve);
        Button   btnCancel       = dialogView.findViewById(R.id.btnCancelReserve);

        // Spinner options
        String[] types = {"Standard", "Premium", "Group", "Honeymoon"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // Set title on dialog layout
        TextView tvTitle = dialogView.findViewById(R.id.tvReserveTitle);
        if (tvTitle != null) tvTitle.setText("Reserve: " + trip.getDestination());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String quantityStr = etQuantity.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                etQuantity.setError("Please enter number of travelers");
                return;
            }
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                etQuantity.setError("Enter a valid number");
                return;
            }

            String type = spinnerType.getSelectedItem().toString();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());

            boolean ok = dbHelper.insertReservation(
                    currentUserId, trip.getId(), date, quantity, type, "active");

            if (ok) {
                Toast.makeText(requireContext(),
                        "Reservation confirmed! ✓", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(),
                        "Failed to reserve. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // ── Image loader ──────────────────────────────────────────────────────────
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