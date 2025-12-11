package com.mt.mytutors.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mt.mytutors.R;
import com.mt.mytutors.utils.BiometricHelper;

/**
 * LoginActivity - Pantalla de inicio de sesión con Firebase Auth y Biometría
 */
public class LoginActivity extends AppCompatActivity {

    // Vistas
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnBiometric;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;

    // SharedPreferences para biometría
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MyTutorsPrefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_USER_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inicializar vistas
        initViews();

        // Configurar botón biométrico
        setupBiometricButton();

        // Configurar listeners
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si ya hay sesión activa
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHome();
        }
    }

    /**
     * Inicializa las vistas
     */
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBiometric = findViewById(R.id.btnBiometric);  // Nuevo botón
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Configura la visibilidad del botón biométrico
     */
    private void setupBiometricButton() {
        boolean biometricAvailable = BiometricHelper.isBiometricAvailable(this);
        boolean biometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        String savedEmail = prefs.getString(KEY_USER_EMAIL, null);

        if (biometricAvailable && biometricEnabled && savedEmail != null) {
            btnBiometric.setVisibility(View.VISIBLE);
            // Pre-llenar el email guardado
            etEmail.setText(savedEmail);
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        // Botón de login tradicional
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Botón de login biométrico
        btnBiometric.setOnClickListener(v -> attemptBiometricLogin());

        // Link a registro
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Link a recuperar contraseña
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Ingresa tu correo electrónico primero", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Intenta iniciar sesión con autenticación biométrica
     */
    private void attemptBiometricLogin() {
        BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
            @Override
            public void onSuccess() {
                // Autenticación biométrica exitosa
                Toast.makeText(LoginActivity.this,
                        "✓ Autenticación biométrica exitosa", Toast.LENGTH_SHORT).show();
                goToHome();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(LoginActivity.this,
                        "Error biométrico: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(LoginActivity.this,
                        "Huella no reconocida. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Intenta iniciar sesión con las credenciales ingresadas
     */
    private void attemptLogin() {
        // Limpiar errores previos
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Obtener valores
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar campos
        if (!validateFields(email, password)) {
            return;
        }

        // Mostrar loading
        showLoading(true);

        // Iniciar sesión con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Login exitoso - ofrecer biometría si está disponible
                        offerBiometricSetup(email);
                        goToHome();
                    } else {
                        // Error en login
                        String errorMessage = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null) {
                                if (error.contains("password")) {
                                    errorMessage = "Contraseña incorrecta";
                                } else if (error.contains("user") || error.contains("email")) {
                                    errorMessage = "Usuario no encontrado";
                                } else if (error.contains("network")) {
                                    errorMessage = "Error de conexión";
                                }
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Ofrece configurar autenticación biométrica después de un login exitoso
     */
    private void offerBiometricSetup(String email) {
        // Solo preguntar si:
        // 1. No está habilitada ya
        // 2. El dispositivo soporta biometría
        boolean alreadyEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        boolean biometricAvailable = BiometricHelper.isBiometricAvailable(this);

        if (!alreadyEnabled && biometricAvailable) {
            new AlertDialog.Builder(this)
                    .setTitle("🔐 Autenticación Biométrica")
                    .setMessage("¿Deseas usar tu huella digital para iniciar sesión más rápido en el futuro?")
                    .setPositiveButton("Sí, activar", (dialog, which) -> {
                        prefs.edit()
                                .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                                .putString(KEY_USER_EMAIL, email)
                                .apply();
                        Toast.makeText(this, "✓ Huella digital activada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No, gracias", null)
                    .setCancelable(false)
                    .show();
        } else if (alreadyEnabled) {
            // Actualizar el email si cambió
            prefs.edit().putString(KEY_USER_EMAIL, email).apply();
        }
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validateFields(String email, String password) {
        boolean isValid = true;

        // Validar email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Envía email de recuperación de contraseña
     */
    private void sendPasswordResetEmail(String email) {
        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Se ha enviado un correo para restablecer tu contraseña",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Error al enviar correo de recuperación",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        btnLogin.setEnabled(!show);
        if (btnBiometric.getVisibility() == View.VISIBLE) {
            btnBiometric.setEnabled(!show);
        }
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