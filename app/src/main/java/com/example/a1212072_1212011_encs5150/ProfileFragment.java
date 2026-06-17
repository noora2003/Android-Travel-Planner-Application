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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail;
    private TextInputLayout tilFirstName, tilLastName, tilPhone,
            tilOldPass, tilNewPass, tilConfirmPass;
    private TextInputEditText etFirstName, etLastName, etPhone,
            etOldPass, etNewPass, etConfirmPass;
    private Button btnSaveProfile, btnChangePass, btnPickPhoto;

    private DatabaseHelper dbHelper;
    private String currentEmail;
    private String profilePicPath = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Animations
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
        view.startAnimation(fadeIn);

        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        currentEmail = prefs.getString("logged_in_email", "");

        // Init views
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilPhone = view.findViewById(R.id.tilPhone);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);

        tilOldPass = view.findViewById(R.id.tilOldPass);
        tilNewPass = view.findViewById(R.id.tilNewPass);
        tilConfirmPass = view.findViewById(R.id.tilConfirmPass);
        etOldPass = view.findViewById(R.id.etOldPass);
        etNewPass = view.findViewById(R.id.etNewPass);
        etConfirmPass = view.findViewById(R.id.etConfirmPass);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnChangePass = view.findViewById(R.id.btnChangePass);
        btnPickPhoto = view.findViewById(R.id.btnPickPhoto);

        loadUserData();

        // Animations for buttons
        btnSaveProfile.startAnimation(slideUp);
        btnChangePass.startAnimation(slideUp);

        btnPickPhoto.setOnClickListener(v -> openImagePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePass.setOnClickListener(v -> changePassword());

        return view;
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUserByEmail(currentEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            profilePicPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_PIC));

            tvProfileName.setText(firstName + " " + lastName);
            tvProfileEmail.setText(currentEmail);

            etFirstName.setText(firstName);
            etLastName.setText(lastName);
            etPhone.setText(phone);

            // Load profile picture if exists
            if (profilePicPath != null && !profilePicPath.isEmpty()) {
                Glide.with(requireContext())
                        .load(profilePicPath)
                        .circleCrop()
                        .placeholder(R.drawable.ic_plane)
                        .into(ivProfilePic);
            }
            cursor.close();
        }
    }

    private void saveProfile() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);

        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
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
            tvProfileName.setText(first + " " + last);
            Toast.makeText(requireContext(), "Profile updated successfully ✓", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Update failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        tilOldPass.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confPass = etConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(oldPass)) {
            tilOldPass.setError("Enter current password");
            return;
        }
        if (!PasswordUtils.isPasswordValid(newPass)) {
            tilNewPass.setError("Min 6 chars, at least 1 letter & 1 number");
            return;
        }
        if (!newPass.equals(confPass)) {
            tilConfirmPass.setError("Passwords don't match");
            return;
        }

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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ivProfilePic.setImageBitmap(bitmap);
                profilePicPath = imageUri.toString();
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}