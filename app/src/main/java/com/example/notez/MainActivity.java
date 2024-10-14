package com.example.notez;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView logoutImageView;
    private ImageView circleImageView;
    private ImageView plusImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this matches your layout file

        // Find the views
        logoutImageView = findViewById(R.id.logout);
        circleImageView = findViewById(R.id.circle);
        plusImageView = findViewById(R.id.plus);

        // Set up the click listener for logout
        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Set up the click listener for the circle icon
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteDisplay();
            }
        });

        // Set up the click listener for the plus icon
        plusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteDisplay();
            }
        });
    }

    private void openNoteDisplay() {
        // Navigate to the note_display activity
        Intent intent = new Intent(MainActivity.this, note_display.class);
        startActivity(intent);
    }

    private void logout() {
        // Clear shared preferences (use "user_prefs" here to match SignIn.java)
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // This will clear the login state
        editor.apply();

        // Navigate back to the login screen
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}
