package com.example.a1212072_1212011_encs5150;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class ContactFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCall   = view.findViewById(R.id.btnCall);
        Button btnLocate = view.findViewById(R.id.btnLocate);
        Button btnEmail  = view.findViewById(R.id.btnEmail);

        // Open phone dialer with predefined number
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+970599000000"));
            startActivity(intent);
        });

        // Open Google Maps with predefined location (Birzeit University)
        btnLocate.setOnClickListener(v -> {
            Uri geoUri = Uri.parse("geo:31.9784,35.2385?q=Birzeit+University");
            Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Open Gmail / email app with predefined address
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@travelplanner.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Travel Planner Support");
            startActivity(intent);
        });
    }
}