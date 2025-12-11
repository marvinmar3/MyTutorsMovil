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

        // Buscar temas donde el creador sea el usuario actual
        db.collection("temas")
                .whereEqualTo("idCreador", currentUserId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    temasList.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Tema tema = document.toObject(Tema.class);
                        tema.setId(document.getId());
                        temasList.add(tema);
                    }

                    temaAdapter.notifyItemRangeInserted(0, temasList.size());
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al cargar temas: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
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