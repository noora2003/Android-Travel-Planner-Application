package com.example.a1212072_1212011_encs5150;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        TextView tvUsers = view.findViewById(R.id.tvStatUsers);
        TextView tvTrips = view.findViewById(R.id.tvStatTrips);
        TextView tvReservations = view.findViewById(R.id.tvStatReservations);

        // Count users
        Cursor cU = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS, null);
        if (cU.moveToFirst()) tvUsers.setText(String.valueOf(cU.getInt(0)));
        cU.close();

        // Count trips
        Cursor cT = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TRIPS, null);
        if (cT.moveToFirst()) tvTrips.setText(String.valueOf(cT.getInt(0)));
        cT.close();

        // Count reservations
        Cursor cR = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RESERVATIONS, null);
        if (cR.moveToFirst()) tvReservations.setText(String.valueOf(cR.getInt(0)));
        cR.close();
    }
}