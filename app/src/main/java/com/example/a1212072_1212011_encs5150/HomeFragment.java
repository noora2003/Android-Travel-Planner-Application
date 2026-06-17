package com.example.a1212072_1212011_encs5150;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String loggedInEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Add fade + slide up animation when Home loads
        android.view.animation.Animation fadeSlide = android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.fade_in);

        view.findViewById(R.id.tvWelcome).startAnimation(fadeSlide);

        dbHelper = new DatabaseHelper(requireContext());

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TravelPlannerPrefs", requireActivity().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        TextView tvWelcome         = view.findViewById(R.id.tvWelcome);
        TextView tvTripCount       = view.findViewById(R.id.tvTripCount);
        TextView tvReservationCount = view.findViewById(R.id.tvReservationCount);
        TextView tvFavoriteCount   = view.findViewById(R.id.tvFavoriteCount);

        // Set welcome message with user's first name
        Cursor cursor = dbHelper.getUserByEmail(loggedInEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            tvWelcome.setText("Hello, " + firstName + "! 👋");
            cursor.close();
        }

        // Count trips
        Cursor tripsCursor = dbHelper.getAllTrips();
        if (tripsCursor != null) {
            tvTripCount.setText(String.valueOf(tripsCursor.getCount()));
            tripsCursor.close();
        }

        // Count reservations
        Cursor resCursor = dbHelper.getUserReservations(loggedInEmail);
        if (resCursor != null) {
            tvReservationCount.setText(String.valueOf(resCursor.getCount()));
            resCursor.close();
        }

        // Count favorites
        Cursor favCursor = dbHelper.getFavoriteTrips(loggedInEmail);
        if (favCursor != null) {
            tvFavoriteCount.setText(String.valueOf(favCursor.getCount()));
            favCursor.close();
        }

        return view;
    }
}
