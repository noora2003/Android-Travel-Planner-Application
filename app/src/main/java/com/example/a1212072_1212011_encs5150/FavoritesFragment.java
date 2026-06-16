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
        emptyState = view.findViewById(R.id.emptyStateFavorites);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        favoriteList = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), favoriteList, new TripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripModel trip) {
                Toast.makeText(requireContext(), trip.getDestination(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFavoriteClick(TripModel trip, int position) {
                dbHelper.removeFavorite(currentUserId, trip.getId());
                favoriteList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, favoriteList.size());
                updateEmptyState();
                Toast.makeText(requireContext(), trip.getDestination() + " removed from favorites", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReserveClick(TripModel trip) {
                Toast.makeText(requireContext(), "Reserve: " + trip.getDestination(), Toast.LENGTH_SHORT).show();
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

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
}