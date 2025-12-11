package com.mt.mytutors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.mt.mytutors.adapters.ConversacionAdapter;
import com.mt.mytutors.models.Conversacion;

import java.util.ArrayList;
import java.util.List;

/**
 * ConversacionesActivity - Lista de conversaciones del usuario
 */
public class ConversacionesActivity extends AppCompatActivity implements ConversacionAdapter.OnConversacionClickListener {

    private Toolbar toolbar;
    private RecyclerView rvConversaciones;
    private LinearLayout llEmpty;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    private ConversacionAdapter adapter;
    private List<Conversacion> conversaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversaciones);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadConversaciones();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvConversaciones = findViewById(R.id.rvConversaciones);
        llEmpty = findViewById(R.id.llEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Conversaciones");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ConversacionAdapter(conversaciones, currentUserId, this);
        rvConversaciones.setLayoutManager(new LinearLayoutManager(this));
        rvConversaciones.setAdapter(adapter);
    }

    private void loadConversaciones() {
        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Buscar conversaciones donde el usuario actual sea participante
        db.collection("conversaciones")
                .whereArrayContains("participantes", currentUserId)
                .orderBy("fechaUltimoMensaje", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    showLoading(false);

                    if (error != null) {
                        // Si hay error de índice, intentar sin ordenar
                        loadConversacionesSinOrden();
                        return;
                    }

                    if (value == null) {
                        updateEmptyState();
                        return;
                    }

                    conversaciones.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Conversacion conv = doc.toObject(Conversacion.class);
                        conv.setId(doc.getId());
                        conversaciones.add(conv);
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    // Método alternativo si no existe el índice compuesto
    private void loadConversacionesSinOrden() {
        db.collection("conversaciones")
                .whereArrayContains("participantes", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    conversaciones.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Conversacion conv = doc.toObject(Conversacion.class);
                        conv.setId(doc.getId());
                        conversaciones.add(conv);
                    }

                    // Ordenar localmente por fecha
                    conversaciones.sort((c1, c2) -> {
                        if (c1.getFechaUltimoMensaje() == null) return 1;
                        if (c2.getFechaUltimoMensaje() == null) return -1;
                        return c2.getFechaUltimoMensaje().compareTo(c1.getFechaUltimoMensaje());
                    });

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al cargar conversaciones", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (conversaciones.isEmpty()) {
            rvConversaciones.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvConversaciones.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onConversacionClick(Conversacion conversacion) {
        // Obtener el otro participante
        String otroUsuarioId = null;
        for (String participante : conversacion.getParticipantes()) {
            if (!participante.equals(currentUserId)) {
                otroUsuarioId = participante;
                break;
            }
        }

        if (otroUsuarioId != null) {
            // Obtener nombre del otro usuario
            final String finalOtroUsuarioId = otroUsuarioId;
            db.collection("usuarios").document(otroUsuarioId)
                    .get()
                    .addOnSuccessListener(document -> {
                        String nombre = document.getString("nombre");
                        if (nombre == null) nombre = "Usuario";

                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("conversacionId", conversacion.getId());
                        intent.putExtra("otroUsuarioId", finalOtroUsuarioId);
                        intent.putExtra("otroUsuarioNombre", nombre);
                        intent.putExtra("temaId", conversacion.getIdTema());
                        intent.putExtra("temaNombre", conversacion.getNombre());
                        startActivity(intent);
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar al volver
        if (currentUserId != null) {
            loadConversaciones();
        }
    }
}