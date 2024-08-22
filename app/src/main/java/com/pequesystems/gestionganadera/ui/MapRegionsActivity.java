package com.pequesystems.gestionganadera.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.LatLngPoint;
import com.pequesystems.gestionganadera.models.Region;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MapRegionsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    MapView mapRegions_mapView_map;
    ImageButton magRegions_imageButton_back;
    Button mapRegions_button_save, mapRegions_button_new, mapRegions_button_delete;
    EditText mapRegions_editText_nameRegion;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;
    private Spinner spinnerPolygons;
    private List<Region> regions = new ArrayList<>();
    private boolean isDrawingMode = false; // Nueva bandera para controlar el modo de dibujo
    private Button colorButton;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_regions);

        spinnerPolygons = findViewById(R.id.mapRegions_spinner_regions);
        mapRegions_editText_nameRegion = findViewById(R.id.mapRegions_editText_nameRegion);
        magRegions_imageButton_back = findViewById(R.id.mapRegions_imageButton_back);
        mapRegions_mapView_map = findViewById(R.id.mapRegions_mapView_map);
        mapRegions_button_save = findViewById(R.id.mapRegions_button_save);
        mapRegions_button_new = findViewById(R.id.mapRegions_button_new);
        mapRegions_button_delete = findViewById(R.id.mapRegions_button_delete);
        colorButton = findViewById(R.id.mapRegions_button_color);

        mapRegions_editText_nameRegion.setEnabled(false);
        mapRegions_button_save.setEnabled(false);
        colorButton.setEnabled(false);

        magRegions_imageButton_back.setOnClickListener(v -> {
            finish();
        });

        // Cargar nombres de polígonos en el Spinner
        loadPolygonNames();

        mapRegions_mapView_map.onCreate(savedInstanceState);
        mapRegions_mapView_map.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapRegions_button_delete.setOnClickListener(v -> {
            if(mapRegions_button_new.isEnabled()){
                // Obtener el ID del registro seleccionado en el Spinner
                int selectedPosition = spinnerPolygons.getSelectedItemPosition();
                if (selectedPosition >= 0 && selectedPosition < regions.size()) {
                    String regionId = regions.get(selectedPosition).getId();
                    deletePolygonFromFirestore(regionId);
                } else {
                    Toast.makeText(this, "Seleccione un polígono válido", Toast.LENGTH_SHORT).show();
                }
            }else{
                if (polygon != null) {
                    polygon.remove();
                    polygon = null;
                }
                polygonPoints.clear();
                googleMap.clear();
                mapRegions_button_new.setEnabled(true);
                colorButton.setEnabled(false);
                mapRegions_editText_nameRegion.setEnabled(false);
                mapRegions_editText_nameRegion.setText("");
                mapRegions_button_save.setEnabled(false);
                mapRegions_button_delete.setText("ELIMINAR");
                showPolygonOnMap(regions.get(spinnerPolygons.getSelectedItemPosition()));
                isDrawingMode = false; // Desactivar el modo de dibujo
            }
        });

        mapRegions_button_save.setOnClickListener(v -> {
            if(mapRegions_editText_nameRegion.length() == 0){
                Toast.makeText(this, "Debe asignarle un nombra a la zona", Toast.LENGTH_SHORT).show();
                return;
            }
            savePolygonToFirestore(mapRegions_editText_nameRegion.getText().toString());
        });

        mapRegions_button_new.setOnClickListener(v -> {
            if (polygon != null) {
                polygon.remove();
                polygon = null;
            }
            polygonPoints.clear();
            googleMap.clear();
            mapRegions_button_new.setEnabled(false);
            mapRegions_editText_nameRegion.setEnabled(true);
            mapRegions_button_save.setEnabled(true);
            mapRegions_button_delete.setText("CANCELAR");
            isDrawingMode = true; // Activar el modo de dibujo
            currentColor = Color.GREEN;
            colorButton.setEnabled(true);
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPickerDialog();
            }
        });
    }

    private void openColorPickerDialog() {
        AmbilWarnaDialog colorPickerDialog = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                currentColor = color;
                if(polygon != null){
                    polygon.setStrokeColor(color);
                    polygon.setFillColor(getFilteredColor(color));
                }
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Acción al cancelar
            }
        });
        colorPickerDialog.show();
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

        // Configurar listener de clics en el mapa
        googleMap.setOnMapClickListener(latLng -> {
            if (isDrawingMode) { // Solo agregar puntos si el modo de dibujo está activado
                polygonPoints.add(latLng);
                googleMap.addMarker(new MarkerOptions().position(latLng));
                drawPolygon();
            }
        });
    }

    private void drawPolygon() {
        if (polygon != null) {
            polygon.remove();
        }
        if (polygonPoints.size() > 2) {
            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(polygonPoints)
                    .strokeColor(currentColor)
                    .fillColor(getFilteredColor(currentColor))
                    .strokeWidth(5);
            polygon = googleMap.addPolygon(polygonOptions);
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
                            //googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapRegions_mapView_map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapRegions_mapView_map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapRegions_mapView_map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapRegions_mapView_map.onLowMemory();
    }

    private void savePolygonToFirestore(String nombre) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<LatLngPoint> latLngPoints = new ArrayList<>();
        if(polygonPoints.size() <= 2){
            Toast.makeText(this, "Debe marcar puntos en el mapa hasta crear un polígono", Toast.LENGTH_SHORT).show();
        }
        for (LatLng latLng : polygonPoints) {
            latLngPoints.add(new LatLngPoint(latLng.latitude, latLng.longitude));
        }

        String regionId = db.collection("regions").document().getId(); // Generar un ID único
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String usuarioId = sharedPref.getString("user_id", "");

        Region region = new Region(regionId, usuarioId, latLngPoints, nombre, currentColor);

        db.collection("regions").document(regionId)
                .set(region)
                .addOnSuccessListener(aVoid ->{
                    Toast.makeText(this, "Polígono guardado", Toast.LENGTH_SHORT).show();
                    if (polygon != null) {
                        polygon.remove();
                        polygon = null;
                    }
                    polygonPoints.clear();
                    googleMap.clear();
                    loadPolygonNames();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el polígono", Toast.LENGTH_SHORT).show());
        mapRegions_button_new.setEnabled(true);
        colorButton.setEnabled(false);
        mapRegions_editText_nameRegion.setEnabled(false);
        mapRegions_editText_nameRegion.setText("");
        mapRegions_button_save.setEnabled(false);
        mapRegions_button_delete.setText("ELIMINAR");
        isDrawingMode = false;
    }

    private void loadPolygonNames() {
        regions.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String usuarioId = sharedPref.getString("user_id", "");

        db.collection("regions")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> nombres = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Region region = document.toObject(Region.class);
                            regions.add(region);
                            nombres.add(region.getNombre());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPolygons.setAdapter(adapter);

                        spinnerPolygons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                showPolygonOnMap(regions.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // No hacer nada
                            }
                        });
                    } else {
                        Toast.makeText(this, "Error al cargar los nombres de los polígonos", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showPolygonOnMap(Region region) {
        if (googleMap != null) {
            googleMap.clear();
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
    }

    private void deletePolygonFromFirestore(String regionId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("regions").document(regionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Polígono eliminado", Toast.LENGTH_SHORT).show();
                    if (polygon != null) {
                        polygon.remove();
                        polygon = null;
                    }
                    polygonPoints.clear();
                    googleMap.clear();
                    loadPolygonNames(); // Recargar los nombres después de eliminar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar el polígono", Toast.LENGTH_SHORT).show();
                });
    }
}