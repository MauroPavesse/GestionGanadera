package com.pequesystems.gestionganadera.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.Animal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class AnimalEditActivity extends AppCompatActivity {

    private Spinner animalEdit_spinner_types, animalEdit_spinner_sex;
    private EditText animalEdit_editText_name, animalEdit_editText_deviceId, animalEdit_editTextDate_birthdate;
    private Button animalEdit_button_save;
    private ImageButton animalEdit_button_back, animalEdit_imageButon_qr, animalEdit_imageButton_delete, animalEdit_imageButton_datetime;
    private FirebaseFirestore db;
    private String id;
    private boolean isEditMode;
    private Map<String, String> typeMap;
    private List<String> typeNames;
    private ArrayAdapter<String> spinnerAdapter, spinnerAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_edit);

        animalEdit_editText_name = findViewById(R.id.animalEdit_editText_name);
        animalEdit_spinner_types = findViewById(R.id.animalEdit_spinner_types);
        animalEdit_editText_deviceId = findViewById(R.id.animalEdit_editText_deviceId);
        animalEdit_button_save = findViewById(R.id.animalEdit_button_save);
        animalEdit_button_back = findViewById(R.id.animalEdit_imageButton_back);
        animalEdit_imageButon_qr = findViewById(R.id.animalEdit_imageButton_qr);
        animalEdit_spinner_sex = findViewById(R.id.animalEdit_spinner_sex);
        animalEdit_editTextDate_birthdate = findViewById(R.id.animalEdit_editTextDate_birthdate);
        animalEdit_imageButton_delete = findViewById(R.id.animalEdit_imageButton_delete);
        animalEdit_imageButton_datetime = findViewById(R.id.animalEdit_imageButton_datetime);

        db = FirebaseFirestore.getInstance();
        typeMap = new HashMap<>();
        typeNames = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animalEdit_spinner_types.setAdapter(spinnerAdapter);

        ArrayList<String> itemsSex = new ArrayList<>();
        itemsSex.add("Macho");
        itemsSex.add("Hembra");
        spinnerAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemsSex);
        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animalEdit_spinner_sex.setAdapter(spinnerAdapter2);

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            animalEdit_imageButton_delete.setAlpha(1.0f); // 100% de opacidad
            animalEdit_imageButton_delete.setEnabled(true);

            id = getIntent().getStringExtra("ID");
            animalEdit_editText_name.setText(getIntent().getStringExtra("Name"));
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) animalEdit_spinner_sex.getAdapter();
            int position = adapter.getPosition(getIntent().getStringExtra("Sex")); // Obtener la posición del ítem
            if (position >= 0) // Verificar que el ítem existe
                animalEdit_spinner_sex.setSelection(position); // Seleccionar el ítem
            animalEdit_editTextDate_birthdate.setText(getIntent().getStringExtra("Birthdate"));
            animalEdit_editText_deviceId.setText(getIntent().getStringExtra("DeviceId"));
        }else{
            animalEdit_imageButton_delete.setAlpha(0.5f); // 50% de opacidad
            animalEdit_imageButton_delete.setEnabled(false);
        }

        loadTypes();

        animalEdit_button_save.setOnClickListener(v -> {
            String name = animalEdit_editText_name.getText().toString();
            String typeName = animalEdit_spinner_types.getSelectedItem().toString();
            String typeId = getTypeIdFromName(typeName);
            String sex = animalEdit_spinner_sex.getSelectedItem().toString(); //animalEdit_editText_sex.getText().toString();
            String deviceId = animalEdit_editText_deviceId.getText().toString();
            String birthdate = animalEdit_editTextDate_birthdate.getText().toString();

            if (typeId == null) {
                Toast.makeText(this, "Tipo de animal inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                updateAnimal(id, name, typeId, typeName, sex, deviceId, birthdate);
            } else {
                addAnimal(name, typeId, sex, deviceId, birthdate);
            }
            Toast.makeText(this, isEditMode ? "Datos actualizados" : "Datos agregados", Toast.LENGTH_SHORT).show();
            finish();
        });

        animalEdit_button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        animalEdit_imageButon_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AnimalEditActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AnimalEditActivity.this, new String[]{Manifest.permission.CAMERA}, 50);
                } else {
                    // Iniciar el escáner de códigos QR
                    IntentIntegrator integrator = new IntentIntegrator(AnimalEditActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE); // Solo leer códigos QR
                    integrator.setPrompt("Escanea un código QR"); // Mensaje que aparecerá
                    integrator.setCameraId(0); // Usar la cámara trasera
                    integrator.setBeepEnabled(true); // Habilitar el sonido al escanear
                    integrator.setBarcodeImageEnabled(true); // Habilitar imagen del código de barras
                    integrator.initiateScan();
                }
            }
        });

        animalEdit_imageButton_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditMode){
                    if (id.length() > 0) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("animals").document(id)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AnimalEditActivity.this, "Animal eliminado", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AnimalEditActivity.this, "Error al eliminar el animal", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
        });

        animalEdit_imageButton_datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Aquí puedes manejar la fecha seleccionada
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        animalEdit_editTextDate_birthdate.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // Manejar el resultado del escaneo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Obtener el resultado del escaneo
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Mostrar el texto del QR en el TextView
                animalEdit_editText_deviceId.setText(result.getContents());
            } else {
                // Si no se obtuvo un resultado
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadTypes() {
        db.collection("typesAnimals").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String type = document.getString("type");
                            typeMap.put(id, type);
                            typeNames.add(type);
                        }
                        spinnerAdapter.notifyDataSetChanged();

                        // Establecer la selección del Spinner aquí
                        if (isEditMode) {
                            String type = getIntent().getStringExtra("Type");
                            int position = typeNames.indexOf(type);
                            if (position >= 0) {
                                animalEdit_spinner_types.setSelection(position);
                            }
                        }
                    } else {
                        // Handle error
                    }
                });
    }

    private String getTypeIdFromName(String typeName) {
        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            if (entry.getValue().equals(typeName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void updateAnimal(String id, String name, String typeId, String type, String sex, String deviceId, String birthdate) {
        db.collection("animals").document(id)
                .update("name", name, "typeId", typeId, "type", typeMap.get(typeId), "sex", sex, "deviceId", deviceId, "birthdate", birthdate);
    }

    private void addAnimal(String name, String typeId, String sex, String deviceId, String birthdate) {
        Animal newAnimal = new Animal(name, typeId, typeMap.get(typeId), sex, deviceId, birthdate, birthdate);
        db.collection("animals").add(newAnimal).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            newAnimal.setId(id);
            db.collection("animals").document(id).set(newAnimal);
        }).addOnFailureListener(e -> {
            // Manejar el error si es necesario
        });
    }
}