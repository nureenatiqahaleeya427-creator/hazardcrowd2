package com.example.hazardcrowd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
// Note: Ensure your XML file is named fragment_maps.xml so this binding class is generated
// If your XML is still activity_maps.xml, change this import to ActivityMapsBinding
import com.example.hazardcrowd.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentMapsBinding binding; // Make sure your layout file is named fragment_maps.xml
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // 📍 Default Location: Perlis
    double userLat = 6.5170;
    double userLng = 100.2151;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // In Fragments, use getChildFragmentManager()
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Focus camera on default location (Perlis)
        LatLng perlisLocation = new LatLng(userLat, userLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(perlisLocation, 15f));

        // Enable Blue Dot
        enableMyLocation();

        // Load Hazards
        loadMarkersFromServer();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            // Request permissions from Fragment
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLng = location.getLongitude();
                            loadMarkersFromServer();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMarkersFromServer() {
        String url = "http://10.0.2.2/hazardcrowd/index.php?format=json";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (mMap == null) return;
                        mMap.clear();

                        // Add "You are here" marker
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(userLat, userLng))
                                .title("Your Location"));

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String type = obj.getString("type");
                            double lat = obj.getDouble("lat");
                            double lng = obj.getDouble("lng");

                            if (lat == 0 || lng == 0) continue;

                            LatLng hazardLocation = new LatLng(lat, lng);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(hazardLocation)
                                    .title(type);

                            // Custom Icons
                            int iconResId = 0;
                            switch (type) {
                                case "Flood": iconResId = R.drawable.logo_flood; break;
                                case "Accident": iconResId = R.drawable.logo_accident; break;
                                case "Fire": iconResId = R.drawable.logo_fire; break;
                                case "Road Closure": iconResId = R.drawable.logo_road_closure; break;
                                case "Landslide": iconResId = R.drawable.logo_landslide; break;
                            }

                            if (iconResId != 0) {
                                markerOptions.icon(getResizedBitmapDescriptor(iconResId, 100, 100));
                            }

                            mMap.addMarker(markerOptions);

                            // Distance Warning
                            float[] results = new float[1];
                            Location.distanceBetween(userLat, userLng, lat, lng, results);
                            if (results[0] < 500) {
                                Toast.makeText(requireContext(), "⚠️ Warning: " + type + " nearby!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error loading map data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private BitmapDescriptor getResizedBitmapDescriptor(int resId, int width, int height) {
        if (!isAdded()) return BitmapDescriptorFactory.defaultMarker(); // Safety check
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
        if (imageBitmap == null) return BitmapDescriptorFactory.defaultMarker();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }
}