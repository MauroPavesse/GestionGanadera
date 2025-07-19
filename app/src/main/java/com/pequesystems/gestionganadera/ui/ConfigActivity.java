package com.pequesystems.gestionganadera.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pequesystems.gestionganadera.R;

import java.util.Arrays;
import java.util.List;

public class ConfigActivity extends AppCompatActivity {

    private Button btnSave, btnDeleteAccount, btnRequestLocation, btnViewUserManual;
    private EditText editTextNewPassword;
    private ImageButton imagenButtonTogglePassword, config_button_back;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private TextView locationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        btnSave = findViewById(R.id.config_button_save);
        btnDeleteAccount = findViewById(R.id.config_button_deleteAccount);
        editTextNewPassword = findViewById(R.id.config_editText_password);
        imagenButtonTogglePassword = findViewById(R.id.config_imagenButton_togglePassword);
        locationStatus = findViewById(R.id.config_textView_locationStatus);
        btnRequestLocation = findViewById(R.id.config_button_requestLocation);
        btnViewUserManual = findViewById(R.id.config_button_userManual);
        config_button_back = findViewById(R.id.config_imageButton_back);

        config_button_back.setOnClickListener(v -> {
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        final boolean[] isPasswordVisible = {false};

        imagenButtonTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                // Ocultar contraseña
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imagenButtonTogglePassword.setImageResource(R.drawable.ic_action_visibility_off);
            } else {
                // Mostrar contraseña
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imagenButtonTogglePassword.setImageResource(R.drawable.ic_action_visibility);
            }
            isPasswordVisible[0] = !isPasswordVisible[0];
            editTextNewPassword.setSelection(editTextNewPassword.getText().length());
        });

        btnSave.setOnClickListener(v -> cambiarPassword());

        btnDeleteAccount.setOnClickListener(v -> eliminarCuenta());

        // Actualizar estado inicial
        updateLocationStatus();

        // Botón para solicitar permiso
        btnRequestLocation.setOnClickListener(v -> {
            requestLocationPermission();
        });

        btnViewUserManual.setOnClickListener(v -> {
            Intent intent = new Intent(ConfigActivity.this, PdfActivity.class);
            startActivity(intent);
        });
    }

    private void cambiarPassword() {
        String passwordNueva = editTextNewPassword.getText().toString().trim();

        if (passwordNueva.isEmpty()) {
            Toast.makeText(this, "Ingresa la nueva contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String passwordActual = sharedPref.getString("user_password", "");

        // Verificar si el usuario está autenticado
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), passwordActual);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Ahora cambiamos la contraseña
                        user.updatePassword(passwordNueva)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show();
                                        editor.putString("user_password", passwordNueva);
                                        editor.apply();
                                        finish(); // Cerrar la actividad
                                    } else {
                                        Toast.makeText(this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarCuenta() {
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            reautenticarYEliminar();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void reautenticarYEliminar() {
        // Pedimos al usuario que ingrese su contraseña antes de eliminar
        EditText edtPassword = new EditText(this);
        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Reautenticación")
                .setMessage("Introduce tu contraseña actual para continuar")
                .setView(edtPassword)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String password = edtPassword.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Debes ingresar la contraseña", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    borrarDatosRelacionados();
                                } else {
                                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarDatosRelacionados() {
        String userId = user.getUid();

        // Lista de colecciones donde hay datos del usuario
        List<String> colecciones = Arrays.asList("animals", "regions");

        for (String coleccion : colecciones) {
            db.collection(coleccion)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            db.collection(coleccion).document(document.getId()).delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error al eliminar documentos de " + coleccion, e);
                    });
        }

        // Eliminar el documento del usuario en la colección "Usuarios"
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    eliminarCuentaFirebase();  // Una vez eliminados los datos, eliminamos la cuenta
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar el perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarCuentaFirebase() {
        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateLocationStatus() {
        if (hasLocationPermission()) {
            locationStatus.setText("Permiso de ubicación: Otorgado ✅");
        } else {
            locationStatus.setText("Permiso de ubicación: Denegado ❌");
        }
    }

    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            Toast.makeText(this, "El permiso ya está otorgado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            updateLocationStatus();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación otorgado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}