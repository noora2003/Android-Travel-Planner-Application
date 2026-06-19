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

    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashactivity);

        ImageView ivLogo    = findViewById(R.id.ivLogo);
        TextView tvAppName  = findViewById(R.id.tvAppName);
        TextView tvTagline  = findViewById(R.id.tvTagline);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeIn);
        tvTagline.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, IntroductionActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}