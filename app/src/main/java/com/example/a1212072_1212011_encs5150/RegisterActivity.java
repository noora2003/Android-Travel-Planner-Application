package com.example.a1212072_1212011_encs5150;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail,
            etPhone, etPassword, etConfirmPassword;
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout,
            phoneLayout, passwordLayout, confirmPasswordLayout;
    private Spinner spinnerGender, spinnerCategory;
    private Button btnRegister;
    private TextView tvLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        // Init views
        etFirstName           = findViewById(R.id.etFirstName);
        etLastName            = findViewById(R.id.etLastName);
        etEmail               = findViewById(R.id.etEmail);
        etPhone               = findViewById(R.id.etPhone);
        etPassword            = findViewById(R.id.etPassword);
        etConfirmPassword     = findViewById(R.id.etConfirmPassword);
        firstNameLayout       = findViewById(R.id.firstNameLayout);
        lastNameLayout        = findViewById(R.id.lastNameLayout);
        emailLayout           = findViewById(R.id.emailLayout);
        phoneLayout           = findViewById(R.id.phoneLayout);
        passwordLayout        = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        spinnerGender         = findViewById(R.id.spinnerGender);
        spinnerCategory       = findViewById(R.id.spinnerCategory);
        btnRegister           = findViewById(R.id.btnRegister);
        tvLogin               = findViewById(R.id.tvLogin);

        setupSpinners();

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupSpinners() {
        // Gender options
        String[] genders = {"Select Gender", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Travel category options
        String[] categories = {"Select Category", "Adventure", "Beach", "Cultural",
                 "Luxury", "Budget"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void attemptRegister() {
        // Clear all previous errors
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Get values
        String firstName   = etFirstName.getText().toString().trim();
        String lastName    = etLastName.getText().toString().trim();
        String email       = etEmail.getText().toString().trim();
        String phone       = etPhone.getText().toString().trim();
        String password    = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String gender      = spinnerGender.getSelectedItem().toString();
        String category    = spinnerCategory.getSelectedItem().toString();

        //  Validate all fields

        boolean hasError = false;

        if (TextUtils.isEmpty(firstName) || firstName.length() < 3) {
            firstNameLayout.setError("First name must be at least 3 characters");
            hasError = true;
        }
        if (TextUtils.isEmpty(lastName) || lastName.length() < 3) {
            lastNameLayout.setError("Last name must be at least 3 characters");
            hasError = true;
        }
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            hasError = true;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneLayout.setError("Phone number is required");
            hasError = true;
        }
        if (gender.equals("Select Gender")) {
            spinnerGender.requestFocus();
            hasError = true;
        }
        if (category.equals("Select Category")) {
            spinnerCategory.requestFocus();
            hasError = true;
        }
        if (!PasswordUtils.isPasswordValid(password)) {
            passwordLayout.setError("Password must be 6+ chars with at least 1 letter and 1 number");
            hasError = true;
        }
        if (!password.equals(confirmPass)) {
            confirmPasswordLayout.setError("Passwords do not match");
            hasError = true;
        }

        if (hasError) return;

        //  Check email not already registered
        if (dbHelper.emailExists(email)) {
            emailLayout.setError("This email is already registered");
            return;
        }

        //  Hash password and insert user
        String hashedPassword = PasswordUtils.hashPassword(password);
        boolean success = dbHelper.insertUser(
                email, firstName, lastName,
                hashedPassword, gender, category, phone);

        if (success) {
            android.widget.Toast.makeText(this,
                    "Account created successfully! Please log in.",
                    android.widget.Toast.LENGTH_LONG).show();

            // Redirect to Login screen
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            android.widget.Toast.makeText(this,
                    "Registration failed. Please try again.",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}