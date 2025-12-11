package com.mt.mytutors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mt.mytutors.R;
import com.mt.mytutors.models.SolicitudTutoria;
import com.mt.mytutors.models.Tema;
import com.mt.mytutors.models.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleTemaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTipo, tvNombre, tvMateria, tvDescripcion;
    private TextView tvNombreCreador, tvCarreraCreador, tvRolCreador;
    private ImageButton btnChat;
    private MaterialButton btnSolicitar, btnReportar, btnEditar, btnEliminar;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Tema tema;
    private Usuario creador;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tema);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        // Obtener tema del intent
        tema = (Tema) getIntent().getSerializableExtra("tema");
        if (tema == null) {
            Toast.makeText(this, "Error al cargar el tema", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        displayTemaInfo();
        loadCreadorInfo();
        setupListeners();
        updateButtonsVisibility();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTipo = findViewById(R.id.tvTipo);
        tvNombre = findViewById(R.id.tvNombre);
        tvMateria = findViewById(R.id.tvMateria);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvNombreCreador = findViewById(R.id.tvNombreCreador);
        tvCarreraCreador = findViewById(R.id.tvCarreraCreador);
        tvRolCreador = findViewById(R.id.tvRolCreador);
        btnChat = findViewById(R.id.btnChat);
        btnSolicitar = findViewById(R.id.btnSolicitar);
        btnReportar = findViewById(R.id.btnReportar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayTemaInfo() {
        tvNombre.setText(tema.getNombre());
        tvDescripcion.setText(tema.getDescripcion());

        if (tema.getNombreMateria() != null) {
            tvMateria.setText(tema.getNombreMateria());
        }

        if ("tutor".equalsIgnoreCase(tema.getRol())) {
            tvTipo.setText("OFERTA");
            tvTipo.setBackgroundResource(R.drawable.badge_oferta);
            btnSolicitar.setText("Solicitar Tutoría");
        } else {
            tvTipo.setText("DEMANDA");
            tvTipo.setBackgroundResource(R.drawable.badge_demanda);
            btnSolicitar.setText("Ofrecer Tutoría");
        }
    }

    private void loadCreadorInfo() {
        if (tema.getIdCreador() == null) return;

        db.collection("usuarios").document(tema.getIdCreador())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        creador = documentSnapshot.toObject(Usuario.class);
                        if (creador != null) {
                            creador.setId(documentSnapshot.getId());
                            tvNombreCreador.setText(creador.getNombre());
                            tvRolCreador.setText(tema.esOferta() ? "Tutor" : "Tutorado");

                            // Cargar carrera
                            if (creador.getIdCarrera() != null) {
                                db.collection("carreras").document(creador.getIdCarrera())
                                        .get()
                                        .addOnSuccessListener(carreraDoc -> {
                                            if (carreraDoc.exists()) {
                                                String nombreCarrera = carreraDoc.getString("nombre");
                                                tvCarreraCreador.setText(nombreCarrera);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void setupListeners() {
        btnSolicitar.setOnClickListener(v -> enviarSolicitud());
        btnReportar.setOnClickListener(v -> reportarTema());
        btnEditar.setOnClickListener(v -> editarTema());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
        btnChat.setOnClickListener(v -> iniciarChat());
    }

    private void updateButtonsVisibility() {
        boolean esMiTema = currentUserId != null && currentUserId.equals(tema.getIdCreador());

        if (esMiTema) {
            btnSolicitar.setVisibility(View.GONE);
            btnChat.setVisibility(View.GONE);
            btnEditar.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            btnSolicitar.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
            btnEditar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        }
    }

    private void enviarSolicitud() {
        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Verificar si ya existe una solicitud
        db.collection("solicitudes")
                .whereEqualTo("idSolicitante", currentUserId)
                .whereEqualTo("idTema", tema.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(this, "Ya has enviado una solicitud para este tema",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Crear nueva solicitud
                    SolicitudTutoria solicitud = new SolicitudTutoria();
                    solicitud.setIdSolicitante(currentUserId);
                    solicitud.setIdTema(tema.getId());
                    solicitud.setAceptada(false);
                    solicitud.setRespondida(false);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    solicitud.setFechaSolicitud(sdf.format(new Date()));
                    solicitud.setNombreTema(tema.getNombre());

                    db.collection("solicitudes")
                            .add(solicitud)
                            .addOnSuccessListener(documentReference -> {
                                showLoading(false);
                                Toast.makeText(this, getString(R.string.success_request_sent),
                                        Toast.LENGTH_SHORT).show();
                                btnSolicitar.setEnabled(false);
                                btnSolicitar.setText("Solicitud enviada");
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Error al enviar solicitud", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void reportarTema() {
        // TODO: Implementar pantalla de reporte
        Toast.makeText(this, "Función de reporte próximamente", Toast.LENGTH_SHORT).show();
    }

    private void editarTema() {
        // TODO: Implementar edición
        Toast.makeText(this, "Función de edición próximamente", Toast.LENGTH_SHORT).show();
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tema")
                .setMessage("¿Estás seguro de que quieres eliminar este tema?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTema())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTema() {
        showLoading(true);

        db.collection("temas").document(tema.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Tema eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                });
    }

    private void iniciarChat() {
        if (creador == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otroUsuarioId", creador.getId());
        intent.putExtra("otroUsuarioNombre", creador.getNombre());
        intent.putExtra("temaId", tema.getId());
        intent.putExtra("temaNombre", tema.getNombre());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}