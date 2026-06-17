package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements TripAdapter.OnTripClickListener {

    private RecyclerView rvFavorites;
    private TripAdapter adapter;
    private List<TripModel> favoriteList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;
    private String loggedInEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper    = new DatabaseHelper(requireContext());
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmpty     = view.findViewById(R.id.tvFavEmpty);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TravelPlannerPrefs", requireActivity().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdapter(requireContext(), favoriteList, this);
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList.clear();
        Cursor cursor = dbHelper.getFavoriteTrips(loggedInEmail);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int    id          = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                String destination = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION));
                String country     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY));
                int    duration    = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION));
                double price       = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE));
                double rating      = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RATING));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                String image       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE));

                TripModel trip = new TripModel(id, destination, country, duration, price, rating, description, image);
                trip.setFavorite(true); // already favorite
                favoriteList.add(trip);
            }
            cursor.close();
        }

        adapter.updateList(favoriteList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (favoriteList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTripClick(TripModel trip) {
        // Open TripDetailFragment
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(trip);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFavoriteClick(TripModel trip, int position) {
        // Remove from favorites
        dbHelper.removeFavorite(loggedInEmail, trip.getId());
        favoriteList.remove(position);
        adapter.notifyItemRemoved(position);
        updateEmptyState();
        Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReserveClick(TripModel trip) {
        // Open reservation dialog directly from favorites
        ReservationDialog dialog = new ReservationDialog(
                requireContext(), trip, loggedInEmail, dbHelper);
        dialog.show();
    }
}