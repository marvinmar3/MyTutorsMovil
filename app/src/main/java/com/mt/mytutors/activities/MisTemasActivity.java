package com.mt.mytutors.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mt.mytutors.R;

/**
 * MisTemasActivity - Muestra los temas creados por el usuario actual
 * Similar a HomeActivity pero filtrado por el usuario actual
 */
public class MisTemasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reutilizar layout de solicitudes o crear uno similar
        setContentView(R.layout.activity_solicitudes);
        // TODO: Implementar carga de temas del usuario
    }
}