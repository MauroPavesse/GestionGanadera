package com.pequesystems.gestionganadera.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.pequesystems.gestionganadera.R;

import java.util.List;

public class AuthActivity extends AppCompatActivity {

    EditText auth_editText_email,
            auth_editText_password;
    Button auth_button_login,
            auth_button_register;
    TextView auth_textView_forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        setTitle("Autenticación");

        auth_editText_email = findViewById(R.id.auth_editText_email);
        auth_editText_password = findViewById(R.id.auth_editText_password);
        auth_button_login = findViewById(R.id.auth_button_login);
        auth_button_register = findViewById(R.id.auth_button_register);
        auth_textView_forgotPassword = findViewById(R.id.auth_textView_forgotPassword);

        auth_button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = auth_editText_email.getText().toString().trim();
                String password = auth_editText_password.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
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
                                        // Obtener la excepción y verificar el tipo de error
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            // Este error indica que el email ya está registrado
                                            Toast.makeText(AuthActivity.this, "Este email ya está registrado", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            // Otros errores
                                            Toast.makeText(AuthActivity.this, "Error al registrarse", Toast.LENGTH_LONG).show();
                                        }
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

                if (!email.isEmpty() && !password.isEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Inicio de sesión exitoso
                                        loadUserDataAndShowHome();
                                    } else {
                                        // Manejar errores específicos
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            // Email no registrado
                                            Toast.makeText(AuthActivity.this, "Usuario no registrado", Toast.LENGTH_LONG).show();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            // Contraseña incorrecta
                                            Toast.makeText(AuthActivity.this, "Datos incorrectos", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            // Otros errores
                                            Toast.makeText(AuthActivity.this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                } else {
                    // Si los campos están vacíos, mostrar un mensaje de error
                    Toast.makeText(AuthActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        auth_textView_forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        // Inflar el layout personalizado para el modal
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_forgot_password, null);

        // Crear el diálogo
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        // Obtener referencias a las vistas dentro del modal
        EditText emailEditText = view.findViewById(R.id.editTextEmail);
        Button submitButton = view.findViewById(R.id.buttonSubmit);

        // Configurar el comportamiento del botón Enviar
        submitButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Por favor, ingresa un correo electrónico", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí puedes implementar la lógica para enviar el correo de recuperación de contraseña
                sendPasswordResetEmail(email);
                alertDialog.dismiss();
            }
        });

        // Mostrar el diálogo
        alertDialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Verificar si el email está registrado en Firebase
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener los métodos de inicio de sesión asociados con el email
                        List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // El email está registrado, proceder con el envío de la recuperación
                            auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(AuthActivity.this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AuthActivity.this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // El email no está registrado
                            Toast.makeText(AuthActivity.this, "No existe ninguna cuenta registrada con ese correo", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AuthActivity.this, "Error al verificar el correo", Toast.LENGTH_SHORT).show();
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