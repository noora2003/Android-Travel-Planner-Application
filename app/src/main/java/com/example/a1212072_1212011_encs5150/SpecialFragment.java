package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class SpecialFragment extends Fragment implements TripAdapter.OnTripClickListener {

    private RecyclerView rvSpecial;
    private LinearLayout emptyState;
    private TextView tvSpecialTitle;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;
    private List<TripModel> specialList;
    private TripAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_special, container, false);

        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        view.startAnimation(fadeIn);

        rvSpecial = view.findViewById(R.id.rvSpecial);
        emptyState = view.findViewById(R.id.emptyStateSpecial);
        tvSpecialTitle = view.findViewById(R.id.tvSpecialTitle);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        specialList = new ArrayList<>();

        adapter = new TripAdapter(requireContext(), specialList, this);

        rvSpecial.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSpecial.setAdapter(adapter);

        loadSpecialTrips();

        return view;
    }

    private void loadSpecialTrips() {
        specialList.clear();
        Cursor cursor = dbHelper.getAllTrips();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RATING));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE));

                if (rating >= 4.7 || price < 300) {
                    int tripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
                    TripModel trip = new TripModel(
                            tripId,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COUNTRY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION)),
                            price,
                            rating,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE))
                    );
                    trip.setFavorite(dbHelper.isFavorite(loggedInEmail, tripId));
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
            tvSpecialTitle.setText("⭐ " + specialList.size() + " Special Offers");
        }
    }

    @Override
    public void onTripClick(TripModel trip) {
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(trip.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFavoriteClick(TripModel trip, int position) {
        boolean isFav = dbHelper.isFavorite(loggedInEmail, trip.getId());
        if (isFav) {
            dbHelper.removeFavorite(loggedInEmail, trip.getId());
            trip.setFavorite(false);
            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addFavorite(loggedInEmail, trip.getId());
            trip.setFavorite(true);
            Toast.makeText(requireContext(), "Added to favorites ♡", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onReserveClick(TripModel trip) {
        ReservationDialog dialog = new ReservationDialog(
                requireContext(), trip, loggedInEmail, dbHelper);
        dialog.show();
    }
}