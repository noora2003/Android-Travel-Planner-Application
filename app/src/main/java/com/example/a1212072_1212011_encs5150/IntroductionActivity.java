package com.example.a1212072_1212011_encs5150;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
    private DatabaseHelper dbHelper;

    // Replace with your actual REST API URL
    private static final String API_URL =
            "https://mocki.io/v1/c1ab722d-ade3-4c76-8971-33932b84562d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introductionactivity);

        dbHelper   = new DatabaseHelper(this);
        btnConnect = findViewById(R.id.btnConnect);

        // Animations — only the views that actually exist in the XML
        Animation fadeIn    = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // bottomSection exists in the XML
        View bottomSection = findViewById(R.id.bottomSection);
        if (bottomSection != null) bottomSection.startAnimation(fadeIn);

        // btnConnect exists in the XML
        if (btnConnect != null) {
            btnConnect.startAnimation(slideDown);
            btnConnect.setOnClickListener(v -> connectToApi());
        }
    }

    private void connectToApi() {
        // Disable button while loading so user can't press it twice
        if (btnConnect != null) {
            btnConnect.setEnabled(false);
            btnConnect.setText("Connecting...");
        }

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    parseAndStoreTrips(result.toString());

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    });

                } else {
                    throw new Exception("Server error: " + responseCode);
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (btnConnect != null) {
                        btnConnect.setEnabled(true);
                        btnConnect.setText("Connect & Explore →");
                    }
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

                dbHelper.insertTrip(id, destination, country,
                        duration, price, rating, description, image);
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