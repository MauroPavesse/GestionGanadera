package com.pequesystems.gestionganadera.ui;

import android.content.Intent;
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
import com.pequesystems.gestionganadera.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        setTitle("Autenticación");

        EditText auth_editText_email = findViewById(R.id.auth_editText_email),
                auth_editText_password = findViewById(R.id.auth_editText_password);
        Button auth_button_login = findViewById(R.id.auth_button_login),
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
                                        showHome();
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
                                        showHome();
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

    private void showHome(){
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Opcional: cerrar la actividad actual
    }
}