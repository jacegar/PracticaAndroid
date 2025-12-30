package com.example.practicaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.RutinaAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.rutina.Rutina;
import com.example.practicaandroid.data.rutina.RutinaDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class RutinaActivity extends AppCompatActivity implements RutinaAdapter.OnRutinaClickListener {

    private RutinaDao rutinaDao;
    private RutinaAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutina);

        // Inicializar base de datos
        rutinaDao = AppDatabase.getInstance(this).rutinaDao();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RutinaAdapter(this);
        recyclerView.setAdapter(adapter);

        // Botón flotante para crear nueva rutina
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarDialogoCrear());

        // Cargar rutinas
        cargarRutinas();
    }

    private void cargarRutinas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Rutina> rutinas = rutinaDao.getAllRutinas();
            runOnUiThread(() -> adapter.setRutinas(rutinas));
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rutina, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);

        new AlertDialog.Builder(this)
                .setTitle("Nueva Rutina")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    crearRutina(nombre, descripcion);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearRutina(String nombre, String descripcion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Rutina rutina = new Rutina(nombre, descripcion, System.currentTimeMillis());
            rutinaDao.insert(rutina);

            runOnUiThread(() -> {
                Toast.makeText(this, "Rutina creada", Toast.LENGTH_SHORT).show();
                cargarRutinas();
            });
        });
    }

    @Override
    public void onEditarClick(Rutina rutina) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rutina, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);

        etNombre.setText(rutina.nombre);
        etDescripcion.setText(rutina.descripcion);

        new AlertDialog.Builder(this)
                .setTitle("Editar Rutina")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarRutina(rutina, nombre, descripcion);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarRutina(Rutina rutina, String nombre, String descripcion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            rutina.nombre = nombre;
            rutina.descripcion = descripcion;
            rutinaDao.update(rutina);

            runOnUiThread(() -> {
                Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
                cargarRutinas();
            });
        });
    }

    @Override
    public void onEliminarClick(Rutina rutina) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Rutina")
                .setMessage("¿Estás seguro de eliminar '" + rutina.nombre + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarRutina(rutina))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarRutina(Rutina rutina) {
        Executors.newSingleThreadExecutor().execute(() -> {
            rutinaDao.delete(rutina);

            runOnUiThread(() -> {
                Toast.makeText(this, "Rutina eliminada", Toast.LENGTH_SHORT).show();
                cargarRutinas();
            });
        });
    }

    @Override
    public void onVerSesionesClick(Rutina rutina) {
        Intent intent = new Intent(this, SesionActivity.class);
        intent.putExtra("rutinaId", rutina.id);
        intent.putExtra("rutinaNombre", rutina.nombre);
        startActivity(intent);
    }
}

