package com.mt.mytutors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mt.mytutors.R;
import com.mt.mytutors.models.Carrera;
import com.mt.mytutors.models.Facultad;
import com.mt.mytutors.models.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * RegisterActivity - Pantalla de registro de nuevos usuarios
 */
public class RegisterActivity extends AppCompatActivity {

    // Vistas
    private ImageButton btnBack;
    private TextInputLayout tilNombre, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Spinner spFacultad, spCarrera;
    private RadioGroup rgTipoUsuario;
    private RadioButton rbAlumno, rbProfesor;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Datos
    private List<Facultad> facultades = new ArrayList<>();
    private List<Carrera> carreras = new ArrayList<>();
    private ArrayAdapter<Facultad> facultadAdapter;
    private ArrayAdapter<Carrera> carreraAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        initViews();

        // Configurar spinners
        setupSpinners();

        // Cargar datos
        loadFacultades();

        // Configurar listeners
        setupListeners();
    }

    /**
     * Inicializa las vistas
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilNombre = findViewById(R.id.tilNombre);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spFacultad = findViewById(R.id.spFacultad);
        spCarrera = findViewById(R.id.spCarrera);
        rgTipoUsuario = findViewById(R.id.rgTipoUsuario);
        rbAlumno = findViewById(R.id.rbAlumno);
        rbProfesor = findViewById(R.id.rbProfesor);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Configura los spinners con adaptadores
     */
    private void setupSpinners() {
        // Adapter para facultades
        facultadAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, facultades);
        facultadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFacultad.setAdapter(facultadAdapter);

        // Adapter para carreras
        carreraAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, carreras);
        carreraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarrera.setAdapter(carreraAdapter);
    }

    /**
     * Carga las facultades desde Firebase
     */
    private void loadFacultades() {
        db.collection("facultades")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    facultades.clear();
                    facultades.add(new Facultad("", "Selecciona una facultad"));

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Facultad facultad = doc.toObject(Facultad.class);
                        facultad.setId(doc.getId());
                        facultades.add(facultad);
                    }
                    facultadAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar facultades", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Carga las carreras de una facultad específica
     */
    private void loadCarreras(String idFacultad) {
        if (TextUtils.isEmpty(idFacultad)) {
            carreras.clear();
            carreras.add(new Carrera("", "Selecciona una carrera", ""));
            carreraAdapter.notifyDataSetChanged();
            return;
        }

        db.collection("carreras")
                .whereEqualTo("idFacultad", idFacultad)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carreras.clear();
                    carreras.add(new Carrera("", "Selecciona una carrera", ""));

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Carrera carrera = doc.toObject(Carrera.class);
                        carrera.setId(doc.getId());
                        carreras.add(carrera);
                    }
                    carreraAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar carreras", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Configura los listeners
     */
    private void setupListeners() {
        // Botón volver
        btnBack.setOnClickListener(v -> finish());

        // Cambio de facultad
        spFacultad.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Facultad facultad = facultades.get(position);
                    loadCarreras(facultad.getId());
                } else {
                    loadCarreras(null);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Botón registrar
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Link a login
        tvLogin.setOnClickListener(v -> finish());
    }

    /**
     * Intenta registrar al usuario
     */
    private void attemptRegister() {
        // Limpiar errores
        tilNombre.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Obtener valores
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar campos
        if (!validateFields(nombre, email, password, confirmPassword)) {
            return;
        }

        // Obtener selecciones
        Facultad facultad = (Facultad) spFacultad.getSelectedItem();
        Carrera carrera = (Carrera) spCarrera.getSelectedItem();
        String tipoUsuario = rbAlumno.isChecked() ? "alumno" : "profesor";

        // Validar selecciones
        if (spFacultad.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.error_select_faculty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (spCarrera.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.error_select_career), Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar loading
        showLoading(true);

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Crear documento de usuario en Firestore
                            saveUserToFirestore(firebaseUser.getUid(), nombre, email,
                                    facultad.getId(), carrera.getId(), tipoUsuario);
                        }
                    } else {
                        showLoading(false);
                        String errorMessage = "Error al crear cuenta";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            if (task.getException().getMessage().contains("email")) {
                                errorMessage = "El correo ya está registrado";
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Guarda el usuario en Firestore
     */
    private void saveUserToFirestore(String uid, String nombre, String email,
                                     String idFacultad, String idCarrera, String tipoUsuario) {
        Usuario usuario = new Usuario();
        usuario.setId(uid);
        usuario.setNombre(nombre);
        usuario.setCorreo(email);
        usuario.setIdFacultad(idFacultad);
        usuario.setIdCarrera(idCarrera);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setActivo(true);

        db.collection("usuarios")
                .document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al guardar datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validateFields(String nombre, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError(getString(R.string.error_empty_name));
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Muestra u oculta el loading
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        btnRegister.setEnabled(!show);
    }

    /**
     * Navega a la pantalla principal
     */
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}