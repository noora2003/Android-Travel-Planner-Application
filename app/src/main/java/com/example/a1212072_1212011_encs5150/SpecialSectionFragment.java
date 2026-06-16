package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class SpecialSectionFragment extends Fragment {

    private RecyclerView rvSpecial;
    private LinearLayout emptyState;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private List<TripModel> specialList;
    private TripAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_special, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSpecial = view.findViewById(R.id.rvSpecial);
        emptyState = view.findViewById(R.id.emptyStateSpecial);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        specialList = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), specialList, new TripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripModel trip) {
                Toast.makeText(requireContext(), trip.getDestination(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Reserve: " + trip.getDestination(), Toast.LENGTH_SHORT).show();
            }
        });

        rvSpecial.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSpecial.setAdapter(adapter);

        loadSpecialTrips();
    }

    private void loadSpecialTrips() {
        specialList.clear();
        Cursor cursor = dbHelper.getAllTrips();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RATING));
                if (rating >= 4.7) {
                    int tripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                    TripModel trip = new TripModel(
                            tripId,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE)),
                            rating,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE))
                    );
                    trip.setFavorite(dbHelper.isFavorite(currentUserId, trip.getId()));
                    specialList.add(trip);
                }
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();

        if (specialList.isEmpty()) {
            rvSpecial.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvSpecial.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}