package com.mt.mytutors.activities;

import android.os.Bundle; // para manejar el ciclo de vida de la actividad
import android.text.TextUtils;
import android.view.View; // para manejar las vistas
import android.widget.LinearLayout;// para manejar diseños lineales
import android.widget.ProgressBar;
import android.widget.TextView; // para manejar los elementos de texto
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.adapters.MensajeAdapter;
import com.mt.mytutors.models.Conversacion;
import com.mt.mytutors.models.Mensaje;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombreUsuario, tvTema;
    private RecyclerView rvMensajes;
    private LinearLayout llEmpty;
    private TextInputEditText etMensaje;
    private FloatingActionButton fabEnviar;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> mensajes = new ArrayList<>();

    private String currentUserId;
    private String otroUsuarioId;
    private String otroUsuarioNombre;
    private String temaId;
    private String temaNombre;
    private String conversacionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        // Obtener datos del intent
        otroUsuarioId = getIntent().getStringExtra("otroUsuarioId");
        otroUsuarioNombre = getIntent().getStringExtra("otroUsuarioNombre");
        temaId = getIntent().getStringExtra("temaId");
        temaNombre = getIntent().getStringExtra("temaNombre");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Buscar o crear conversación
        findOrCreateConversation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        tvTema = findViewById(R.id.tvTema);
        rvMensajes = findViewById(R.id.rvMensajes);
        llEmpty = findViewById(R.id.llEmpty);
        etMensaje = findViewById(R.id.etMensaje);
        fabEnviar = findViewById(R.id.fabEnviar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
        tvNombreUsuario.setText(otroUsuarioNombre);
        tvTema.setText(temaNombre);
    }

    private void setupRecyclerView() {
        mensajeAdapter = new MensajeAdapter(mensajes, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMensajes.setLayoutManager(layoutManager);
        rvMensajes.setAdapter(mensajeAdapter);
    }

    private void setupListeners() {
        fabEnviar.setOnClickListener(v -> enviarMensaje());
    }

    private void findOrCreateConversation() {
        showLoading(true);

        // Buscar conversación existente
        db.collection("conversaciones")
                .whereArrayContains("participantes", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean encontrada = false;

                    for (var doc : querySnapshot) {
                        List<String> participantes = (List<String>) doc.get("participantes");
                        if (participantes != null &&
                                participantes.contains(otroUsuarioId) &&
                                participantes.size() == 2) {
                            conversacionId = doc.getId();
                            encontrada = true;
                            loadMensajes();
                            break;
                        }
                    }

                    if (!encontrada) {
                        crearConversacion();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void crearConversacion() {
        Map<String, Object> conversacion = new HashMap<>();
        conversacion.put("nombre", "Chat - " + temaNombre);
        conversacion.put("tipo", "privada");
        conversacion.put("idTema", temaId);
        conversacion.put("participantes", Arrays.asList(currentUserId, otroUsuarioId));
        conversacion.put("fechaUltimoMensaje", null);
        conversacion.put("ultimoMensaje", "");

        db.collection("conversaciones")
                .add(conversacion)
                .addOnSuccessListener(documentReference -> {
                    conversacionId = documentReference.getId();
                    showLoading(false);
                    updateEmptyState();

                    // Mostrar mensaje de ayuda
                    Toast.makeText(this, "Conversación creada. ¡Envía el primer mensaje!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al crear conversación: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMensajes() {
        if (conversacionId == null) {
            showLoading(false);
            updateEmptyState();
            return;
        }

        db.collection("mensajes")
                .whereEqualTo("idConversacion", conversacionId)
                .orderBy("fechaEnvio", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    showLoading(false);

                    if (error != null) {
                        Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                        return;
                    }

                    if (value == null) {
                        updateEmptyState();
                        return;
                    }

                    mensajes.clear();

                    for (var doc: value.getDocuments()){
                        Mensaje mensaje = doc.toObject(Mensaje.class);
                        if (mensaje != null) {
                            mensaje.setId(doc.getId());
                            mensajes.add(mensaje);
                        }
                    }

                    mensajeAdapter.notifyDataSetChanged();
                    if (!mensajes.isEmpty()) {
                        rvMensajes.scrollToPosition(mensajes.size() - 1);
                    }
                    updateEmptyState();
                });
    }
    private void enviarMensaje() {
        String contenido = etMensaje.getText() != null ?
                etMensaje.getText().toString().trim() : "";

        if (TextUtils.isEmpty(contenido)) return;
        if (conversacionId == null) return;

        Mensaje mensaje = new Mensaje();
        mensaje.setIdConversacion(conversacionId);
        mensaje.setIdEmisor(currentUserId);
        mensaje.setContenido(contenido);
        mensaje.setLeido(false);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        mensaje.setFechaEnvio(sdf.format(new Date()));

        etMensaje.setText("");

        db.collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar último mensaje en conversación
                    db.collection("conversaciones").document(conversacionId)
                            .update("ultimoMensaje", contenido,
                                    "fechaUltimoMensaje", mensaje.getFechaEnvio());
                });
    }

    private void updateEmptyState() {
        llEmpty.setVisibility(mensajes.isEmpty() ? View.VISIBLE : View.GONE);
        rvMensajes.setVisibility(mensajes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
