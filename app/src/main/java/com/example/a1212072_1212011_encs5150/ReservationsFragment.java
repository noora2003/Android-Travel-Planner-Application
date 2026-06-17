package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class ReservationsFragment extends Fragment {

    private LinearLayout reservationsContainer;
    private TextView tvEmpty;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservations, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper             = new DatabaseHelper(requireContext());
        reservationsContainer = view.findViewById(R.id.reservationsContainer);
        tvEmpty              = view.findViewById(R.id.tvEmpty);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TravelPlannerPrefs", requireActivity().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        loadReservations();
    }

    private void loadReservations() {
        reservationsContainer.removeAllViews();
        Cursor cursor = dbHelper.getUserReservations(loggedInEmail);

        if (cursor == null || cursor.getCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            reservationsContainer.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        reservationsContainer.setVisibility(View.VISIBLE);

        while (cursor.moveToNext()) {
            String tripName   = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TRIP_NAME));
            String date       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_DATE));
            String status     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_STATUS));
            int    quantity   = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_QUANTITY));
            String type       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TYPE));
            double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TOTAL_PRICE));

            // Build card programmatically
            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setCardElevation(4);
            card.setRadius(24);
            card.setCardBackgroundColor(0xFFFFFFFF);

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(40, 36, 40, 36);

            // Trip name row
            LinearLayout topRow = new LinearLayout(requireContext());
            topRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(requireContext());
            tvName.setText(tripName);
            tvName.setTextSize(16);
            tvName.setTextColor(0xFF0D530E);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameParams);

            TextView tvStatus = new TextView(requireContext());
            tvStatus.setText(status);
            tvStatus.setTextSize(12);
            tvStatus.setPadding(16, 6, 16, 6);
            if ("Confirmed".equals(status)) {
                tvStatus.setBackgroundColor(0xFF1B5E20);
                tvStatus.setTextColor(0xFFC8E6C9);
            } else {
                tvStatus.setBackgroundColor(0xFFFF8F00);
                tvStatus.setTextColor(0xFFFFFFFF);
            }

            topRow.addView(tvName);
            topRow.addView(tvStatus);

            // Divider
            View divider = new View(requireContext());
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divParams.setMargins(0, 16, 0, 16);
            divider.setLayoutParams(divParams);
            divider.setBackgroundColor(0xFFEEEEEE);

            // Details
            TextView tvDetails = new TextView(requireContext());
            tvDetails.setText(
                    "📅  Date: " + date + "\n" +
                            "🎫  Type: " + type + "\n" +
                            "👤  Quantity: " + quantity + " traveler(s)\n" +
                            "💰  Total: $" + String.format("%.0f", totalPrice)
            );
            tvDetails.setTextSize(13);
            tvDetails.setTextColor(0xFF555555);
            tvDetails.setLineSpacing(6, 1);

            inner.addView(topRow);
            inner.addView(divider);
            inner.addView(tvDetails);
            card.addView(inner);
            reservationsContainer.addView(card);
        }
        cursor.close();
    }
}