package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout emptyState;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;
    private List<TripModel> favoriteList;
    private TripAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        emptyState = view.findViewById(R.id.emptyState);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        favoriteList = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), favoriteList, new TripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripModel trip) {
                openTripDetail(trip.getId());
            }

            @Override
            public void onFavoriteClick(TripModel trip, int position) {
                dbHelper.removeFavorite(loggedInEmail, trip.getId());
                favoriteList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, favoriteList.size());
                updateEmptyState();
                Toast.makeText(requireContext(),
                        trip.getDestination() + " removed from favorites",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReserveClick(TripModel trip) {
                showReservationDialog(trip);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList.clear();
        Cursor cursor = dbHelper.getFavoriteTrips(loggedInEmail);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                TripModel trip = new TripModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RATING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE))
                );
                trip.setFavorite(true);
                favoriteList.add(trip);
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (favoriteList.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void openTripDetail(int tripId) {
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(tripId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showReservationDialog(TripModel trip) {
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
}