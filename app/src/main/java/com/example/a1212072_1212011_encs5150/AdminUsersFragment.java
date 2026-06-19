package com.example.a1212072_1212011_encs5150;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView rv;
    private DatabaseHelper db;
    private List<UserRow> userList = new ArrayList<>();
    private UserAdapter adapter;
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
        tvHeader.setText("Registered Users");

        tvCount = view.findViewById(R.id.tvAdminListCount);

        // إخفاء زر الإضافة — المستخدمون لا يُضافون يدوياً
        view.findViewById(R.id.btnAddItem).setVisibility(View.GONE);

        rv = view.findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter();
        rv.setAdapter(adapter);

        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        Cursor c = db.getReadableDatabase().query(
                DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);
        while (c != null && c.moveToNext()) {
            UserRow row = new UserRow();
            row.id    = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            row.name  = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME))
                    + " " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            row.email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
            row.phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            row.gender = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER));
            userList.add(row);
        }
        if (c != null) c.close();
        adapter.notifyDataSetChanged();
        tvCount.setText(userList.size() + " registered user" + (userList.size() != 1 ? "s" : ""));
    }

    static class UserRow { int id; String name, email, phone, gender; }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_admin_row, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            UserRow row = userList.get(pos);
            h.tvTitle.setText(row.name);
            h.tvSub1.setText(row.email);
            h.tvSub2.setText((row.phone != null && !row.phone.isEmpty() ? row.phone : "—")
                    + "  ·  " + (row.gender != null ? row.gender : "—"));

            // Avatar: أول حرف من الاسم
            if (h.tvAvatar != null && row.name.length() > 0) {
                h.tvAvatar.setText(String.valueOf(row.name.charAt(0)).toUpperCase());
            }

            h.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete User")
                        .setMessage("Delete " + row.name + "?\nThis will also remove their reservations and favorites.")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.getWritableDatabase().delete(DatabaseHelper.TABLE_USERS,
                                    DatabaseHelper.COL_USER_ID + "=?",
                                    new String[]{String.valueOf(row.id)});
                            userList.remove(pos);
                            notifyDataSetChanged();
                            tvCount.setText(userList.size() + " registered user" + (userList.size() != 1 ? "s" : ""));
                            Toast.makeText(requireContext(), "User deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null).show();
            });
        }

        @Override public int getItemCount() { return userList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub1, tvSub2, tvAvatar;
            android.widget.Button btnDelete;
            VH(View v) {
                super(v);
                tvTitle  = v.findViewById(R.id.tvRowTitle);
                tvSub1   = v.findViewById(R.id.tvRowSub1);
                tvSub2   = v.findViewById(R.id.tvRowSub2);
                tvAvatar = v.findViewById(R.id.tvAvatarLetter);
                btnDelete = v.findViewById(R.id.btnRowDelete);
            }
        }
    }
}