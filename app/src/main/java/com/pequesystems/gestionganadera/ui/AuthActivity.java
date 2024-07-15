package com.pequesystems.gestionganadera.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.pequesystems.gestionganadera.R;

public class AuthActivity extends AppCompatActivity {

    EditText auth_editText_email,
            auth_editText_password;
    Button auth_button_login,
            auth_button_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        setTitle("Autenticación");

        auth_editText_email = findViewById(R.id.auth_editText_email);
        auth_editText_password = findViewById(R.id.auth_editText_password);
        auth_button_login = findViewById(R.id.auth_button_login);
        auth_button_register = findViewById(R.id.auth_button_register);

        auth_button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = auth_editText_email.getText().toString().trim();
                String password = auth_editText_password.getText().toString().trim();

                if(!email.isEmpty() && !password.isEmpty()){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AuthActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {
                                            createUserInFirestore(firebaseUser);
                                        }
                                    } else {
                                        Toast.makeText(AuthActivity.this, "Error al registrarse", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    // Si los campos están vacíos, mostrar un mensaje de error
                    Toast.makeText(AuthActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        auth_button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = auth_editText_email.getText().toString().trim();
                String password = auth_editText_password.getText().toString().trim();

                if(!email.isEmpty() && !password.isEmpty()){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        loadUserDataAndShowHome();
                                    } else {
                                        // Si el inicio de sesión falla, mostrar un mensaje al usuario
                                        Toast.makeText(AuthActivity.this, "Usted no se encuentra registrado", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    // Si los campos están vacíos, mostrar un mensaje de error
                    Toast.makeText(AuthActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserInFirestore(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.pequesystems.gestionganadera.models.User newUser = new com.pequesystems.gestionganadera.models.User(
                firebaseUser.getUid(),
                "username",
                auth_editText_password.getText().toString(),
                auth_editText_email.getText().toString()
        );

        db.collection("users").document(firebaseUser.getUid()).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AuthActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    saveUserToSharedPreferences(newUser);
                    showHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AuthActivity.this, "Error al guardar usuario en Firestore", Toast.LENGTH_LONG).show();
                });
    }

    private void showHome(){
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Opcional: cerrar la actividad actual
    }

    private void saveUserToSharedPreferences(com.pequesystems.gestionganadera.models.User user) {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_id", user.getId());
        editor.putString("user_username", user.getUsername());
        editor.putString("user_password", user.getPassword());
        editor.putString("user_email", user.getEmail());
        editor.apply();
    }

    private void loadUserDataAndShowHome() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                com.pequesystems.gestionganadera.models.User currentUser = documentSnapshot.toObject(com.pequesystems.gestionganadera.models.User.class);
                if (currentUser != null) {
                    saveUserToSharedPreferences(currentUser);
                    showHome();
                }
            }
        }).addOnFailureListener(e -> {
            // Manejar error
        });
    }
}