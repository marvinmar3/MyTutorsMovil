package com.mt.mytutors.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mt.mytutors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mt.mytutors.utils.BiometricHelper;

/**
 * MainActivity - Pantalla de Splash y verificación de sesión
 * Si el usuario ya está autenticado, va directo a Home
 * Si no, va a Login
 */
public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 segundos
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v, insets) -> {
            Insets systemBars= insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Navegar a LoginActivity después de 2 segundos
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, SPLASH_DELAY);
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent;
        if (currentUser != null) {
            // Usuario autenticado: verificar si se desea autenticación biométrica
            SharedPreferences prefs = getSharedPreferences("MyTutorsPrefs", MODE_PRIVATE);
            boolean biometricEnabled = prefs.getBoolean("biometric_enabled", false);

            if (biometricEnabled) {
                // Mostrar prompt biométrico primero; si tiene éxito, ir a Home, si no, ir a Login
                BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onSuccess() {
                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        // Si hay un error con la biometría, enviar al login para ingresar con contraseña
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailed() {
                        // En caso de fallo (huella no reconocida), ir al Login
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                return; // Ya iniciamos el flujo biométrico asíncrono
            } else {
                // No hay biometría configurada, ir directo a Home
                intent = new Intent(this, HomeActivity.class);
            }
        } else {
            // No hay sesión, ir a Login
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Cerrar esta activity para que no se pueda volver
    }


}