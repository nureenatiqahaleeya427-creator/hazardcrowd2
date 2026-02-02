package com.example.hazardcrowd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ReportFragment extends Fragment {

    // UI Components
    private Spinner spinnerType;
    private EditText etDescription;
    private Button btnSubmit;
    private TextView btnCancel; // Tukar ke TextView sebab dalam XML ia adalah TextView
    private TextView tvLocation;

    // Default Coordinates (Example: UUM/Kedah)
    private double lat = 6.5170;
    private double lng = 100.2151;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Load layout XML yang kita cantikkan tadi
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // 1. Initialize Views (Pastikan ID sama dengan XML)
        spinnerType = view.findViewById(R.id.spinnerType);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmit = view.findViewById(R.id.btn_submit_report);
        btnCancel = view.findViewById(R.id.btnCancel); // Ini TextLink
        tvLocation = view.findViewById(R.id.tvLocation);

        // 2. Setup Dropdown (Spinner)
        setupSpinner();

        // 3. Check for Arguments (Location passed from MapsFragment)
        if (getArguments() != null) {
            lat = getArguments().getDouble("lat", 6.5170);
            lng = getArguments().getDouble("lng", 100.2151);
        }

        // Paparkan koordinat
        tvLocation.setText(String.format("Lat: %.6f, Lng: %.6f", lat, lng));

        // 4. Setup Listeners
        btnSubmit.setOnClickListener(v -> sendToServer());

        btnCancel.setOnClickListener(v -> {
            // Kosongkan form atau kembali ke home
            etDescription.setText("");
            if (getActivity() != null) {
                // Contoh: Kembali ke HomeFragment jika cancel ditekan
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
            Toast.makeText(requireContext(), "Report Cancelled", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupSpinner() {
        String[] hazards = {
                "🌊 Flood",
                "🚗 Accident",
                "🔥 Fire",
                "🚧 Road Closure",
                "⛰️ Landslide",
                "⚠️ Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                hazards
        );
        spinnerType.setAdapter(adapter);
    }

    private void sendToServer() {
        // Guna IP Emulator Android (10.0.2.2) jika localhost XAMPP
        String url = "http://10.0.2.2/hazardcrowd/index.php";

        // Validate Input
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Please describe the hazard details");
            etDescription.requestFocus();
            return;
        }

        // Disable button to prevent double-click
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Sending...");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Success
                    Toast.makeText(requireContext(), "✅ Report submitted successfully!", Toast.LENGTH_LONG).show();

                    // Reset UI
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("SUBMIT REPORT");
                    etDescription.setText("");
                    spinnerType.setSelection(0);
                },
                error -> {
                    // Error
                    Toast.makeText(requireContext(), "❌ Connection Failed. Check Server.", Toast.LENGTH_LONG).show();
                    error.printStackTrace();

                    // Reset Button
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("SUBMIT REPORT");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Clean up the emoji from hazard type (e.g. "🌊 Flood" -> "Flood")
                String selectedType = spinnerType.getSelectedItem().toString();
                String cleanType = selectedType.replaceAll("[^a-zA-Z ]", "").trim();

                params.put("type", cleanType);
                params.put("description", etDescription.getText().toString().trim());
                params.put("lat", String.valueOf(lat));
                params.put("lng", String.valueOf(lng));

                return params;
            }
        };

        // Add to Volley Queue
        Volley.newRequestQueue(requireContext()).add(request);
    }
}