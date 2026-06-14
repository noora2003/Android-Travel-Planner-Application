package com.example.a1212072_1212011_encs5150;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // How long the splash screen stays visible (3 seconds)
    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashactivity);

        ImageView ivLogo    = findViewById(R.id.ivLogo);
        TextView tvAppName  = findViewById(R.id.tvAppName);
        TextView tvTagline  = findViewById(R.id.tvTagline);

        // Load the fade-in + slide-up animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Start animation on all three elements
        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeIn);
        tvTagline.startAnimation(fadeIn);

        // After 3 seconds, go to Introduction screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, IntroductionActivity.class);
            startActivity(intent);
            // Fade out transition when leaving splash
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // so user can't press back to splash
        }, SPLASH_DURATION);
    }
}