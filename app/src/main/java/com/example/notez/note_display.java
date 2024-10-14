package com.example.notez;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class note_display extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private ImageView cameraImageView;
    private ImageView backImageView;
    private EditText noteEditText;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private ImageView locationImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_display); // Use your actual layout file

        // Initialize camera icon, back arrow icon, and note field
        cameraImageView = findViewById(R.id.imageView5); // Your camera icon ID
        backImageView = findViewById(R.id.imageView); // Your back arrow icon ID
        noteEditText = findViewById(R.id.textView5); // Your note field ID

        // Set up the camera click listener
        cameraImageView.setOnClickListener(v -> checkCameraPermission());

        // Set up the back arrow click listener
        backImageView.setOnClickListener(v -> goBackToMainPage());

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Find location icon and set listener
        locationImageView = findViewById(R.id.imageView7);  // Your location icon ID
        noteEditText = findViewById(R.id.textView5); // The EditText to display location

        // Set click listener for the location icon
        locationImageView.setOnClickListener(v -> getLocation());
    }

    // Method to go back to the MainActivity
    private void goBackToMainPage() {
        finish(); // This will close the note_display activity and go back to MainActivity
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void openCamera() {
        if (!hasCamera()) {
            Toast.makeText(this, "No camera found on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("CameraCheck", "Starting camera intent");
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Log.d("CameraCheck", "No app available to handle camera intent");
            Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, open the camera
            openCamera();
        } else {
            // Permission is not granted, request for permission
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            Log.d("CameraCheck", "Camera permission not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            // Handle camera permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied, show a toast message
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Handle location permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            if (photo != null) {
                // Set the captured image in the ImageView
                cameraImageView.setImageBitmap(photo);

                // Append image information without deleting the current note
                String existingText = noteEditText.getText().toString(); // Get current text
                noteEditText.setText("Image captured.\n" + existingText); // Add image info above existing text
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, fetch location
            fetchLocation();
        }
    }

    // Fetch location details and update the note with address information
    private void fetchLocation() {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            updateLocationInNoteField(latitude, longitude);
                        } else {
                            Toast.makeText(note_display.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateLocationInNoteField(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(note_display.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                String country = address.getCountryName();
                String city = address.getLocality();
                String state = address.getAdminArea();
                String road = address.getThoroughfare();

                String existingText = noteEditText.getText().toString();
                noteEditText.setText("Address: " + addressLine + "\n" +
                        "Country: " + country + "\n" +
                        "City: " + city + "\n" +
                        "State: " + state + "\n" +
                        "Road: " + road + "\n" + "\n" + existingText);
            } else {
                noteEditText.append("\nLocation: " + latitude + ", " + longitude + " (Address not found)");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(note_display.this, "Error fetching address", Toast.LENGTH_SHORT).show();
        }
    }
}
