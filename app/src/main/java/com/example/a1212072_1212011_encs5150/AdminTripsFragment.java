package com.example.a1212072_1212011_encs5150;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AdminTripsFragment extends Fragment {

    private RecyclerView rvTrips;
    private DatabaseHelper db;
    private List<TripRow> tripList = new ArrayList<>();
    private TripAdminAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        TextView tvHeader = view.findViewById(R.id.tvAdminListHeader);
        tvHeader.setText("Manage Trips");

        // زر إضافة رحلة
        view.findViewById(R.id.btnAddItem).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btnAddItem).setOnClickListener(v -> showAddTripDialog());

        rvTrips = view.findViewById(R.id.rvAdminList);
        rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdminAdapter();
        rvTrips.setAdapter(adapter);
        loadTrips();
    }

    private void loadTrips() {
        tripList.clear();
        Cursor c = db.getAllTrips();
        if (c != null) {
            while (c.moveToNext()) {
                TripRow row = new TripRow();
                row.id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                row.destination = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION));
                row.country = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY));
                row.price = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE));
                row.rating = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_RATING));
                row.duration = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION));
                row.description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                row.image = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE));
                tripList.add(row);
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }

    static class TripRow {
        int id, duration;
        double price, rating;
        String destination, country, description, image;
    }

    // ── Adapter ─────────────────────────────────────────────────────────────
    class TripAdminAdapter extends RecyclerView.Adapter<TripAdminAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TripRow row = tripList.get(position);
            holder.tvTitle.setText(row.destination + ", " + row.country);
            holder.tvSub1.setText("★ " + row.rating + "  |  $" + (int) row.price + "  |  " + row.duration + " days");
            holder.tvSub2.setText(row.description.length() > 60 ? row.description.substring(0, 60) + "..." : row.description);

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Trip")
                        .setMessage("Delete " + row.destination + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.getWritableDatabase().delete(DatabaseHelper.TABLE_TRIPS,
                                    DatabaseHelper.COL_TRIP_ID + "=?",
                                    new String[]{String.valueOf(row.id)});
                            tripList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            holder.btnEdit.setOnClickListener(v -> showEditTripDialog(row, position));
        }

        @Override public int getItemCount() { return tripList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub1, tvSub2;
            android.widget.Button btnDelete, btnEdit;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvRowTitle);
                tvSub1 = v.findViewById(R.id.tvRowSub1);
                tvSub2 = v.findViewById(R.id.tvRowSub2);
                btnDelete = v.findViewById(R.id.btnRowDelete);
                btnEdit = v.findViewById(R.id.btnRowEdit);
                btnEdit.setVisibility(View.VISIBLE);
            }
        }
    }

    // ── Add Trip Dialog ────────────────────────────────────────────────────
    private void showAddTripDialog() {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_trip, null);

        EditText etDest = v.findViewById(R.id.etTripDest);
        EditText etCountry = v.findViewById(R.id.etTripCountry);
        EditText etDuration = v.findViewById(R.id.etTripDuration);
        EditText etPrice = v.findViewById(R.id.etTripPrice);
        EditText etRating = v.findViewById(R.id.etTripRating);
        EditText etDesc = v.findViewById(R.id.etTripDesc);
        EditText etImage = v.findViewById(R.id.etTripImage);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Trip")
                .setView(v)
                .setPositiveButton("Add", (d, w) -> {
                    try {
                        String dest = etDest.getText().toString().trim();
                        String country = etCountry.getText().toString().trim();
                        String durStr = etDuration.getText().toString().trim();
                        String priceStr = etPrice.getText().toString().trim();
                        String ratingStr = etRating.getText().toString().trim();
                        String desc = etDesc.getText().toString().trim();
                        String image = etImage.getText().toString().trim();

                        if (dest.isEmpty() || country.isEmpty() || durStr.isEmpty()
                                || priceStr.isEmpty() || ratingStr.isEmpty() || desc.isEmpty()) {
                            Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int duration = Integer.parseInt(durStr);
                        double price = Double.parseDouble(priceStr);
                        double rating = Double.parseDouble(ratingStr);

                        // Generate a unique ID (max + 1)
                        int newId = tripList.isEmpty() ? 1 : tripList.get(tripList.size() - 1).id + 1;

                        db.insertTrip(newId, dest, country, duration, price, rating, desc, image);
                        loadTrips();
                        Toast.makeText(requireContext(), "Trip added successfully!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Edit Trip Dialog ────────────────────────────────────────────────────
    private void showEditTripDialog(TripRow row, int position) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_trip, null);

        EditText etDest = v.findViewById(R.id.etTripDest);
        EditText etCountry = v.findViewById(R.id.etTripCountry);
        EditText etDuration = v.findViewById(R.id.etTripDuration);
        EditText etPrice = v.findViewById(R.id.etTripPrice);
        EditText etRating = v.findViewById(R.id.etTripRating);
        EditText etDesc = v.findViewById(R.id.etTripDesc);
        EditText etImage = v.findViewById(R.id.etTripImage);

        etDest.setText(row.destination);
        etCountry.setText(row.country);
        etDuration.setText(String.valueOf(row.duration));
        etPrice.setText(String.valueOf(row.price));
        etRating.setText(String.valueOf(row.rating));
        etDesc.setText(row.description);
        etImage.setText(row.image);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Trip")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        String dest = etDest.getText().toString().trim();
                        String country = etCountry.getText().toString().trim();
                        String durStr = etDuration.getText().toString().trim();
                        String priceStr = etPrice.getText().toString().trim();
                        String ratingStr = etRating.getText().toString().trim();
                        String desc = etDesc.getText().toString().trim();
                        String image = etImage.getText().toString().trim();

                        if (dest.isEmpty() || country.isEmpty() || durStr.isEmpty()
                                || priceStr.isEmpty() || ratingStr.isEmpty() || desc.isEmpty()) {
                            Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int duration = Integer.parseInt(durStr);
                        double price = Double.parseDouble(priceStr);
                        double rating = Double.parseDouble(ratingStr);

                        // حذف القديم وإضافة الجديد
                        db.getWritableDatabase().delete(DatabaseHelper.TABLE_TRIPS,
                                DatabaseHelper.COL_TRIP_ID + "=?",
                                new String[]{String.valueOf(row.id)});
                        db.insertTrip(row.id, dest, country, duration, price, rating, desc, image);

                        loadTrips();
                        Toast.makeText(requireContext(), "Trip updated successfully!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}