package com.example.a1212072_1212011_encs5150;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseHelper db = new DatabaseHelper(requireContext());

        TextView tvDate          = view.findViewById(R.id.tvAdminDate);
        TextView tvUsers         = view.findViewById(R.id.tvStatUsers);
        TextView tvTrips         = view.findViewById(R.id.tvStatTrips);
        TextView tvReservations  = view.findViewById(R.id.tvStatReservations);
        Button   btnQuickUsers   = view.findViewById(R.id.btnQuickUsers);
        Button   btnQuickTrips   = view.findViewById(R.id.btnQuickTrips);
        Button   btnQuickRes     = view.findViewById(R.id.btnQuickReservations);

        // التاريخ الحالي
        String date = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(new Date());
        tvDate.setText(date);

        // قراءة الإحصائيات
        int userCount = 0, tripCount = 0, resCount = 0;

        Cursor cU = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS, null);
        if (cU.moveToFirst()) userCount = cU.getInt(0);
        cU.close();

        Cursor cT = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TRIPS, null);
        if (cT.moveToFirst()) tripCount = cT.getInt(0);
        cT.close();

        Cursor cR = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RESERVATIONS, null);
        if (cR.moveToFirst()) resCount = cR.getInt(0);
        cR.close();

        // Animate counter from 0 to target
        animateCount(tvUsers, userCount);
        animateCount(tvTrips, tripCount);
        animateCount(tvReservations, resCount);

        // Fade-in animation on cards
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));

        // Quick action buttons → navigate to matching fragment
        btnQuickUsers.setOnClickListener(v -> {
            btnQuickUsers.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity())
                        .loadFragment(new AdminUsersFragment(), "Manage Users");
            }
        });

        btnQuickTrips.setOnClickListener(v -> {
            btnQuickTrips.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity())
                        .loadFragment(new AdminTripsFragment(), "Manage Trips");
            }
        });

        btnQuickRes.setOnClickListener(v -> {
            btnQuickRes.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity())
                        .loadFragment(new AdminReservationsFragment(), "View Reservations");
            }
        });
    }

    // Counter animation: 0 → target in ~600ms
    private void animateCount(TextView tv, int target) {
        int steps = 20;
        long delay = 600L / steps;
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 1; i <= steps; i++) {
            final int value = (int) Math.round(target * (i / (double) steps));
            handler.postDelayed(() -> tv.setText(String.valueOf(value)), delay * i);
        }
    }
}