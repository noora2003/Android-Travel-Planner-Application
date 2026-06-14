package com.example.a1212072_1212011_encs5150;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout emailLayout, passwordLayout;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnSignUp;
    private TextView tvError;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME     = "TravelPlannerPrefs";
    private static final String KEY_EMAIL     = "saved_email";
    private static final String KEY_REMEMBER  = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper        = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etEmail       = findViewById(R.id.etEmail);
        etPassword    = findViewById(R.id.etPassword);
        emailLayout   = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        cbRememberMe  = findViewById(R.id.cbRememberMe);
        btnLogin      = findViewById(R.id.btnLogin);
        btnSignUp     = findViewById(R.id.btnSignUp);
        tvError       = findViewById(R.id.tvError);

        loadSavedEmail();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loadSavedEmail() {
        boolean remembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remembered) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            etEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
    }

    private void attemptLogin() {
        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);
        tvError.setVisibility(View.GONE);

        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }

        // Hash the password the same way it was stored during registration
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Check credentials in DB
        boolean isAdmin = email.equals("admin@admin.com") && password.equals("Admin123!");
        boolean isValid = isAdmin || dbHelper.checkUser(email, hashedPassword);

        if (isValid) {
            handleRememberMe(email);
            navigateAfterLogin(isAdmin, email);
        } else {
            tvError.setText("Invalid email or password. Please try again.");
            tvError.setVisibility(View.VISIBLE);
            passwordLayout.setError(" ");
        }
    }

    // Remember Me saves email using SharedPreferences + password encrypted
    private void handleRememberMe(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cbRememberMe.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_EMAIL, email);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_EMAIL);
        }
        editor.apply();
    }

    private void navigateAfterLogin(boolean isAdmin, String email) {
        // Save the currently logged-in user email for use across the app
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("logged_in_email", email);
        editor.apply();

        Intent intent;
        if (isAdmin) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}