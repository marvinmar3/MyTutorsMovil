package com.mt.mytutors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.adapters.TemaAdapter;
import com.mt.mytutors.models.Tema;
import com.mt.mytutors.models.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TextInputEditText etBuscar;
    private RecyclerView rvTemas;
    private LinearLayout llEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabCrearTema;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TemaAdapter temaAdapter;
    private List<Tema> temasList = new ArrayList<>();
    private List<Tema> temasFilteredList = new ArrayList<>();
    private Usuario currentUser;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadCurrentUser();
        loadTemas();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etBuscar = findViewById(R.id.etBuscar);
        rvTemas = findViewById(R.id.rvTemas);
        llEmpty = findViewById(R.id.llEmpty);
        progressBar = findViewById(R.id.progressBar);
        fabCrearTema = findViewById(R.id.fabCrearTema);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        temaAdapter = new TemaAdapter(temasFilteredList, tema -> {
            Intent intent = new Intent(this, DetalleTemaActivity.class);
            intent.putExtra("tema", tema);
            startActivity(intent);
        });
        rvTemas.setLayoutManager(new LinearLayoutManager(this));
        rvTemas.setAdapter(temaAdapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filterTemas();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTemas();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabCrearTema.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearTemaActivity.class);
            startActivity(intent);
        });
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            goToLogin();
            return;
        }

        db.collection("usuarios").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(Usuario.class);
                        if (currentUser != null) {
                            currentUser.setId(documentSnapshot.getId());
                            updateNavigationHeader();
                        }
                    }
                });
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.tvNombreUsuario);
        TextView tvCorreo = headerView.findViewById(R.id.tvCorreoUsuario);
        if (currentUser != null) {
            tvNombre.setText(currentUser.getNombre());
            tvCorreo.setText(currentUser.getCorreo());
        }
    }

    private void loadTemas() {
        showLoading(true);
        db.collection("temas")
                .addSnapshotListener((value, error) -> {
                    showLoading(false);
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar temas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    temasList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Tema tema = doc.toObject(Tema.class);
                            tema.setId(doc.getId());
                            temasList.add(tema);
                        }
                    }
                    filterTemas();
                });
    }

    private void filterTemas() {
        String searchText = etBuscar.getText() != null ?
                etBuscar.getText().toString().toLowerCase().trim() : "";

        temasFilteredList.clear();

        for (Tema tema : temasList) {
            boolean passesTabFilter = false;
            switch (currentTab) {
                case 0: passesTabFilter = true; break;
                case 1: passesTabFilter = "tutor".equalsIgnoreCase(tema.getRol()); break;
                case 2: passesTabFilter = "tutorado".equalsIgnoreCase(tema.getRol()); break;
            }

            boolean passesSearchFilter = searchText.isEmpty() ||
                    (tema.getNombre() != null && tema.getNombre().toLowerCase().contains(searchText)) ||
                    (tema.getDescripcion() != null && tema.getDescripcion().toLowerCase().contains(searchText));

            if (passesTabFilter && passesSearchFilter) {
                temasFilteredList.add(tema);
            }
        }
        temaAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        llEmpty.setVisibility(temasFilteredList.isEmpty() ? View.VISIBLE : View.GONE);
        rvTemas.setVisibility(temasFilteredList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Ya estamos aquí
        } else if (id == R.id.nav_my_topics) {
            startActivity(new Intent(this, MisTemasActivity.class));
        } else if (id == R.id.nav_requests) {
            startActivity(new Intent(this, SolicitudesActivity.class));
        } else if (id == R.id.nav_messages) {
            startActivity(new Intent(this, ConversacionesActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, PerfilActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.signOut();
                    goToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemas();
    }
}
