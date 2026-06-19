package com.example.a1212072_1212011_encs5150;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private String loggedInEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("TravelPlannerPrefs", MODE_PRIVATE);
        loggedInEmail = prefs.getString("logged_in_email", "");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout   = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupNavHeader();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Home");
            navigationView.setCheckedItem(R.id.nav_home);
        }

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void setupNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName  = headerView.findViewById(R.id.navUserName);
        TextView navUserEmail = headerView.findViewById(R.id.navUserEmail);

        Cursor cursor = dbHelper.getUserByEmail(loggedInEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName  = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            navUserName.setText(firstName + " " + lastName);
            cursor.close();
        }
        navUserEmail.setText(loggedInEmail);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        String title = "Home";

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        } else if (id == R.id.nav_trips) {
            fragment = new TripsFragment();
            title = "Trips";
        } else if (id == R.id.nav_reservations) {
            fragment = new ReservationsFragment();
            title = "My Reservations";
        } else if (id == R.id.nav_favorites) {
            fragment = new FavoritesFragment();
            title = "Favorites";
        } else if (id == R.id.nav_special) {
            fragment = new SpecialFragment();
            title = "Special Section";
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = "Profile";
        } else if (id == R.id.nav_contact) {
            fragment = new ContactFragment();
            title = "Contact Us";
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        loadFragment(fragment, title);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences(
                "TravelPlannerPrefs", MODE_PRIVATE).edit();
        editor.remove("logged_in_email");
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}