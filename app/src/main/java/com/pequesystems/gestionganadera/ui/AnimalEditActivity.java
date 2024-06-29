package com.pequesystems.gestionganadera.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.Animal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalEditActivity extends AppCompatActivity {

    private Spinner animalEdit_spinner_types;
    private EditText animalEdit_editText_name, animalEdit_editText_sex;
    private Button animalEdit_button_save, animalEdit_button_back;
    private FirebaseFirestore db;
    private String id;
    private boolean isEditMode;
    private Map<String, String> typeMap;
    private List<String> typeNames;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_edit);

        animalEdit_editText_name = findViewById(R.id.animalEdit_editText_name);
        animalEdit_editText_sex = findViewById(R.id.animalEdit_editText_sex);
        animalEdit_spinner_types = findViewById(R.id.animalEdit_spinner_types);
        animalEdit_button_save = findViewById(R.id.animalEdit_button_save);
        animalEdit_button_back = findViewById(R.id.animalEdit_button_back);
        db = FirebaseFirestore.getInstance();
        typeMap = new HashMap<>();
        typeNames = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animalEdit_spinner_types.setAdapter(spinnerAdapter);

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            id = getIntent().getStringExtra("ID");
            animalEdit_editText_name.setText(getIntent().getStringExtra("Name"));
            animalEdit_editText_sex.setText(getIntent().getStringExtra("Sex"));
            /*String type = getIntent().getStringExtra("Type");*/
            /*animalEdit_spinner_types.setSelection(typeNames.indexOf(type));*/
        }

        loadTypes();

        animalEdit_button_save.setOnClickListener(v -> {
            String name = animalEdit_editText_name.getText().toString();
            String typeName = animalEdit_spinner_types.getSelectedItem().toString();
            String typeId = getTypeIdFromName(typeName);
            String sex = animalEdit_editText_sex.getText().toString();

            if (typeId == null) {
                Toast.makeText(this, "Tipo de animal inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                updateAnimal(id, name, typeId, typeName, sex);
            } else {
                addAnimal(name, typeId, sex);
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

    private void updateAnimal(String id, String name, String typeId, String type, String sex) {
        db.collection("animals").document(id)
                .update("name", name, "typeId", typeId, "type", typeMap.get(typeId), "sex", sex);
    }

    private void addAnimal(String name, String typeId, String sex) {
        Animal newAnimal = new Animal(name, typeId, typeMap.get(typeId), sex);
        db.collection("animals").add(newAnimal).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            newAnimal.setId(id);
            db.collection("animals").document(id).set(newAnimal);
        }).addOnFailureListener(e -> {
            // Manejar el error si es necesario
        });
    }
}