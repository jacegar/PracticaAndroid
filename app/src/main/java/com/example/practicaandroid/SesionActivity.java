package com.example.practicaandroid;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.SesionAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SesionActivity extends AppCompatActivity implements SesionAdapter.OnSesionClickListener {

    private SesionDao sesionDao;
    private SesionAdapter adapter;
    private RecyclerView recyclerView;
    private long rutinaId;
    private String rutinaNombre;
    private long diaPlanificadoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);

        // Obtener ID de la rutina desde el Intent
        rutinaId = getIntent().getLongExtra("rutinaId", -1);
        rutinaNombre = getIntent().getStringExtra("rutinaNombre");

        if (rutinaId == -1) {
            Toast.makeText(this, "Error: Rutina no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar base de datos
        sesionDao = AppDatabase.getInstance(this).sesionDao();

        // Configurar título
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Sesiones de: " + rutinaNombre);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(this);
        recyclerView.setAdapter(adapter);

        // Botón flotante para crear nueva sesión
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarDialogoCrear());

        // Cargar sesiones
        cargarSesiones();
    }

    private void cargarSesiones() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Sesion> sesiones = sesionDao.getSesionByRutina(rutinaId);
            runOnUiThread(() -> adapter.setSesiones(sesiones));
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sesion, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextView tvDiaPlanificado = dialogView.findViewById(R.id.tvDiaPlanificado);

        // Por defecto, establecer la fecha actual
        diaPlanificadoSeleccionado = System.currentTimeMillis();
        actualizarTextoDiaPlanificado(tvDiaPlanificado);

        // Configurar selector de fecha y hora
        tvDiaPlanificado.setOnClickListener(v -> mostrarSelectorFechaHora(tvDiaPlanificado));

        new AlertDialog.Builder(this)
                .setTitle("Nueva Sesión")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    crearSesion(nombre, diaPlanificadoSeleccionado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarSelectorFechaHora(TextView tvDiaPlanificado) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(diaPlanificadoSeleccionado);

        // Primero seleccionar fecha
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Luego seleccionar hora
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                diaPlanificadoSeleccionado = calendar.getTimeInMillis();
                actualizarTextoDiaPlanificado(tvDiaPlanificado);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarTextoDiaPlanificado(TextView tv) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm", Locale.getDefault());
        tv.setText(sdf.format(diaPlanificadoSeleccionado));
    }

    private void crearSesion(String nombre, long diaPlanificado) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Sesion sesion = new Sesion(rutinaId, nombre, diaPlanificado);
            sesionDao.insert(sesion);

            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión creada", Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    @Override
    public void onEditarClick(Sesion sesion) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sesion, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextView tvDiaPlanificado = dialogView.findViewById(R.id.tvDiaPlanificado);

        etNombre.setText(sesion.nombre);
        diaPlanificadoSeleccionado = sesion.diaPlanificado;
        actualizarTextoDiaPlanificado(tvDiaPlanificado);

        tvDiaPlanificado.setOnClickListener(v -> mostrarSelectorFechaHora(tvDiaPlanificado));

        new AlertDialog.Builder(this)
                .setTitle("Editar Sesión")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarSesion(sesion, nombre, diaPlanificadoSeleccionado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarSesion(Sesion sesion, String nombre, long diaPlanificado) {
        Executors.newSingleThreadExecutor().execute(() -> {
            sesion.nombre = nombre;
            sesion.diaPlanificado = diaPlanificado;
            sesionDao.update(sesion);

            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión actualizada", Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    @Override
    public void onEliminarClick(Sesion sesion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Sesión")
                .setMessage("¿Estás seguro de eliminar '" + sesion.nombre + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarSesion(sesion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarSesion(Sesion sesion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            sesionDao.delete(sesion);

            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión eliminada", Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    @Override
    public void onMarcarCompletadaClick(Sesion sesion) {
        String mensaje;
        if (sesion.fechaRealizada == 0) {
            // Marcar como completada
            sesion.fechaRealizada = System.currentTimeMillis();
            mensaje = "Sesión marcada como completada";
        } else {
            // Desmarcar como completada
            sesion.fechaRealizada = 0;
            mensaje = "Sesión marcada como pendiente";
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            sesionDao.update(sesion);

            runOnUiThread(() -> {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    @Override
    public void onVerEjerciciosClick(Sesion sesion) {
        Intent intent = new Intent(this, SesionEjerciciosActivity.class);
        intent.putExtra("sesionId", sesion.id);
        intent.putExtra("sesionNombre", sesion.nombre);
        startActivity(intent);
    }
}

