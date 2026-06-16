package com.example.a1212072_1212011_encs5150;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class ReservationsFragment extends Fragment {

    private RecyclerView rvReservations;
    private LinearLayout emptyState;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReservations = view.findViewById(R.id.rvReservations);
        emptyState = view.findViewById(R.id.emptyStateReservations);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", "");
        currentUserId = dbHelper.getUserIdByEmail(email);

        loadReservations();
    }

    private void loadReservations() {
        List<ReservationItem> list = new ArrayList<>();

        Cursor cursor = dbHelper.getUserReservations(currentUserId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ReservationItem item = new ReservationItem();
                item.tripName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESTINATION));
                item.date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RESERVATION_DATE));
                item.quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY));
                item.type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RESERVATION_TYPE));
                item.status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS));
                list.add(item);
            }
            cursor.close();
        }

        if (list.isEmpty()) {
            rvReservations.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvReservations.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            ReservationAdapter adapter = new ReservationAdapter(list);
            rvReservations.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvReservations.setAdapter(adapter);
        }
    }

    public static class ReservationItem {
        String tripName, date, type, status;
        int quantity;
    }

    static class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {
        private final List<ReservationItem> list;

        ReservationAdapter(List<ReservationItem> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reservation, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ReservationItem item = list.get(position);
            h.tvTrip.setText(item.tripName);
            h.tvDate.setText("📅 " + item.date);
            h.tvDetails.setText("👥 " + item.quantity + " × " + item.type);

            int color;
            String statusLabel;
            switch (item.status != null ? item.status : "active") {
                case "completed":
                    color = 0xFF306D29;
                    statusLabel = "✓ Completed";
                    break;
                case "cancelled":
                    color = 0xFFA32D2D;
                    statusLabel = "✗ Cancelled";
                    break;
                default:
                    color = 0xFF2FA084;
                    statusLabel = "● Active";
                    break;
            }
            h.tvStatus.setText(statusLabel);
            h.tvStatus.setTextColor(color);
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTrip, tvDate, tvDetails, tvStatus;
            VH(View v) {
                super(v);
                tvTrip = v.findViewById(R.id.tvReservationTrip);
                tvDate = v.findViewById(R.id.tvReservationDate);
                tvDetails = v.findViewById(R.id.tvReservationDetails);
                tvStatus = v.findViewById(R.id.tvReservationStatus);
            }
        }
    }
}