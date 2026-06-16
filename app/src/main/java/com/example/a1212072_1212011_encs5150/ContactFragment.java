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
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        Button btnCall     = view.findViewById(R.id.btnCall);
        Button btnLocation = view.findViewById(R.id.btnLocation);
        Button btnEmail    = view.findViewById(R.id.btnEmail);

        // Call Us — opens the phone dialer with a pre-filled number
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+1234567890"));
            startActivity(intent);
        });

        // Locate Us — opens Google Maps at a predefined location
        btnLocation.setOnClickListener(v -> {
            Uri location = Uri.parse("geo:31.9154,35.2067?q=Travel+Planner+Office");
            Intent intent = new Intent(Intent.ACTION_VIEW, location);
            intent.setPackage("com.google.android.apps.maps");
            // Fallback: if Maps is not installed, open in browser
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=31.9154,35.2067"));
                startActivity(browserIntent);
            }
        });

        // Email Us — opens Gmail (or any email app) with a pre-filled address
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@travelplanner.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Travel Planner Support");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        return view;
    }
}