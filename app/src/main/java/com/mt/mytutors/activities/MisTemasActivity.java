package com.mt.mytutors.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mt.mytutors.R;
import com.mt.mytutors.adapters.TemaAdapter;
import com.mt.mytutors.models.Tema;

import java.util.ArrayList;
import java.util.List;

/**
 * MisTemasActivity - Muestra los temas creados por el usuario actual
 */
public class MisTemasActivity extends AppCompatActivity implements TemaAdapter.OnTemaClickListener {

    private Toolbar toolbar;
    private RecyclerView rvTemas;
    private ProgressBar progressBar;
    private View llEmpty;
    private TextView tvEmptyMessage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    private TemaAdapter temaAdapter;
    private List<Tema> temasList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_temas);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        rvTemas = findViewById(R.id.rvTemas);
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadMisTemas();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTemas = findViewById(R.id.rvTemas);
        progressBar = findViewById(R.id.progressBar);
        llEmpty = findViewById(R.id.llEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText("No has creado ningún tema todavía");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Temas");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        temaAdapter = new TemaAdapter(temasList, this);
        rvTemas.setLayoutManager(new LinearLayoutManager(this));
        rvTemas.setAdapter(temaAdapter);
    }

    private void loadMisTemas() {
        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Intentar primero CON ordenamiento (si existe el índice)
        db.collection("temas")
                .whereEqualTo("idCreador", currentUserId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Si falla por índice, cargar sin ordenar
                        android.util.Log.w("MisTemas", "Error con índice, cargando sin ordenar", error);
                        loadMisTemasSimple();
                        return;
                    }

                    showLoading(false);
                    procesarResultados(value);
                });
    }
    // Método alternativo sin ordenamiento (funciona siempre)
    private void loadMisTemasSimple() {
        db.collection("temas")
                .whereEqualTo("idCreador", currentUserId)
                .addSnapshotListener((value, error) -> {
                    showLoading(false);

                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                        return;
                    }

                    procesarResultados(value);
                });
    }

    // Método único para procesar resultados
    private void procesarResultados(com.google.firebase.firestore.QuerySnapshot value) {
        temasList.clear();

        if (value != null && !value.isEmpty()) {
            for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                Tema tema = document.toObject(Tema.class);
                tema.setId(document.getId());
                temasList.add(tema);

                // Log para debug
                android.util.Log.d("MisTemas", "Tema cargado: " + tema.getNombre() +
                        " - Rol: " + tema.getRol());
            }
        }

        temaAdapter.notifyDataSetChanged();
        updateEmptyState();

        // Mensaje informativo
        if (temasList.isEmpty()) {
            Toast.makeText(this, "No has creado ningún tema todavía",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void updateEmptyState() {
        if (temasList.isEmpty()) {
            rvTemas.setVisibility(View.GONE);
            if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvTemas.setVisibility(View.VISIBLE);
            if (llEmpty != null) llEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onTemaClick(Tema tema) {
        // ejemplo simple: mostrar Toast; reemplaza por la acción real
        String nombre = tema != null ? tema.getNombre() : "tema";
        Toast.makeText(this, "Seleccionado: " + nombre, Toast.LENGTH_SHORT).show();
    }

}