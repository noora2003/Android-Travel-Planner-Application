package com.example.a1212072_1212011_encs5150;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IntroductionActivity extends AppCompatActivity {

    private Button btnConnect;
    private ProgressBar progressBar;
    private TextView tvLoading;
    private DatabaseHelper dbHelper;

    // ⚠ Replace this URL with your actual REST API URL
    private static final String API_URL = "https://mocki.io/v1/283816d0-2bd8-4e87-9d9c-a4771e50b820";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introductionactivity);

        dbHelper    = new DatabaseHelper(this);
        btnConnect  = findViewById(R.id.btnConnect);
        progressBar = findViewById(R.id.progressBar);
        tvLoading   = findViewById(R.id.tvLoading);

        // Animations
        Animation fadeIn  = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Hero image fades in
        findViewById(R.id.heroImage).startAnimation(slideDown);

        // Bottom section slides up ↑
        findViewById(R.id.bottomSection).startAnimation(fadeIn);

        btnConnect.startAnimation(fadeIn);

        btnConnect.setOnClickListener(v -> connectToApi());
    }

    private void connectToApi() {
        // Show loading state
        btnConnect.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);

        // Run network call on a background thread (never on main thread)
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Parse JSON and store in database
                    parseAndStoreTrips(result.toString());

                    // Go to Login screen on success
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    });

                } else {
                    throw new Exception("Server error: " + responseCode);
                }

            } catch (Exception e) {
                // Connection failed — show error and stay on screen
                runOnUiThread(() -> {
                    btnConnect.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    tvLoading.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Connection failed. Please check your internet and try again.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void parseAndStoreTrips(String jsonString) {
        try {
            JSONArray tripsArray = new JSONArray(jsonString);
            for (int i = 0; i < tripsArray.length(); i++) {
                JSONObject trip = tripsArray.getJSONObject(i);

                int    id          = trip.getInt("id");
                String destination = trip.getString("destination");
                String country     = trip.getString("country");
                int    duration    = trip.getInt("duration_days");
                double price       = trip.getDouble("price");
                double rating      = trip.getDouble("rating");
                String description = trip.getString("description");
                String image       = trip.getString("image");

                // Save each trip to the database
                dbHelper.insertTrip(id, destination, country, duration, price, rating, description, image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(IntroductionActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}