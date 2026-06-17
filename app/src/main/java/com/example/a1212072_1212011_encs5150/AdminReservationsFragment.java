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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdminReservationsFragment extends Fragment {

    private RecyclerView rv;
    private DatabaseHelper db;
    private List<ReservationRow> reservationList = new ArrayList<>();
    private ReservationAdapter adapter;

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

        view.findViewById(R.id.btnAddItem).setVisibility(View.GONE);

        rv = view.findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservationAdapter();
        rv.setAdapter(adapter);
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
            holder.tvSub1.setText("👤 " + row.userEmail + "  |  👥 " + row.quantity);
            holder.tvSub2.setText("📅 " + row.date + "  |  " + row.type + "  |  Status: " + row.status);

            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        @Override public int getItemCount() { return reservationList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub1, tvSub2;
            android.widget.Button btnDelete, btnEdit;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvRowTitle);
                tvSub1 = v.findViewById(R.id.tvRowSub1);
                tvSub2 = v.findViewById(R.id.tvRowSub2);
                btnDelete = v.findViewById(R.id.btnRowDelete);
                btnEdit = v.findViewById(R.id.btnRowEdit);
            }
        }
    }
}