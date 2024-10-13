package com.pequesystems.gestionganadera.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.Animal;
import com.pequesystems.gestionganadera.models.AnimalLocation;
import com.pequesystems.gestionganadera.models.LatLngPoint;
import com.pequesystems.gestionganadera.models.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    MapView map_mapView_map;
    ImageButton map_button_back;
    Chip map_chip_viewRegions;
    private boolean isShowRegions = false;
    private List<Region> regions = new ArrayList<>();
    private List<AnimalLocation> animalLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map_mapView_map = findViewById(R.id.map_mapView_map);
        map_button_back = findViewById(R.id.map_imageButton_back);
        map_chip_viewRegions = findViewById(R.id.map_chip_viewRegions);

        map_button_back.setOnClickListener(v -> {
            finish();
        });

        map_mapView_map.onCreate(savedInstanceState);
        map_mapView_map.getMapAsync(this);

        loadRegions();
        loadAnimalsLocations();
        showAnimals();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        map_chip_viewRegions.setOnClickListener(v -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.1f);
            animator.setDuration(150);
            animator.setRepeatCount(1);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();
            isShowRegions = !isShowRegions;
            if(isShowRegions){
                showRegions();
            }else{
                hideRegions();
            }
        });
    }

    /*
    private void loadAnimals(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String usuarioId = sharedPref.getString("user_id", "");

        db.collection("animals")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Animal animal = document.toObject(Animal.class);
                            animals.add(animal);
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar los animales", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    */

    private void loadAnimalsLocations(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String usuarioId = sharedPref.getString("user_id", "");

        db.collection("animals")
                .whereEqualTo("usuarioId", usuarioId) // Reemplaza con el userId correspondiente
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Animal> animals = new ArrayList<>();
                        List<AnimalLocation> animalLocationsAux = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Animal animal = document.toObject(Animal.class);
                            animals.add(animal);
                        }

                        // Paso 2: Obtener las locaciones para cada animal
                        if (animals.stream().anyMatch(obj -> obj.getDeviceId() != null && obj.getDeviceId().length() > 0)) {
                            List<String> deviceIds = animals.stream()
                                    .map(Animal::getDeviceId)
                                    .filter(deviceId -> deviceId != null && deviceId.length() > 0)
                                    .collect(Collectors.toList());
                            db.collection("animalsLocations")
                                    .whereIn("Id", deviceIds)
                                    .get()
                                    .addOnCompleteListener(locationTask -> {
                                        if (locationTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot locationDocument : locationTask.getResult()) {
                                                AnimalLocation animalLocation = locationDocument.toObject(AnimalLocation.class);
                                                Animal animalAux = animals.stream()
                                                        .filter(deviceId -> deviceId != null && deviceId.equals(animalLocation.getId())) // Filtra nombres que coincidan con "Alice"
                                                        .findFirst() // Encuentra el primer elemento que coincida
                                                        .orElse(null); // Devuelve null si no hay coincidencias
                                                animalLocation.setAnimal(animalAux);
                                                animalLocations.add(animalLocation);
                                            }
                                        } else {
                                            Toast.makeText(this, "Error al cargar las ubicaciones de los animales", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar los animales", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRegions(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String usuarioId = sharedPref.getString("user_id", "");

        db.collection("regions")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Region region = document.toObject(Region.class);
                            regions.add(region);
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar los polígonos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAnimals(){
        if (googleMap != null) {
            for (int i = 0; i < animalLocations.size(); i++) {
                MarkerOptions marcador = new MarkerOptions();
                LatLngPoint point = animalLocations.get(i).getPoint();
                marcador.position(new LatLng(point.getLatitude(), point.getLongitude()));
                googleMap.addMarker(marcador);
            }
        }
    }

    private void showRegions(){
        if (googleMap != null) {
            googleMap.clear();
            for (int i = 0; i < regions.size(); i++) {
                showPolygonOnMap(regions.get(i));
            }
            Toast.makeText(this, "Mostrando regiones", Toast.LENGTH_SHORT).show();
            map_chip_viewRegions.setText("Ocultar Regiones");
            map_chip_viewRegions.setChipBackgroundColor(ColorStateList.valueOf(Color.RED));
        }
    }

    private int getFilteredColor(int color){
        // Extraer los componentes del color almacenado
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        // Crear un nuevo color con la misma transparencia que el valor deseado
        int newFillColor = Color.argb(50, red, green, blue);
        return newFillColor;
    }

    private void showPolygonOnMap(Region region) {
        List<LatLng> latLngs = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLngPoint point : region.getPoints()) {
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            latLngs.add(latLng);
            builder.include(latLng);
        }
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(latLngs)
                .strokeColor(region.getColor())
                .fillColor(getFilteredColor(region.getColor())) //Color.argb(50, 255, 0, 0))
                .strokeWidth(5);
        googleMap.addPolygon(polygonOptions);

        if (!latLngs.isEmpty()) {
            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50)); // Padding de 50 px
        }
    }

    private void hideRegions(){
        if (googleMap != null) {
            Toast.makeText(this, "Ocultando regiones", Toast.LENGTH_SHORT).show();
            googleMap.clear();
            map_chip_viewRegions.setText("Ver Regiones");
            map_chip_viewRegions.setChipBackgroundColor(ColorStateList.valueOf(Color.GREEN));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // Habilitar gestos de zoom
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map_mapView_map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_mapView_map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map_mapView_map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map_mapView_map.onLowMemory();
    }
}