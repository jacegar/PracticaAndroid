package com.example.practicaandroid;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import com.example.practicaandroid.adapter.SesionAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.example.practicaandroid.notifications.SessionReminderWorker;
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

    private android.widget.Spinner spinnerWeekFilter;
    private java.util.List<Sesion> allSesiones = new java.util.ArrayList<>();
    private java.util.List<WeekItem> weekItems = new java.util.ArrayList<>();

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

        // Configurar RecyclerView y filtro de semana
        spinnerWeekFilter = findViewById(R.id.spinnerWeekFilter);
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new java.util.ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeekFilter.setAdapter(spinnerAdapter);
        spinnerWeekFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyWeekFilter(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

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
            allSesiones = sesiones;
            runOnUiThread(() -> {
                buildWeekItemsAndPopulateSpinner(sesiones);
                adapter.setSesiones(sesiones);
            });
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
                .setNegativeButton(R.string.cancel, null)
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

            //Creamos la notificacion con el id actualizado para poder borrarla luego si es necesario
            long nuevoId = sesionDao.insert(sesion);
            sesion.id = nuevoId;
            crearNotificacion(sesion);

            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión creada", Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    private void crearNotificacion(Sesion sesion){
        if(sesion.fechaRealizada == 0) {
            long diaPlanificadoMillis = sesion.diaPlanificado;
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            long minutos_antes = sharedPreferences.getInt("minutos_antes_notificacion", 60 * 24);
            long tiempoNotificacionMillis = diaPlanificadoMillis - (minutos_antes * 60 * 1000);
            long retrasoInicial = tiempoNotificacionMillis - System.currentTimeMillis();

            // Si la sesion es en el futuro, programamos la notificacion
            if (retrasoInicial > 0) {
                String workTag = "sesion-" + sesion.id;

                Data inputData = new Data.Builder()
                        .putLong("id", sesion.id)
                        .putString("nombre", sesion.nombre)
                        .putLong("fecha", sesion.diaPlanificado)
                        .build();

                OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(SessionReminderWorker.class)
                        .setInitialDelay(retrasoInicial, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag(workTag)
                        .build();

                WorkManager.getInstance(getApplicationContext()).enqueue(reminderRequest);
            }
        }
    }

    private void cancelarNotificacion(Sesion sesion) {
        String workTag = "sesion-" + sesion.id;
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(workTag);
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

            cancelarNotificacion(sesion);
            crearNotificacion(sesion);

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
            cancelarNotificacion(sesion);
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
            cancelarNotificacion(sesion);
            sesion.fechaRealizada = System.currentTimeMillis();
            mensaje = "Sesión marcada como completada";
        } else {
            // Desmarcar como completada
            crearNotificacion(sesion);
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

    // Construir lista de semanas disponibles y poblar el spinner
    private void buildWeekItemsAndPopulateSpinner(java.util.List<Sesion> sesiones) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        // Force first day of week to Monday
        int firstDay = java.util.Calendar.MONDAY;

        // Calcular la semana actual
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != firstDay) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        }

        // Retroceder a la semana anterior
        cal.add(java.util.Calendar.WEEK_OF_YEAR, -1);

        // Etiquetas descriptivas para cada semana
        String[] weekLabels = {
                getString(R.string.week_previous),
                getString(R.string.week_current),
                getString(R.string.week_next),
                getString(R.string.week_next_2)
        };

        // Generar semana anterior, actual y 2 semanas futuras (total 4 semanas)
        weekItems.clear();
        for (int i = 0; i < 4; i++) {
            long weekStart = cal.getTimeInMillis();
            long weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000 - 1;
            String dateRange = formatLabel(weekStart, weekEnd);
            String label = weekLabels[i] + " (" + dateRange + ")";
            weekItems.add(new WeekItem(weekStart, weekEnd, label));

            // Avanzar a la siguiente semana
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }

        final java.util.List<String> labels = new java.util.ArrayList<>();
        labels.add(getString(R.string.all_weeks));
        for (WeekItem wi : weekItems) {
            labels.add(wi.label);
        }

        // Actualizar spinner en UI thread
        runOnUiThread(() -> {
            android.widget.ArrayAdapter<String> spinnerAdapter = (android.widget.ArrayAdapter<String>) spinnerWeekFilter.getAdapter();
            spinnerAdapter.clear();
            spinnerAdapter.addAll(labels);
            spinnerAdapter.notifyDataSetChanged();
            // Auto-select current week (segunda semana en la lista después de "All weeks" e índice 0 de anterior)
            spinnerWeekFilter.setSelection(2);
        });
    }

    private void applyWeekFilter(int spinnerPosition) {
        if (spinnerPosition == 0) {
            adapter.setSesiones(allSesiones);
            return;
        }

        int idx = spinnerPosition - 1;
        if (idx < 0 || idx >= weekItems.size()) return;

        WeekItem w = weekItems.get(idx);
        java.util.List<Sesion> filtered = new java.util.ArrayList<>();
        for (Sesion s : allSesiones) {
            if (s.diaPlanificado >= w.start && s.diaPlanificado <= w.end) {
                filtered.add(s);
            }
        }
        adapter.setSesiones(filtered);
    }

    private String formatLabel(long start, long end) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(start)) + " - " + sdf.format(new java.util.Date(end));
    }

    private static class WeekItem {
        long start;
        long end;
        String label;

        WeekItem(long start, long end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }
    }
}

