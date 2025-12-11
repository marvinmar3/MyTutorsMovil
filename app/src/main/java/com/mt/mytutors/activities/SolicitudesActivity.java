package com.mt.mytutors.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.adapters.SolicitudAdapter;
import com.mt.mytutors.models.SolicitudTutoria;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvSolicitudes;
    private LinearLayout llEmpty;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SolicitudAdapter solicitudAdapter;
    private List<SolicitudTutoria> solicitudesRecibidas = new ArrayList<>();
    private List<SolicitudTutoria> solicitudesEnviadas = new ArrayList<>();
    private String currentUserId;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadSolicitudes();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        rvSolicitudes = findViewById(R.id.rvSolicitudes);
        llEmpty = findViewById(R.id.llEmpty);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        solicitudAdapter = new SolicitudAdapter(new ArrayList<>(), currentUserId, true,
                new SolicitudAdapter.OnSolicitudActionListener() {
                    @Override
                    public void onAceptar(SolicitudTutoria solicitud) {
                        actualizarSolicitud(solicitud, true);
                    }

                    @Override
                    public void onRechazar(SolicitudTutoria solicitud) {
                        actualizarSolicitud(solicitud, false);
                    }
                });

        rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        rvSolicitudes.setAdapter(solicitudAdapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadSolicitudes() {
        showLoading(true);
        loadSolicitudesRecibidas();
        loadSolicitudesEnviadas();
    }

    private void loadSolicitudesRecibidas() {
        // Primero obtener mis temas
        db.collection("temas")
                .whereEqualTo("idCreador", currentUserId)
                .get()
                .addOnSuccessListener(temasSnapshot -> {
                    List<String> misTemaIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : temasSnapshot) {
                        misTemaIds.add(doc.getId());
                    }

                    if (misTemaIds.isEmpty()) {
                        showLoading(false);
                        updateList();
                        return;
                    }

                    // Cargar solicitudes de mis temas
                    db.collection("solicitudes")
                            .whereIn("idTema", misTemaIds)
                            .get()
                            .addOnSuccessListener(solicitudesSnapshot -> {
                                solicitudesRecibidas.clear();
                                for (QueryDocumentSnapshot doc : solicitudesSnapshot) {
                                    SolicitudTutoria solicitud = doc.toObject(SolicitudTutoria.class);
                                    solicitud.setId(doc.getId());
                                    solicitudesRecibidas.add(solicitud);
                                }
                                showLoading(false);
                                updateList();
                            });
                });
    }

    private void loadSolicitudesEnviadas() {
        db.collection("solicitudes")
                .whereEqualTo("idSolicitante", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    solicitudesEnviadas.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        SolicitudTutoria solicitud = doc.toObject(SolicitudTutoria.class);
                        solicitud.setId(doc.getId());
                        solicitudesEnviadas.add(solicitud);
                    }
                    updateList();
                });
    }

    private void actualizarSolicitud(SolicitudTutoria solicitud, boolean aceptar) {
        db.collection("solicitudes").document(solicitud.getId())
                .update("aceptada", aceptar, "respondida", true)
                .addOnSuccessListener(aVoid -> {
                    loadSolicitudes();
                });
    }

    private void updateList() {
        List<SolicitudTutoria> listToShow;
        boolean esRecibidas;

        if (currentTab == 0) {
            listToShow = solicitudesRecibidas;
            esRecibidas = true;
            tvEmpty.setText("No has recibido solicitudes");
        } else {
            listToShow = solicitudesEnviadas;
            esRecibidas = false;
            tvEmpty.setText("No has enviado solicitudes");
        }

        solicitudAdapter.updateData(listToShow, esRecibidas);

        llEmpty.setVisibility(listToShow.isEmpty() ? View.VISIBLE : View.GONE);
        rvSolicitudes.setVisibility(listToShow.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
