package com.pequesystems.gestionganadera.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.pequesystems.gestionganadera.R;

public class HomeActivity extends AppCompatActivity {

    Button home_button_logout;
    ImageButton home_imageButton_animals, home_imageButton_map, home_imageButton_mapRegions, home_imageButton_config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Inicio");

        home_button_logout = findViewById(R.id.home_button_logout);
        home_imageButton_animals = findViewById(R.id.home_imageButton_animals);
        home_imageButton_map = findViewById(R.id.home_imageButton_map);
        home_imageButton_mapRegions = findViewById(R.id.home_imageButton_mapRegions);
        home_imageButton_config = findViewById(R.id.home_imageButton_config);

        home_button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                showAuth();
            }
        });

        home_imageButton_animals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAnimals();
            }
        });

        home_imageButton_map.setOnClickListener(v -> {
            showMap();
        });

        home_imageButton_mapRegions.setOnClickListener(v -> {
            showMapRegions();
        });

        home_imageButton_config.setOnClickListener(v -> {
            showConfig();
        });
    }

    private void showAuth(){
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        startActivity(intent);
        finish(); // Opcional: cerrar la actividad actual
    }

    private void showAnimals(){
        Intent intent = new Intent(HomeActivity.this, AnimalsActivity.class);
        startActivity(intent);
    }

    private void showMap(){
        Intent intent = new Intent(HomeActivity.this, MapActivity.class);
        startActivity(intent);
    }

    private void showMapRegions(){
        Intent intent = new Intent(HomeActivity.this, MapRegionsActivity.class);
        startActivity(intent);
    }

    private void showConfig(){
        Intent intent = new Intent(HomeActivity.this, ConfigActivity.class);
        startActivity(intent);
    }
}