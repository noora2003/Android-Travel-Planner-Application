package com.example.a1212072_1212011_encs5150;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class AdminHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Admin Panel");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                androidx.appcompat.R.string.abc_action_bar_up_description,
                androidx.appcompat.R.string.abc_action_bar_up_description);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment(), "Dashboard");
            navigationView.setCheckedItem(R.id.admin_nav_dashboard);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    new AlertDialog.Builder(AdminHomeActivity.this)
                            .setTitle("Exit Admin Panel")
                            .setMessage("Do you want to logout?")
                            .setPositiveButton("Yes", (d, w) -> performLogout())
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.admin_nav_dashboard) {
            loadFragment(new AdminDashboardFragment(), "Dashboard");
        } else if (id == R.id.admin_nav_users) {
            loadFragment(new AdminUsersFragment(), "Manage Users");
        } else if (id == R.id.admin_nav_trips) {
            loadFragment(new AdminTripsFragment(), "Manage Trips");
        } else if (id == R.id.admin_nav_reservations) {
            loadFragment(new AdminReservationsFragment(), "View Reservations");
        } else if (id == R.id.admin_nav_add_admin) {
            showAddAdminDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.admin_nav_logout) {
            performLogout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    private void showAddAdminDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_admin, null);

        TextInputEditText etEmail = dialogView.findViewById(R.id.etAdminEmail);
        TextInputEditText etPass = dialogView.findViewById(R.id.etAdminPass);
        TextInputEditText etFirst = dialogView.findViewById(R.id.etAdminFirst);
        TextInputEditText etLast = dialogView.findViewById(R.id.etAdminLast);

        new AlertDialog.Builder(this)
                .setTitle("Add New Admin")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass = etPass.getText().toString().trim();
                    String first = etFirst.getText().toString().trim();
                    String last = etLast.getText().toString().trim();

                    if (email.isEmpty() || pass.isEmpty() || first.isEmpty() || last.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dbHelper.emailExists(email)) {
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String hashed = PasswordUtils.hashPassword(pass);
                    boolean ok = dbHelper.insertUser(email, first, last, hashed, "—", "Admin", "—");
                    Toast.makeText(this,
                            ok ? "Admin added successfully ✓" : "Failed to add admin.",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences.Editor editor = getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE).edit();
        editor.remove("logged_in_email");
        editor.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }
}