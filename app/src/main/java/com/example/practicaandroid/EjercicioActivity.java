package com.example.practicaandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.EjercicioAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.ejercicio.TipoEjercicio;

import java.util.List;
import java.util.concurrent.Executors;

public class EjercicioActivity extends AppCompatActivity implements EjercicioAdapter.OnEjercicioClickListener {
    private EjercicioDao ejercicioDao;

    private EjercicioAdapter adapter;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio);

        // Inicializar base de datos
        ejercicioDao = AppDatabase.getInstance(this).ejercicioDao();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EjercicioAdapter(this);
        recyclerView.setAdapter(adapter);

        // Botón flotante para crear nuevo ejercicio
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarDialogoCrear());

        // Cargar ejercicios
        cargarEjercicios();
    }

    private void cargarEjercicios(){
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.getAll();
            runOnUiThread(() -> adapter.setEjercicios(ejercicios));
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ejercicio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        String[] tipos = new String[TipoEjercicio.values().length];
        for(int i = 0; i < TipoEjercicio.values().length; i++){
            tipos[i] = TipoEjercicio.values()[i].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tipos);
        spinnerTipo.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Ejercicio")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String tipo = spinnerTipo.getSelectedItem().toString();

                    if (nombre.isEmpty()){
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    crearEjercicio(nombre, descripcion, tipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearEjercicio(String nombre, String descripcion, String tipo){
        Executors.newSingleThreadExecutor().execute(() -> {
            Ejercicio ejercicio = new Ejercicio(nombre, descripcion, tipo);
            ejercicioDao.insert(ejercicio);

            runOnUiThread(() -> {
                Toast.makeText(this, "Ejercicio creado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }

    @Override
    public void onEditarClick(Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ejercicio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        // Configurar Spinner con los tipos
        String[] tipos = new String[TipoEjercicio.values().length];
        for(int i = 0; i < TipoEjercicio.values().length; i++){
            tipos[i] = TipoEjercicio.values()[i].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tipos);
        spinnerTipo.setAdapter(adapter);

        // Prellenar campos con datos actuales
        etNombre.setText(ejercicio.nombre);
        etDescripcion.setText(ejercicio.descripcion);

        // Seleccionar el tipo actual en el spinner
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i].equals(ejercicio.tipo)) {
                spinnerTipo.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Editar Ejercicio")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String tipo = spinnerTipo.getSelectedItem().toString();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarEjercicio(ejercicio, nombre, descripcion, tipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarEjercicio(Ejercicio ejercicio, String nombre, String descripcion, String tipo) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ejercicio.nombre = nombre;
            ejercicio.descripcion = descripcion;
            ejercicio.tipo = tipo;
            ejercicioDao.update(ejercicio);

            runOnUiThread(() -> {
                Toast.makeText(this, "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }

    @Override
    public void onEliminarClick(Ejercicio ejercicio) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Ejercicio")
                .setMessage("¿Estás seguro de eliminar '" + ejercicio.nombre + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEjercicio(ejercicio))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEjercicio(Ejercicio ejercicio) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ejercicioDao.delete(ejercicio);

            runOnUiThread(() -> {
                Toast.makeText(this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }
}
