package com.pequesystems.gestionganadera.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.adapters.AnimalAdapter;
import com.pequesystems.gestionganadera.models.Animal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private List<Animal> dataList;
    private FirebaseFirestore db;
    private Button btnAdd;
    private ImageButton animals_button_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animals);

        animals_button_back = findViewById(R.id.animals_imageButton_back);
        recyclerView = findViewById(R.id.animals_recyclerView_itemsAnimals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        adapter = new AnimalAdapter(dataList, this);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        loadData();

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AnimalsActivity.this, AnimalEditActivity.class);
            intent.putExtra("isEditMode", false);
            startActivity(intent);
        });

        animals_button_back.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadData() {
        // Primero cargar los tipos de animales
        db.collection("typesAnimals").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> tiposMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String type = document.getString("type");
                            tiposMap.put(id, type);
                        }

                        // Luego cargar los animales
                        db.collection("animals")
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            // Manejar error
                                            return;
                                        }
                                        dataList.clear();
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            String id = document.getId();
                                            String name = document.getString("name");
                                            String typeId = document.getString("typeId");
                                            String sex = document.getString("sex");
                                            String deviceId = document.getString("deviceId");
                                            String birthdate = document.getString("birthdate");

                                            // Desnormalizar el tipo de animal
                                            String type = tiposMap.get(typeId);

                                            dataList.add(new Animal(id, name, typeId, type, sex, deviceId, birthdate));
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    } else {
                        // Manejar error
                    }
                });
    }

}