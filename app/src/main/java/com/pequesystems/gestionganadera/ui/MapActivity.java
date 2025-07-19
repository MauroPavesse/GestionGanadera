package com.pequesystems.gestionganadera.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.Animal;
import com.pequesystems.gestionganadera.models.AnimalLocation;
import com.pequesystems.gestionganadera.models.LatLngPoint;
import com.pequesystems.gestionganadera.models.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    MapView map_mapView_map;
    ImageButton map_button_back;
    Chip map_chip_viewRegions;
    private boolean isShowRegions = false;
    private List<Animal> animals = new ArrayList<>();
    private List<Region> regions = new ArrayList<>();
    private List<AnimalLocation> animalLocations = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private final Handler handler = new Handler();
    private final int REFRESH_INTERVAL_MS = 5_000; // Cada 30 segundos (ajustá como necesites)
    private Map<String, Marker> animalMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = sharedPref.getString("user_id", "");

        map_mapView_map = findViewById(R.id.map_mapView_map);
        map_button_back = findViewById(R.id.map_imageButton_back);
        map_chip_viewRegions = findViewById(R.id.map_chip_viewRegions);

        map_button_back.setOnClickListener(v -> {
            finish();
        });

        map_mapView_map.onCreate(savedInstanceState);
        map_mapView_map.getMapAsync(this);

        loadRegions(userId);

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

    private void loadAnimals(){
        try{
            db.collection("animals")
                    .whereEqualTo("userId", "NJtqKEDxvHSdJ4K5jdwj93Le3cF3")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Animal animal = document.toObject(Animal.class);
                                    animals.add(animal);
                                }
                                loadAnimalsLocations();
                            }
                        } else {
                            Toast.makeText(this, "Error al cargar los animales", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAnimalsLocations(){
        try{
            animalLocations.clear();
            List<String> deviceIds = new ArrayList<>();
            for (Animal animal : animals) {
                if (animal.getDeviceId() != null && !animal.getDeviceId().isEmpty()) {
                    deviceIds.add(animal.getDeviceId());
                }
            }

            if (deviceIds.isEmpty()) {
                Toast.makeText(this, "No hay dispositivos para buscar ubicaciones", Toast.LENGTH_SHORT).show();
                return;
            }

            List<List<String>> chunks = splitList(deviceIds, 10);

            for (List<String> chunk : chunks) {
                db.collection("animalsLocations")
                        .whereIn("id", chunk)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getString("id");

                                    Map<String, Object> point = (Map<String, Object>) document.get("point");
                                    if (point != null) {
                                        Double latitude = (Double) point.get("latitude");
                                        Double longitude = (Double) point.get("longitude");
                                        LatLngPoint pointActual = new LatLngPoint(latitude, longitude);
                                        Animal animalAux = animals.stream()
                                                .filter(animal -> animal.getDeviceId() != null && animal.getDeviceId().equals(id))
                                                .findFirst()
                                                .orElse(null);
                                        if (latitude != null && longitude != null) {
                                            AnimalLocation location = new AnimalLocation(id, pointActual, animalAux);
                                            animalLocations.add(location);
                                        }
                                    }
                                    showAnimals();
                                }
                            } else {
                                Toast.makeText(this, "Error al obtener ubicaciones", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<List<String>> splitList(List<String> originalList, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i += chunkSize) {
            chunks.add(originalList.subList(i, Math.min(i + chunkSize, originalList.size())));
        }
        return chunks;
    }

    private void loadRegions(String userId){
        try{
            db.collection("regions")
                    .whereEqualTo("userId", userId)
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
        catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnimals() {
        if (googleMap == null) return;

        Set<String> currentIds = new HashSet<>();

        for (AnimalLocation location : animalLocations) {
            String animalId = location.getAnimal().getId();
            LatLng newPos = new LatLng(location.getPoint().getLatitude(), location.getPoint().getLongitude());
            currentIds.add(animalId);

            Marker marker = animalMarkers.get(animalId);
            if (marker == null) {
                // 🔰 Nuevo marcador
                String type = location.getAnimal().getType();
                String iconName = "";
                switch (type) {
                    case "Vaca": iconName = "typeanimal_cow"; break;
                    case "Caballo": iconName = "typeanimal_horse"; break;
                    case "Oveja": iconName = "typeanimal_sheep"; break;
                }

                int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());

                MarkerOptions options = new MarkerOptions()
                        .position(newPos)
                        .title(location.getAnimal().getName());

                if (iconResId != 0) {
                    options.icon(getScaledBitmapDescriptor(iconResId, 64, 64));
                }

                marker = googleMap.addMarker(options);
                animalMarkers.put(animalId, marker);
            } else {
                // 🔄 Solo actualizar posición si cambió
                if (!marker.getPosition().equals(newPos)) {
                    marker.setPosition(newPos);
                }
            }
        }

        // 🧹 Eliminar marcadores que ya no están
        Iterator<Map.Entry<String, Marker>> iterator = animalMarkers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Marker> entry = iterator.next();
            if (!currentIds.contains(entry.getKey())) {
                entry.getValue().remove(); // elimina el marcador del mapa
                iterator.remove();         // elimina del Map
            }
        }
    }

    private void showAnimals2() {
        if (googleMap != null) {
            googleMap.clear();
            if (isShowRegions) {
                for (Region region : regions) {
                    showPolygonOnMap(region); // 🔁 Vuelve a mostrar las regiones si estaban activas
                }
            }
            for (AnimalLocation location : animalLocations) {
                LatLng point = new LatLng(location.getPoint().getLatitude(), location.getPoint().getLongitude());
                Animal animal = location.getAnimal();

                String iconName = "";
                switch (animal.getType()) {
                    case "Vaca": iconName = "typeanimal_cow"; break;
                    case "Caballo": iconName = "typeanimal_horse"; break;
                    case "Oveja": iconName = "typeanimal_sheep"; break;
                }

                int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(point)
                        .title(animal.getName());

                if (iconResId != 0) {
                    markerOptions.icon(getScaledBitmapDescriptor(iconResId, 64, 64));
                }

                googleMap.addMarker(markerOptions);
            }
        }
    }

    private BitmapDescriptor getScaledBitmapDescriptor(int resId, int width, int height) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), resId);
        Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaled);
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
                //googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
            }
        });

        loadAnimals();
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
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_mapView_map.onPause();
        stopLocationUpdates();
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

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAnimalsLocations();
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private void startLocationUpdates() {
        handler.post(refreshRunnable);
    }

    private void stopLocationUpdates() {
        handler.removeCallbacks(refreshRunnable);
    }
}