package com.example.a1212072_1212011_encs5150;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment implements TripAdapter.OnTripClickListener {

    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private List<TripModel> allTrips      = new ArrayList<>();
    private List<TripModel> filteredTrips = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvResultCount;
    private LinearLayout emptyState;
    private String currentFilter  = "All";
    private String loggedInEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TravelPlannerPrefs", requireActivity().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        rvTrips       = view.findViewById(R.id.rvTrips);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        emptyState    = view.findViewById(R.id.emptyState);
        EditText etSearch = view.findViewById(R.id.etSearch);

        // Setup RecyclerView
        rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdapter(requireContext(), filteredTrips, this);
        rvTrips.setAdapter(adapter);

        loadTrips();
        setupFilterChips(view);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString(), currentFilter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadTrips() {
        allTrips.clear();
        Cursor cursor = dbHelper.getAllTrips();
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

                TripModel trip = new TripModel(id, destination, country,
                        duration, price, rating, description, image);
                trip.setFavorite(dbHelper.isFavorite(loggedInEmail, id));
                allTrips.add(trip);
            }
            cursor.close();
        }
        filterTrips("", currentFilter);
    }

    private void setupFilterChips(View view) {
        LinearLayout filterContainer = view.findViewById(R.id.filterContainer);
        String[] filters = {"All", "Beach", "Adventure", "Cultural", "Luxury", "Budget"};

        for (String filter : filters) {
            TextView chip = new TextView(requireContext());
            chip.setText(filter);
            chip.setTextSize(11);
            chip.setPadding(28, 14, 28, 14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            chip.setLayoutParams(params);

            updateChipStyle(chip, filter.equals(currentFilter));

            chip.setOnClickListener(v -> {
                currentFilter = filter;
                for (int i = 0; i < filterContainer.getChildCount(); i++) {
                    TextView c = (TextView) filterContainer.getChildAt(i);
                    updateChipStyle(c, c.getText().toString().equals(currentFilter));
                }
                EditText etSearch = view.findViewById(R.id.etSearch);
                filterTrips(etSearch.getText().toString(), currentFilter);
            });

            filterContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.chip_active_bg);
            chip.setTextColor(0xFFEEEEEE);
        } else {
            chip.setBackgroundResource(R.drawable.chip_inactive_bg);
            chip.setTextColor(0xFF2FA084);
        }
    }

    private void filterTrips(String query, String filter) {
        filteredTrips.clear();
        for (TripModel trip : allTrips) {
            boolean matchesSearch = query.isEmpty()
                    || trip.getDestination().toLowerCase().contains(query.toLowerCase())
                    || trip.getCountry().toLowerCase().contains(query.toLowerCase())
                    || trip.getDescription().toLowerCase().contains(query.toLowerCase());

            boolean matchesFilter = filter.equals("All")
                    || trip.getDescription().toLowerCase().contains(filter.toLowerCase())
                    || trip.getDestination().toLowerCase().contains(filter.toLowerCase());

            if (matchesSearch && matchesFilter) filteredTrips.add(trip);
        }

        adapter.updateList(filteredTrips);
        tvResultCount.setText(filteredTrips.size() + " trips found");

        if (filteredTrips.isEmpty()) {
            rvTrips.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvTrips.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTripClick(TripModel trip) {
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(trip);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFavoriteClick(TripModel trip, int position) {
        if (trip.isFavorite()) {
            dbHelper.removeFavorite(loggedInEmail, trip.getId());
            trip.setFavorite(false);
            Toast.makeText(requireContext(),
                    "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addFavorite(loggedInEmail, trip.getId());
            trip.setFavorite(true);
            Toast.makeText(requireContext(),
                    "Added to favorites!", Toast.LENGTH_SHORT).show();
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
