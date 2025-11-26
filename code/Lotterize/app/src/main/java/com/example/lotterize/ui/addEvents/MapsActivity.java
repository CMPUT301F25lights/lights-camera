package com.example.lotterize.ui.addEvents;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.lotterize.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lotterize.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying a Google map of locations where users joined waitlist from.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("MapsAPI", "API key: ${BuildConfig.MAPS_API_KEY}");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Back button
        findViewById(R.id.back).setOnClickListener(v -> finish());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            List<String> waitList = (List<String>) doc.get("waitList");
            Map<String, GeoPoint> userLocations = (Map<String, GeoPoint>) doc.get("userLocations");

            if (waitList == null || waitList.isEmpty() || userLocations == null || userLocations.isEmpty()) {
                // No users to display; map will stay blank
                return;
            }

            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            List<LatLng> points = new ArrayList<>();

            for (String userId : waitList) {
                GeoPoint geo = userLocations.get(userId);
                if (geo == null) continue; // skip users without a location

                LatLng latLng = new LatLng(geo.getLatitude(), geo.getLongitude());
                points.add(latLng);

                // Fetch the user's name
                db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                    String name = "Unknown";
                    if (userDoc.exists()) {
                        String fetchedName = userDoc.getString("name");
                        if (fetchedName != null) name = fetchedName;
                    }

                    // Add marker for this user
                    mMap.addMarker(new MarkerOptions().position(latLng).title(name));

                    // Only animate camera after adding the last marker
                    if (points.size() == waitList.size()) {
                        if (!points.isEmpty()) {
                            for (LatLng point : points) {
                                boundsBuilder.include(point);
                            }
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                        }
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("MapsActivity", "Failed to get event", e));

    }
}