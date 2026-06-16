package com.example.a1212072_1212011_encs5150;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class ContactFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        Button btnCall = view.findViewById(R.id.btnCall);
        Button btnLocation = view.findViewById(R.id.btnLocation);
        Button btnEmail = view.findViewById(R.id.btnEmail);

        btnCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Call Us: +1234567890", Toast.LENGTH_SHORT).show();
            // سنضيف الإجراء الحقيقي لاحقاً
        });

        btnLocation.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening maps...", Toast.LENGTH_SHORT).show();
            // سنضيف الإجراء الحقيقي لاحقاً
        });

        btnEmail.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Email: support@travelplanner.com", Toast.LENGTH_SHORT).show();
            // سنضيف الإجراء الحقيقي لاحقاً
        });

        return view;
    }
}