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
    private int currentUserId;
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
        emptyState  = view.findViewById(R.id.emptyStateFavorites);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        favoriteList = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), favoriteList, new TripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripModel trip) {
                // فتح صفحة التفاصيل ✓
                openTripDetail(trip.getId());
            }

            @Override
            public void onFavoriteClick(TripModel trip, int position) {
                // إزالة من المفضلة ✓
                dbHelper.removeFavorite(currentUserId, trip.getId());
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
                // فتح نموذج الحجز مباشرة ✓
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
        // إعادة تحميل عند الرجوع من التفاصيل
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList.clear();
        Cursor cursor = dbHelper.getFavoriteTrips(currentUserId);
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
                .replace(R.id.frameContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showReservationDialog(TripModel trip) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reserve_form, null);

        TextView tvTitle     = dialogView.findViewById(R.id.tvReserveTitle);
        EditText etQuantity  = dialogView.findViewById(R.id.etQuantity);
        Spinner  spinnerType = dialogView.findViewById(R.id.spinnerReserveType);
        Button   btnConfirm  = dialogView.findViewById(R.id.btnConfirmReserve);
        Button   btnCancel   = dialogView.findViewById(R.id.btnCancelReserve);

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