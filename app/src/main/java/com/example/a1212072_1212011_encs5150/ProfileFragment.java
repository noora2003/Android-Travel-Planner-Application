package com.example.a1212072_1212011_encs5150;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setText("ProfileFragment - Coming Soon");
        tv.setTextSize(18);
        tv.setPadding(40, 40, 40, 40);
        return tv;
    }
}
