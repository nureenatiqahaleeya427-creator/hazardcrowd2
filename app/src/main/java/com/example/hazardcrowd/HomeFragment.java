package com.example.hazardcrowd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_home, container, false);




        LinearLayout btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                // Open ProfileActivity
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            });
        }


        Button btnEmergency = view.findViewById(R.id.btnEmergency);
        if (btnEmergency != null) {
            btnEmergency.setOnClickListener(v -> {
                // Call 999
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:999"));
                startActivity(intent);
            });
        }

        return view;
    }
}