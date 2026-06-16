package com.example.a1212072_1212011_encs5150;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView ivProfilePic;
    private TextView tvName, tvEmail, tvGender, tvCategory, tvPhone;

    private TextInputLayout tilFirstName, tilLastName, tilPhone,
            tilOldPass, tilNewPass, tilConfirmPass;
    private TextInputEditText etFirstName, etLastName, etPhone,
            etOldPass, etNewPass, etConfirmPass;
    private Button btnSaveProfile, btnChangePass, btnPickPhoto;

    private DatabaseHelper dbHelper;
    private String currentEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        currentEmail = prefs.getString("logged_in_email", "");

        // Info TextViews
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName       = view.findViewById(R.id.tvProfileName);
        tvEmail      = view.findViewById(R.id.tvProfileEmail);
        tvGender     = view.findViewById(R.id.tvProfileGender);
        tvCategory   = view.findViewById(R.id.tvProfileCategory);
        tvPhone      = view.findViewById(R.id.tvProfilePhone);

        // Edit fields
        tilFirstName   = view.findViewById(R.id.tilFirstName);
        tilLastName    = view.findViewById(R.id.tilLastName);
        tilPhone       = view.findViewById(R.id.tilPhone);
        etFirstName    = view.findViewById(R.id.etFirstName);
        etLastName     = view.findViewById(R.id.etLastName);
        etPhone        = view.findViewById(R.id.etPhone);

        tilOldPass     = view.findViewById(R.id.tilOldPass);
        tilNewPass     = view.findViewById(R.id.tilNewPass);
        tilConfirmPass = view.findViewById(R.id.tilConfirmPass);
        etOldPass      = view.findViewById(R.id.etOldPass);
        etNewPass      = view.findViewById(R.id.etNewPass);
        etConfirmPass  = view.findViewById(R.id.etConfirmPass);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnChangePass  = view.findViewById(R.id.btnChangePass);
        btnPickPhoto   = view.findViewById(R.id.btnPickPhoto);

        loadUserData();

        btnPickPhoto.setOnClickListener(v -> openImagePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePass.setOnClickListener(v -> changePassword());
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUserByEmail(currentEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            String phone     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            String gender    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER));
            String category  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY));

            tvName.setText(firstName + " " + lastName);
            tvEmail.setText(currentEmail);
            tvGender.setText(gender != null ? gender : "—");
            tvCategory.setText(category != null ? category : "—");
            tvPhone.setText(phone != null ? phone : "—");

            etFirstName.setText(firstName);
            etLastName.setText(lastName);
            etPhone.setText(phone);
            cursor.close();
        }
    }

    private void saveProfile() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);

        String first = etFirstName.getText().toString().trim();
        String last  = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        boolean hasError = false;
        if (first.length() < 3) {
            tilFirstName.setError("Minimum 3 characters");
            hasError = true;
        }
        if (last.length() < 3) {
            tilLastName.setError("Minimum 3 characters");
            hasError = true;
        }
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone is required");
            hasError = true;
        }
        if (hasError) return;

        boolean ok = dbHelper.updateUser(currentEmail, first, last, phone);
        if (ok) {
            tvName.setText(first + " " + last);
            tvPhone.setText(phone);
            Toast.makeText(requireContext(), "Profile updated successfully ✓", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Update failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        tilOldPass.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        String oldPass  = etOldPass.getText().toString().trim();
        String newPass  = etNewPass.getText().toString().trim();
        String confPass = etConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(oldPass)) { tilOldPass.setError("Enter current password"); return; }
        if (!PasswordUtils.isPasswordValid(newPass)) {
            tilNewPass.setError("Min 6 chars, at least 1 letter & 1 number"); return;
        }
        if (!newPass.equals(confPass)) { tilConfirmPass.setError("Passwords don't match"); return; }

        // Verify current password
        String hashedOld = PasswordUtils.hashPassword(oldPass);
        if (!dbHelper.checkUser(currentEmail, hashedOld)) {
            tilOldPass.setError("Current password is incorrect");
            return;
        }

        String hashedNew = PasswordUtils.hashPassword(newPass);
        boolean ok = dbHelper.updatePassword(currentEmail, hashedNew);
        if (ok) {
            etOldPass.setText("");
            etNewPass.setText("");
            etConfirmPass.setText("");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Password Changed")
                    .setMessage("Your password has been updated successfully.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(requireContext(), "Failed to update password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(requireActivity().getContentResolver(), imageUri);
                ivProfilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}