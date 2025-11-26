package com.example.lotterize.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.ActivityShowListBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;



/**
 * Activity that shows the waiting list for an event
 * (eventID given in intent). Also has a button which allows current
 * user to join or leave the waiting list.
 */
public class ShowWaitingListActivity extends AppCompatActivity {

    ActivityShowListBinding binding;

    FirebaseFirestore db;

    CollectionReference events;
    CollectionReference users;

    ArrayList<String> usersId;
    ShowListArrayAdapter adapter;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private interface LocationCallback {
        void onLocationObtained(Location location);
        void onLocationFailed();
    }


    /**
     * Displays the waiting list, along with an option to
     * leave or join the list.
     * @param savedInstanceState intent contains a String in the
     *                           "eventId" field
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityShowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        users = db.collection(("users"));
        usersId = new ArrayList<>();
        adapter = new ShowListArrayAdapter(this, usersId, "users", "userId", "name");

        ListView list = binding.listViewShowList;
        TextView header = binding.eventListNameText;
        Button interact = binding.interactButton;
        ImageButton back = binding.back;

        String leaveList = "Leave Waiting List";
        String joinList = "Join Waiting List";
        String eventId = getIntent().getStringExtra("eventId");
        String userId = CurrentUser.get().getUserId();

        header.setText(R.string.waiting_list);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        interact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interact.getText().equals(leaveList)) { // leave waiting list
                    usersId.remove(userId);
                    adapter.notifyDataSetChanged();
                    assert eventId != null;
                    events.document(eventId).get().addOnSuccessListener(doc-> {
                        if (!doc.exists()) {
                            Toast.makeText(ShowWaitingListActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentReference event = events.document(eventId);
                        event.update("waitList", FieldValue.arrayRemove(userId))
                                .addOnSuccessListener(aVoid -> {
                                // Also remove location
                                String locationPath = "userLocations." + userId;
                                event.update(locationPath, FieldValue.delete());
                            });
                        interact.setText(joinList);
                    });
                } else { // join waiting list

                    assert eventId != null;

                    events.document(eventId).get().addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(ShowWaitingListActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentReference event = events.document(eventId);
                        Long entrantsLimit = doc.getLong("entrantsLimit");

                        Boolean geolocationEnabled = doc.getBoolean("geolocationEnabled");
                        if (geolocationEnabled == null) geolocationEnabled = false;
                        Log.d("ShowWaitingList", "Joining waiting list. Geolocation enabled: " + geolocationEnabled);

                        if (entrantsLimit != null && entrantsLimit <= usersId.size() && entrantsLimit != 0){
                            Toast.makeText(ShowWaitingListActivity.this, "Waiting List is Full!!!", Toast.LENGTH_SHORT).show();
                        } else{
                            if (geolocationEnabled) {
                                Log.d("ShowWaitingList", "Geolocation enabled");
                                if (checkPermissions()) {
                                    Log.d("ShowWaitingList", "Location permissions granted, requesting location");
                                    getUserLocation(new LocationCallback() {
                                        @Override
                                        public void onLocationObtained(Location location) {
                                            GeoPoint geo = new GeoPoint(
                                                    location.getLatitude(),
                                                    location.getLongitude()
                                            );
                                            String updatePath = "userLocations." + userId;
                                            // Add location to Firestore
                                            events.document(eventId)
                                                    .update(updatePath, geo)
                                                    .addOnSuccessListener(aVoid -> Log.d("UserLocation", "Updated location for " + userId))
                                                    .addOnFailureListener(e -> Log.e("UserLocation", "Failed to update location for " + userId, e));

                                            Log.d("UserLocation", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                                        }
                                        @Override
                                        public void onLocationFailed() {
                                            Log.d("UserLocation", "Failed to obtain location");
                                        }
                                    });
                                } else {
                                    requestPermissions(); // join will happen in onRequestPermissionsResult
                                    Toast.makeText(ShowWaitingListActivity.this, "This event requires geolocation collection", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(ShowWaitingListActivity.this, "Please try again after enabling geolocation", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            // add user to waiting list
                            event.update("waitList", FieldValue.arrayUnion(userId));
                            interact.setText(leaveList);
                            usersId.add(userId);
                            adapter.notifyDataSetChanged();
                        }

                    });
                }

            }
        });

        list.setAdapter(adapter);

        assert eventId != null;
        events.document(eventId).get().addOnSuccessListener(doc-> {
            if (!doc.exists()) {
                Toast.makeText(ShowWaitingListActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> obtainedUserIds = (List<String>) doc.get("waitList");
            if (obtainedUserIds != null && !obtainedUserIds.isEmpty()){
                if(obtainedUserIds.contains(userId)){
                    interact.setText(leaveList);
                } else {
                    interact.setText(joinList);
                }
                usersId.addAll(obtainedUserIds);
                adapter.notifyDataSetChanged();
            } else {
                interact.setText(joinList);
            }
        });

    }
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation(LocationCallback callback) {
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        callback.onLocationObtained(location);
//                    } else {
//                        Log.d("UserLocation", "Location is null");
//                        callback.onLocationFailed();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("UserLocation", "Failed to get location", e);
//                    callback.onLocationFailed();
//                });
        LocationRequest locationRequest =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
                        .setMinUpdateIntervalMillis(0)
                        .setMaxUpdateDelayMillis(0)
                        .setWaitForAccurateLocation(true)
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        fusedLocationClient.removeLocationUpdates(this);

                        if (locationResult == null) {
                            callback.onLocationFailed();
                            return;
                        }

                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            callback.onLocationObtained(location);
                        } else {
                            callback.onLocationFailed();
                        }
                    }
                },
                getMainLooper()
        );
    }






}
