package com.mt.mytutors.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.SetOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mt.mytutors.R;
import com.mt.mytutors.utils.CameraHelper;
import com.mt.mytutors.utils.LocationHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private TextView tvNombre, tvEmail, tvUbicacion, tvRol, tvFechaRegistro;
    private Button btnCambiarFoto, btnObtenerUbicacion, btnCerrarSesion, btnEditarPerfil;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();


        if (currentUser == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        initViews();
        setupToolbar(); // Configurar la toolbar
        setupListeners(); // Configurar listeners antes de cargar el perfil
        loadUserProfile(); // Cargar perfil después de configurar listeners
    }

    private void initViews() {
        // Corregido: usar el ID correcto del layout
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvRol = findViewById(R.id.tvRol);
        tvFechaRegistro = findViewById(R.id.tvFechaRegistro);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);

        if (ivProfilePhoto == null) {
            android.util.Log.e("PerfilActivity", "ERROR: ivProfilePhoto es null. Verificar ID en XML.");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }


    private void setupListeners() {
        // Verificar que los botones no sean null antes de asignar listeners
        if (btnCambiarFoto != null) {
            btnCambiarFoto.setOnClickListener(v -> showPhotoOptions());
        }

        if (btnObtenerUbicacion != null) {
            btnObtenerUbicacion.setOnClickListener(v -> getLocation());
        }

        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Cerrar Sesión")
                        .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            mAuth.signOut();
                            goToLogin();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        if (btnEditarPerfil != null) {
            btnEditarPerfil.setOnClickListener(v -> {
                Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show();
            });
        }

        // Permitir cambiar foto tocando la imagen
        if (ivProfilePhoto != null) {
            ivProfilePhoto.setOnClickListener(v -> showPhotoOptions());
        }
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmail != null) tvEmail.setText(currentUser.getEmail());

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (document.exists()) {
                        String nombre = document.getString("nombre");
                        String ubicacion = document.getString("ubicacion");
                        String tipoUsuario = document.getString("tipoUsuario"); // ← alumno/profesor
                        String rolEnApp = document.getString("rolEnApp"); // ← tutor/tutorado

                        if (nombre != null && tvNombre != null) {
                            tvNombre.setText(nombre);
                        }

                        if (ubicacion != null && tvUbicacion != null) {
                            tvUbicacion.setText("📍 " + ubicacion);
                        }

                        // MOSTRAR ROL CORRECTAMENTE
                        if (tvRol != null) {
                            String rolTexto = "";

                            // Primero mostrar tipo de usuario
                            if (tipoUsuario != null) {
                                rolTexto = tipoUsuario.equals("alumno") ? "Alumno" : "Profesor";
                            }

                            // Agregar rol en app si existe
                            if (rolEnApp != null && !rolEnApp.isEmpty()) {
                                String rolApp = rolEnApp.equals("tutor") ? "Tutor" : "Tutorado";
                                rolTexto = rolTexto.isEmpty() ? rolApp : rolTexto + " • " + rolApp;
                            }

                            // Si no hay ningún rol, mostrar "Usuario"
                            if (rolTexto.isEmpty()) {
                                rolTexto = "Usuario";
                            }

                            tvRol.setText(rolTexto);
                        }

                        // Fecha de registro
                        if (document.getTimestamp("fechaRegistro") != null && tvFechaRegistro != null) {
                            Date fecha = document.getTimestamp("fechaRegistro").toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            tvFechaRegistro.setText(sdf.format(fecha));
                        }

                        // Cargar foto desde Base64
                        String fotoBase64 = document.getString("fotoBase64");
                        if (fotoBase64 != null && !fotoBase64.isEmpty() && ivProfilePhoto != null) {
                            try {
                                byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                ivProfilePhoto.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                android.util.Log.e("PerfilActivity", "Error al decodificar imagen: " + e.getMessage());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }
    private void showPhotoOptions() {
        String[] options = {"📷 Tomar foto", "🖼️ Elegir de galería", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle("Cambiar foto de perfil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Cámara
                            if (CameraHelper.checkCameraPermission(this)) {
                                CameraHelper.openCamera(this);
                            }
                            break;
                        case 1: // Galería
                            CameraHelper.openGallery(this);
                            break;
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CameraHelper.REQUEST_CAMERA) {
                String photoPath = CameraHelper.getCurrentPhotoPath();
                if (photoPath != null) {
                    File photoFile = new File(photoPath);
                    Uri photoUri = Uri.fromFile(photoFile);
                    uploadPhotoToFirebase(photoUri);
                }
            } else if (requestCode == CameraHelper.REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    uploadPhotoToFirebase(selectedImage);
                }
            }
        }
    }

    private void uploadPhotoToFirebase(Uri photoUri) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Procesando foto...", Toast.LENGTH_SHORT).show();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Map<String, Object> updates = new HashMap<>();
            updates.put("fotoBase64", base64Image);

            db.collection("usuarios").document(currentUser.getUid())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "✓ Foto actualizada", Toast.LENGTH_SHORT).show();
                        if (ivProfilePhoto != null) {
                            ivProfilePhoto.setImageBitmap(scaled);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        if (!LocationHelper.checkLocationPermission(this)) {
            return;
        }

        Toast.makeText(this, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();
        btnObtenerUbicacion.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);

        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback2() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnObtenerUbicacion.setEnabled(true);
                    String ubicacionText = String.format(Locale.getDefault(),
                            "Lat: %.4f, Lon: %.4f", latitude, longitude);
                    tvUbicacion.setText("📍 " + ubicacionText);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ubicacion", ubicacionText);
                    updates.put("latitud", latitude);
                    updates.put("longitud", longitude);

                    db.collection("usuarios").document(currentUser.getUid())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(PerfilActivity.this,
                                        "✓ Ubicación guardada", Toast.LENGTH_SHORT).show();
                            });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (btnObtenerUbicacion != null) btnObtenerUbicacion.setEnabled(true);
                    Toast.makeText(PerfilActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CameraHelper.PERMISSION_CAMERA) {
                CameraHelper.openCamera(this);
            } else if (requestCode == LocationHelper.PERMISSION_LOCATION) {
                getLocation();
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
