package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class TripsFragment extends Fragment {

    private RecyclerView rvTrips;
    private LinearLayout emptyState;
    private TextView tvResultCount;
    private EditText etSearch;
    private LinearLayout filterContainer;

    private DatabaseHelper dbHelper;
    private List<TripModel> allTrips = new ArrayList<>();   // كل الرحلات
    private List<TripModel> filteredTrips = new ArrayList<>(); // بعد الفلترة
    private TripAdapter adapter;
    private int currentUserId;

    // فلتر الـ country الحالي (null = كل الدول)
    private String activeCountryFilter = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTrips         = view.findViewById(R.id.rvTrips);
        emptyState      = view.findViewById(R.id.emptyState);
        tvResultCount   = view.findViewById(R.id.tvResultCount);
        etSearch        = view.findViewById(R.id.etSearch);
        filterContainer = view.findViewById(R.id.filterContainer);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        filteredTrips = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), filteredTrips, new TripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripModel trip) {
                // فتح صفحة التفاصيل
                openTripDetail(trip.getId());
            }

            @Override
            public void onFavoriteClick(TripModel trip, int position) {
                boolean isFav = dbHelper.isFavorite(currentUserId, trip.getId());
                if (isFav) {
                    dbHelper.removeFavorite(currentUserId, trip.getId());
                    trip.setFavorite(false);
                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.addFavorite(currentUserId, trip.getId());
                    trip.setFavorite(true);
                    Toast.makeText(requireContext(), "Added to favorites ♡", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onReserveClick(TripModel trip) {
                showReservationDialog(trip);
            }
        });

        rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTrips.setAdapter(adapter);

        loadAllTrips();
        setupSearch();
        buildFilterChips();
    }

    // ── Load Data ─────────────────────────────────────────────────────────────
    private void loadAllTrips() {
        allTrips.clear();
        Cursor cursor = dbHelper.getAllTrips();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int tripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                TripModel trip = new TripModel(
                        tripId,
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RATING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE))
                );
                trip.setFavorite(dbHelper.isFavorite(currentUserId, tripId));
                allTrips.add(trip);
            }
            cursor.close();
        }
        applyFilters();
    }

    // ── Search ────────────────────────────────────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                applyFilters();
            }
        });
    }

    // ── Filter Chips ──────────────────────────────────────────────────────────
    private void buildFilterChips() {
        filterContainer.removeAllViews();

        // زر "All"
        addChip("All", null);

        // جمع الدول الفريدة
        List<String> countries = new ArrayList<>();
        for (TripModel t : allTrips) {
            if (!countries.contains(t.getCountry())) {
                countries.add(t.getCountry());
            }
        }
        for (String country : countries) {
            addChip(country, country);
        }
    }

    private void addChip(String label, String filterValue) {
        TextView chip = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(10);
        chip.setLayoutParams(params);
        chip.setText(label);
        chip.setTextSize(12f);
        chip.setPadding(32, 14, 32, 14);

        boolean isActive = (filterValue == null && activeCountryFilter == null)
                || (filterValue != null && filterValue.equals(activeCountryFilter));

        if (isActive) {
            chip.setBackgroundResource(R.drawable.pill_green_bg);
            chip.setTextColor(0xFFE7E1B1);
        } else {
            chip.setBackgroundResource(R.drawable.pill_beige_bg);
            chip.setTextColor(0xFF2FA084);
        }

        chip.setOnClickListener(v -> {
            activeCountryFilter = filterValue;
            buildFilterChips();
            applyFilters();
        });

        filterContainer.addView(chip);
    }

    // ── Apply Search + Filter ─────────────────────────────────────────────────
    private void applyFilters() {
        String query = etSearch.getText().toString().trim().toLowerCase();

        filteredTrips.clear();
        for (TripModel trip : allTrips) {
            boolean matchesSearch = query.isEmpty()
                    || trip.getDestination().toLowerCase().contains(query)
                    || trip.getCountry().toLowerCase().contains(query)
                    || trip.getDescription().toLowerCase().contains(query);

            boolean matchesCountry = activeCountryFilter == null
                    || trip.getCountry().equals(activeCountryFilter);

            if (matchesSearch && matchesCountry) {
                filteredTrips.add(trip);
            }
        }

        adapter.notifyDataSetChanged();

        // Result count
        tvResultCount.setText(filteredTrips.size() + " trip" + (filteredTrips.size() != 1 ? "s" : "") + " found");

        if (filteredTrips.isEmpty()) {
            rvTrips.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvTrips.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    // ── Open Detail Fragment ──────────────────────────────────────────────────
    private void openTripDetail(int tripId) {
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(tripId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    // ── Reservation Dialog ────────────────────────────────────────────────────
    private void showReservationDialog(TripModel trip) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reserve_form, null);

        TextView tvTitle      = dialogView.findViewById(R.id.tvReserveTitle);
        EditText etQuantity   = dialogView.findViewById(R.id.etQuantity);
        Spinner  spinnerType  = dialogView.findViewById(R.id.spinnerReserveType);
        Button   btnConfirm   = dialogView.findViewById(R.id.btnConfirmReserve);
        Button   btnCancel    = dialogView.findViewById(R.id.btnCancelReserve);

        if (tvTitle != null) tvTitle.setText("Reserve: " + trip.getDestination());

        String[] types = {"Standard", "Premium", "Group", "Honeymoon"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            if (qtyStr.isEmpty()) { etQuantity.setError("Required"); return; }
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                etQuantity.setError("Enter a valid number");
                return;
            }
            String type = spinnerType.getSelectedItem().toString();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            boolean ok = dbHelper.insertReservation(currentUserId, trip.getId(), date, qty, type, "active");
            Toast.makeText(requireContext(),
                    ok ? "Reservation confirmed! ✓" : "Failed. Try again.",
                    Toast.LENGTH_SHORT).show();
            if (ok) dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
}