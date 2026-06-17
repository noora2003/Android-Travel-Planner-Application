package com.example.a1212072_1212011_encs5150;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

public class TripDetailFragment extends Fragment {

    private static final String ARG_ID          = "id";
    private static final String ARG_DESTINATION = "destination";
    private static final String ARG_COUNTRY     = "country";
    private static final String ARG_DURATION    = "duration";
    private static final String ARG_PRICE       = "price";
    private static final String ARG_RATING      = "rating";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE       = "image";

    private TripModel trip;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;
    private ImageView btnFavorite;

    // Create a new instance and pass the trip data as arguments
    public static TripDetailFragment newInstance(TripModel trip) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, trip.getId());
        args.putString(ARG_DESTINATION, trip.getDestination());
        args.putString(ARG_COUNTRY, trip.getCountry());
        args.putInt(ARG_DURATION, trip.getDurationDays());
        args.putDouble(ARG_PRICE, trip.getPrice());
        args.putDouble(ARG_RATING, trip.getRating());
        args.putString(ARG_DESCRIPTION, trip.getDescription());
        args.putString(ARG_IMAGE, trip.getImage());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_detail, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TravelPlannerPrefs", requireActivity().MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        // Rebuild trip model from arguments
        Bundle args = getArguments();
        if (args != null) {
            trip = new TripModel(
                    args.getInt(ARG_ID),
                    args.getString(ARG_DESTINATION),
                    args.getString(ARG_COUNTRY),
                    args.getInt(ARG_DURATION),
                    args.getDouble(ARG_PRICE),
                    args.getDouble(ARG_RATING),
                    args.getString(ARG_DESCRIPTION),
                    args.getString(ARG_IMAGE)
            );
            trip.setFavorite(dbHelper.isFavorite(loggedInEmail, trip.getId()));
        }

        bindViews(view);
        return view;
    }

    private void bindViews(View view) {
        ImageView ivHeroImage   = view.findViewById(R.id.ivHeroImage);
        ImageView btnBack       = view.findViewById(R.id.btnBack);
        btnFavorite             = view.findViewById(R.id.btnFavorite);
        TextView tvDestination  = view.findViewById(R.id.tvDestination);
        TextView tvCountry      = view.findViewById(R.id.tvCountry);
        TextView tvRating       = view.findViewById(R.id.tvRating);
        TextView tvDuration     = view.findViewById(R.id.tvDuration);
        TextView tvPrice        = view.findViewById(R.id.tvPrice);
        TextView tvDescription  = view.findViewById(R.id.tvDescription);
        TextView tvCountryDetail = view.findViewById(R.id.tvCountryDetail);
        TextView tvDurationDetail = view.findViewById(R.id.tvDurationDetail);
        TextView tvRatingDetail = view.findViewById(R.id.tvRatingDetail);
        TextView tvPriceDetail  = view.findViewById(R.id.tvPriceDetail);
        TextView tvBottomPrice  = view.findViewById(R.id.tvBottomPrice);
        Button btnReserveNow    = view.findViewById(R.id.btnReserveNow);

        // Load hero image
        Glide.with(requireContext())
                .load(trip.getImage())
                .centerCrop()
                .placeholder(R.drawable.ic_plane)
                .into(ivHeroImage);

        // Set all text fields with full API data
        tvDestination.setText(trip.getDestination());
        tvCountry.setText(trip.getCountry());
        tvRating.setText("★ " + trip.getRating());
        tvDuration.setText(trip.getDurationDays() + "d");
        tvPrice.setText("$" + (int) trip.getPrice());
        tvDescription.setText(trip.getDescription());

        tvCountryDetail.setText(trip.getCountry());
        tvDurationDetail.setText(trip.getDurationDays() + " Days");
        tvRatingDetail.setText(trip.getRating() + " / 5");
        tvPriceDetail.setText("$" + (int) trip.getPrice());

        tvBottomPrice.setText("$" + (int) trip.getPrice());

        updateFavoriteIcon();

        // Back button — pop fragment from stack
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Favorite toggle
        btnFavorite.setOnClickListener(v -> {
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
            updateFavoriteIcon();
        });

        // Reserve Now — opens the SAME reservation dialog used in TripsFragment
        btnReserveNow.setOnClickListener(v -> {
            ReservationDialog dialog = new ReservationDialog(
                    requireContext(), trip, loggedInEmail, dbHelper);
            dialog.show();
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                trip.isFavorite()
                        ? R.drawable.ic_favorite_filled
                        : R.drawable.ic_favorite_border);
    }
}