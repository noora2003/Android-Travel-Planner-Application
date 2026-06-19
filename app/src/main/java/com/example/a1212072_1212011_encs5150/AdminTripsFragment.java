package com.example.a1212072_1212011_encs5150;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminTripsFragment extends Fragment {

    private RecyclerView rvTrips;
    private DatabaseHelper db;
    private List<TripRow> tripList = new ArrayList<>();
    private TripAdminAdapter adapter;
    private TextView tvCount;

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

        tvCount = view.findViewById(R.id.tvAdminListCount);

        android.widget.Button btnAdd = view.findViewById(R.id.btnAddItem);
        btnAdd.setVisibility(View.VISIBLE);
        btnAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
            showAddTripDialog();
        });

        rvTrips = view.findViewById(R.id.rvAdminList);
        rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdminAdapter();
        rvTrips.setAdapter(adapter);

        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
        loadTrips();
    }

    private void loadTrips() {
        tripList.clear();
        Cursor c = db.getAllTrips();
        if (c != null) {
            while (c.moveToNext()) {
                TripRow row = new TripRow();
                row.id          = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                row.destination = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION));
                row.country     = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY));
                row.price       = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE));
                row.rating      = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_RATING));
                row.duration    = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION));
                row.description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                row.image       = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE));
                tripList.add(row);
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        tvCount.setText(tripList.size() + " trip" + (tripList.size() != 1 ? "s" : "") + " in database");
    }

    static class TripRow {
        int id, duration;
        double price, rating;
        String destination, country, description, image;
    }

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
            holder.tvSub1.setText("★ " + row.rating + "  ·  $" + (int) row.price + "  ·  " + row.duration + " days");
            holder.tvSub2.setText(row.description.length() > 55
                    ? row.description.substring(0, 55) + "…" : row.description);

            if (holder.tvAvatar != null && row.destination.length() > 0) {
                holder.tvAvatar.setText(String.valueOf(row.destination.charAt(0)).toUpperCase());
            }

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Trip")
                        .setMessage("Delete trip to " + row.destination + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.getWritableDatabase().delete(DatabaseHelper.TABLE_TRIPS,
                                    DatabaseHelper.COL_TRIP_ID + "=?",
                                    new String[]{String.valueOf(row.id)});
                            tripList.remove(position);
                            notifyDataSetChanged();
                            tvCount.setText(tripList.size() + " trip" + (tripList.size() != 1 ? "s" : "") + " in database");
                            Toast.makeText(requireContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null).show();
            });

            holder.btnEdit.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
                showEditTripDialog(row, position);
            });
        }

        @Override public int getItemCount() { return tripList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub1, tvSub2, tvAvatar;
            android.widget.Button btnDelete, btnEdit;
            VH(View v) {
                super(v);
                tvTitle   = v.findViewById(R.id.tvRowTitle);
                tvSub1    = v.findViewById(R.id.tvRowSub1);
                tvSub2    = v.findViewById(R.id.tvRowSub2);
                tvAvatar  = v.findViewById(R.id.tvAvatarLetter);
                btnDelete = v.findViewById(R.id.btnRowDelete);
                btnEdit   = v.findViewById(R.id.btnRowEdit);
                btnEdit.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showAddTripDialog() {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trip, null);
        EditText etDest     = v.findViewById(R.id.etTripDest);
        EditText etCountry  = v.findViewById(R.id.etTripCountry);
        EditText etDuration = v.findViewById(R.id.etTripDuration);
        EditText etPrice    = v.findViewById(R.id.etTripPrice);
        EditText etRating   = v.findViewById(R.id.etTripRating);
        EditText etDesc     = v.findViewById(R.id.etTripDesc);
        EditText etImage    = v.findViewById(R.id.etTripImage);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Trip")
                .setView(v)
                .setPositiveButton("Add", (d, w) -> {
                    try {
                        String dest    = etDest.getText().toString().trim();
                        String country = etCountry.getText().toString().trim();
                        String durStr  = etDuration.getText().toString().trim();
                        String prStr   = etPrice.getText().toString().trim();
                        String ratStr  = etRating.getText().toString().trim();
                        String desc    = etDesc.getText().toString().trim();
                        String image   = etImage.getText().toString().trim();

                        if (dest.isEmpty() || country.isEmpty() || durStr.isEmpty()
                                || prStr.isEmpty() || ratStr.isEmpty() || desc.isEmpty()) {
                            Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int    duration = Integer.parseInt(durStr);
                        double price    = Double.parseDouble(prStr);
                        double rating   = Double.parseDouble(ratStr);
                        int    newId    = tripList.isEmpty() ? 1000 : tripList.get(tripList.size() - 1).id + 1;

                        db.insertTrip(newId, dest, country, duration, price, rating, desc, image);
                        loadTrips();
                        Toast.makeText(requireContext(), "Trip added successfully!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showEditTripDialog(TripRow row, int position) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trip, null);
        EditText etDest     = v.findViewById(R.id.etTripDest);
        EditText etCountry  = v.findViewById(R.id.etTripCountry);
        EditText etDuration = v.findViewById(R.id.etTripDuration);
        EditText etPrice    = v.findViewById(R.id.etTripPrice);
        EditText etRating   = v.findViewById(R.id.etTripRating);
        EditText etDesc     = v.findViewById(R.id.etTripDesc);
        EditText etImage    = v.findViewById(R.id.etTripImage);

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
                        String dest    = etDest.getText().toString().trim();
                        String country = etCountry.getText().toString().trim();
                        int    duration = Integer.parseInt(etDuration.getText().toString().trim());
                        double price   = Double.parseDouble(etPrice.getText().toString().trim());
                        double rating  = Double.parseDouble(etRating.getText().toString().trim());
                        String desc    = etDesc.getText().toString().trim();
                        String image   = etImage.getText().toString().trim();

                        if (dest.isEmpty() || country.isEmpty() || desc.isEmpty()) {
                            Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        db.getWritableDatabase().delete(DatabaseHelper.TABLE_TRIPS,
                                DatabaseHelper.COL_TRIP_ID + "=?", new String[]{String.valueOf(row.id)});
                        db.insertTrip(row.id, dest, country, duration, price, rating, desc, image);
                        loadTrips();
                        Toast.makeText(requireContext(), "Trip updated!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
}