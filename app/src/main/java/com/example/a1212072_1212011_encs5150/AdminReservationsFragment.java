package com.example.a1212072_1212011_encs5150;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminReservationsFragment extends Fragment {

    private RecyclerView rv;
    private DatabaseHelper db;
    private List<ReservationRow> reservationList = new ArrayList<>();
    private ReservationAdapter adapter;
    private TextView tvCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        TextView tvHeader = view.findViewById(R.id.tvAdminListHeader);
        tvHeader.setText("All Reservations");

        tvCount = view.findViewById(R.id.tvAdminListCount);

        view.findViewById(R.id.btnAddItem).setVisibility(View.GONE);

        rv = view.findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservationAdapter();
        rv.setAdapter(adapter);

        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
        loadReservations();
    }

    private void loadReservations() {
        reservationList.clear();
        Cursor c = db.getAllReservations();
        if (c != null) {
            while (c.moveToNext()) {
                ReservationRow row = new ReservationRow();
                row.id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_ID));
                row.tripName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TRIP_NAME));
                row.userEmail = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_EMAIL));
                row.date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_DATE));
                row.quantity = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_QUANTITY));
                row.type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TYPE));
                row.status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RES_STATUS));
                reservationList.add(row);
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        tvCount.setText(reservationList.size() + " total reservation" + (reservationList.size() != 1 ? "s" : ""));
    }

    static class ReservationRow {
        int id, quantity;
        String tripName, userEmail, date, type, status;
    }

    class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ReservationRow row = reservationList.get(position);

            holder.tvTitle.setText(row.tripName);
            holder.tvSub1.setText("👤 " + row.userEmail + "  ·  👥 " + row.quantity + "  ·  " + row.type);
            holder.tvSub2.setText("📅 " + row.date + "  ·  " + formatStatus(row.status));

            if (holder.tvAvatar != null && row.tripName.length() > 0) {
                holder.tvAvatar.setText(String.valueOf(row.tripName.charAt(0)).toUpperCase());
            }

            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        private String formatStatus(String status) {
            if (status == null) return "● Active";
            switch (status) {
                case "completed": return "✓ Completed";
                case "cancelled": return "✗ Cancelled";
                default: return "● Active";
            }
        }

        @Override public int getItemCount() { return reservationList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub1, tvSub2, tvAvatar;
            android.widget.Button btnDelete, btnEdit;
            VH(View v) {
                super(v);
                tvTitle   = v.findViewById(R.id.tvRowTitle);
                tvSub1    = v.findViewById(R.id.tvRowSub1);
                tvSub2    = v.findViewById(R.id.tvRowSub2);
                tvAvatar  = v.findViewById(R.id.tvAvatarLetter);
                btnDelete = v.findViewById(R.id.btnRowDelete);
                btnEdit   = v.findViewById(R.id.btnRowEdit);
            }
        }
    }
}