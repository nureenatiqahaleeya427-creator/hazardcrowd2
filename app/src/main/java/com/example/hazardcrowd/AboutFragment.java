package com.example.hazardcrowd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sambungkan dengan XML anda tadi
        View view = inflater.inflate(R.layout.activity_about, container, false);

        // Setup butang GitHub (sama macam activity, tapi guna 'view.')
        View btnGithub = view.findViewById(R.id.tvGithub);
        if (btnGithub != null) {
            btnGithub.setOnClickListener(v -> {
                String url = "https://github.com/hazardcrowd/hazardcrowd-app";
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Browser not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Sembunyikan butang back di header jika guna Fragment (sebab menu bawah dah ada)
        // Atau biarkan saja.

        return view;
    }
}