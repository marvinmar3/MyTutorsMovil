package com.mt.mytutors.activities;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.mt.mytutors.models.Materia;
import com.mt.mytutors.models.Tema;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CrearTemaActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup rgTipo;
    private RadioButton rbOferta, rbDemanda;
    private TextInputLayout tilNombre, tilDescripcion;
    private TextInputEditText etNombre, etDescripcion;
    private Spinner spMateria;
    private MaterialCardView cardInfo;
    private TextView tvInfo;
    private MaterialButton btnCrear;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Materia> materias = new ArrayList<>();
    private ArrayAdapter<Materia> materiaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tema);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        loadMaterias();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgTipo = findViewById(R.id.rgTipo);
        rbOferta = findViewById(R.id.rbOferta);
        rbDemanda = findViewById(R.id.rbDemanda);
        tilNombre = findViewById(R.id.tilNombre);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        spMateria = findViewById(R.id.spMateria);
        cardInfo = findViewById(R.id.cardInfo);
        tvInfo = findViewById(R.id.tvInfo);
        btnCrear = findViewById(R.id.btnCrear);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        materiaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materias);
        materiaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMateria.setAdapter(materiaAdapter);
    }

    private void loadMaterias() {
        db.collection("materias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materias.clear();
                    materias.add(new Materia("", "Selecciona una materia"));

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Materia materia = doc.toObject(Materia.class);
                        materia.setId(doc.getId());
                        materias.add(materia);
                    }
                    materiaAdapter.notifyDataSetChanged();
                });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOferta) {
                tvInfo.setText(getString(R.string.offer_info));
            } else {
                tvInfo.setText(getString(R.string.demand_info));
            }
        });

        btnCrear.setOnClickListener(v -> crearTema());
    }

    private void crearTema() {
        tilNombre.setError(null);
        tilDescripcion.setError(null);

        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String rol = rbOferta.isChecked() ? "tutor" : "tutorado";

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("Ingresa el nombre del tema");
            return;
        }
        if (TextUtils.isEmpty(descripcion)) {
            tilDescripcion.setError("Ingresa una descripción");
            return;
        }
        if (spMateria.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona una materia", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        Materia materiaSeleccionada = (Materia) spMateria.getSelectedItem();

        showLoading(true);

        // Crear el tema
        Tema tema = new Tema();
        tema.setNombre(nombre);
        tema.setDescripcion(descripcion);
        tema.setRol(rol);
        tema.setIdMateria(materiaSeleccionada.getId());
        tema.setIdCreador(currentUser.getUid());
        tema.setNombreMateria(materiaSeleccionada.getNombre());

        if ("tutor".equals(rol)) {
            tema.setIdTutor(currentUser.getUid());
        }

        // Obtener nombre del creador
        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreCreador = documentSnapshot.getString("nombre");
                        tema.setNombreCreador(nombreCreador);
                    }

                    // Guardar en Firestore
                    db.collection("temas")
                            .add(tema)
                            .addOnSuccessListener(documentReference -> {
                                showLoading(false);
                                Toast.makeText(this, getString(R.string.success_topic_created),
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Error al crear el tema", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCrear.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        btnCrear.setEnabled(!show);
    }
}
