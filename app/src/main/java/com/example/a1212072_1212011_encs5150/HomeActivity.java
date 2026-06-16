package com.example.a1212072_1212011_encs5150;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private String currentUserEmail;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("logged_in_email", "");

        if (currentUserEmail.isEmpty()) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupToolbarAndDrawer();
        updateNavHeader();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameContainer, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // إضافة OnBackPressedDispatcher (الطريقة الجديدة)
        setupBackPressedDispatcher();
    }

    private void setupToolbarAndDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Travel Planner");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                androidx.appcompat.R.string.abc_action_bar_up_description,
                androidx.appcompat.R.string.abc_action_bar_up_description);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupBackPressedDispatcher() {
        // الطريقة الجديدة والصحيحة للتعامل مع زر الرجوع
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
                    if (currentFragment instanceof HomeFragment) {
                        // إذا كنا في HomeFragment، نطلب تأكيد الخروج
                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Exit")
                                .setMessage("Do you want to exit the app?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    finish();
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    // لا شيء، فقط أغلق الحوار
                                })
                                .show();
                    } else {
                        // إذا كنا في Fragment آخر، نرجع إلى HomeFragment
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frameContainer, new HomeFragment())
                                .commit();
                        navigationView.setCheckedItem(R.id.nav_home);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Travel Planner");
                        }
                    }
                }
            }
        };

        // إضافة الـ callback إلى dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserNameNav);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmailNav);

        Cursor cursor = dbHelper.getUserByEmail(currentUserEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            tvUserName.setText(firstName + " " + lastName);
            tvUserEmail.setText(currentUserEmail);
            cursor.close();
        } else {
            tvUserName.setText("Traveler");
            tvUserEmail.setText(currentUserEmail);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Travel Planner");
            }
        }
        else if (id == R.id.nav_trips) {
            selectedFragment = new TripsFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Explore Trips");
            }
        }
        else if (id == R.id.nav_reservations) {
            selectedFragment = new ReservationsFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Reservations");
            }
        }
        else if (id == R.id.nav_favorites) {
            selectedFragment = new FavoritesFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Favorites");
            }
        }
        else if (id == R.id.nav_special) {
            selectedFragment = new SpecialSectionFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Special Offers");
            }
        }
        else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Profile");
            }
        }
        else if (id == R.id.nav_contact) {
            selectedFragment = new ContactFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Contact Us");
            }
        }
        else if (id == R.id.nav_logout) {
            showLogoutDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameContainer, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("logged_in_email");
        editor.remove("remember_me");
        editor.remove("saved_email");
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}