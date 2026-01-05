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
    private int currentWeekFilterPosition = 2; // Por defecto semana actual (índice 2)
    private boolean isUpdatingSpinner = false; // Bandera para evitar disparos durante actualización programática

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
                if (!isUpdatingSpinner) {
                    currentWeekFilterPosition = position; // Guardar la posición seleccionada
                    applyWeekFilter(position);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(this, this);
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
                // Aplicar el filtro actual en lugar de mostrar todas las sesiones
                applyWeekFilter(currentWeekFilterPosition);
            });
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sesion, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextView tvDiaPlanificado = dialogView.findViewById(R.id.tvDiaPlanificado);

        // Elementos de recurrencia
        android.widget.CheckBox cbRecurring = dialogView.findViewById(R.id.cbRecurring);
        android.widget.LinearLayout layoutRecurringOptions = dialogView.findViewById(R.id.layoutRecurringOptions);
        android.widget.CheckBox cbMonday = dialogView.findViewById(R.id.cbMonday);
        android.widget.CheckBox cbTuesday = dialogView.findViewById(R.id.cbTuesday);
        android.widget.CheckBox cbWednesday = dialogView.findViewById(R.id.cbWednesday);
        android.widget.CheckBox cbThursday = dialogView.findViewById(R.id.cbThursday);
        android.widget.CheckBox cbFriday = dialogView.findViewById(R.id.cbFriday);
        android.widget.CheckBox cbSaturday = dialogView.findViewById(R.id.cbSaturday);
        android.widget.CheckBox cbSunday = dialogView.findViewById(R.id.cbSunday);
        EditText etWeeksCount = dialogView.findViewById(R.id.etWeeksCount);

        // Mostrar/ocultar opciones de recurrencia
        cbRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRecurringOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

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

                    if (cbRecurring.isChecked()) {
                        // Crear sesiones recurrentes
                        java.util.List<Integer> selectedDays = new java.util.ArrayList<>();
                        if (cbMonday.isChecked()) selectedDays.add(Calendar.MONDAY);
                        if (cbTuesday.isChecked()) selectedDays.add(Calendar.TUESDAY);
                        if (cbWednesday.isChecked()) selectedDays.add(Calendar.WEDNESDAY);
                        if (cbThursday.isChecked()) selectedDays.add(Calendar.THURSDAY);
                        if (cbFriday.isChecked()) selectedDays.add(Calendar.FRIDAY);
                        if (cbSaturday.isChecked()) selectedDays.add(Calendar.SATURDAY);
                        if (cbSunday.isChecked()) selectedDays.add(Calendar.SUNDAY);

                        if (selectedDays.isEmpty()) {
                            Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String weeksStr = etWeeksCount.getText().toString().trim();
                        int weeksCount = 4;
                        try {
                            weeksCount = Integer.parseInt(weeksStr);
                            if (weeksCount <= 0) {
                                Toast.makeText(this, "El número de semanas debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Número de semanas inválido", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        crearSesionesRecurrentes(nombre, diaPlanificadoSeleccionado, selectedDays, weeksCount);
                    } else {
                        crearSesion(nombre, diaPlanificadoSeleccionado);
                    }
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

    private void crearSesionesRecurrentes(String nombre, long diaBase, java.util.List<Integer> selectedDays, int weeksCount) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Generar un ID único para este grupo de sesiones recurrentes
            String recurringGroupId = "recurring_" + System.currentTimeMillis();

            Calendar baseCal = Calendar.getInstance();
            baseCal.setTimeInMillis(diaBase);
            int baseHour = baseCal.get(Calendar.HOUR_OF_DAY);
            int baseMinute = baseCal.get(Calendar.MINUTE);

            // Obtener el inicio de la semana actual (lunes)
            Calendar weekStart = Calendar.getInstance();
            weekStart.setTimeInMillis(diaBase);
            weekStart.set(Calendar.HOUR_OF_DAY, 0);
            weekStart.set(Calendar.MINUTE, 0);
            weekStart.set(Calendar.SECOND, 0);
            weekStart.set(Calendar.MILLISECOND, 0);

            // Retroceder al lunes de la semana
            while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                weekStart.add(Calendar.DAY_OF_MONTH, -1);
            }

            int sessionCount = 0;

            // Crear sesiones para cada semana
            for (int week = 0; week < weeksCount; week++) {
                for (int dayOfWeek : selectedDays) {
                    Calendar sessionCal = (Calendar) weekStart.clone();

                    // Calcular el día de la semana
                    int daysToAdd = 0;
                    switch (dayOfWeek) {
                        case Calendar.MONDAY: daysToAdd = 0; break;
                        case Calendar.TUESDAY: daysToAdd = 1; break;
                        case Calendar.WEDNESDAY: daysToAdd = 2; break;
                        case Calendar.THURSDAY: daysToAdd = 3; break;
                        case Calendar.FRIDAY: daysToAdd = 4; break;
                        case Calendar.SATURDAY: daysToAdd = 5; break;
                        case Calendar.SUNDAY: daysToAdd = 6; break;
                    }

                    sessionCal.add(Calendar.WEEK_OF_YEAR, week);
                    sessionCal.add(Calendar.DAY_OF_MONTH, daysToAdd);
                    sessionCal.set(Calendar.HOUR_OF_DAY, baseHour);
                    sessionCal.set(Calendar.MINUTE, baseMinute);

                    // No crear sesiones en el pasado
                    if (sessionCal.getTimeInMillis() >= System.currentTimeMillis()) {
                        Sesion sesion = new Sesion(rutinaId, nombre, sessionCal.getTimeInMillis());
                        sesion.recurringGroupId = recurringGroupId;

                        long nuevoId = sesionDao.insert(sesion);
                        sesion.id = nuevoId;
                        crearNotificacion(sesion);
                        sessionCount++;
                    }
                }
            }

            final int totalCreated = sessionCount;
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.sessions_created, totalCreated), Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    private void crearNotificacion(Sesion sesion){
        if(sesion.fechaRealizada == 0) {
            long diaPlanificadoMillis = sesion.diaPlanificado;
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            long horas_antes = sharedPreferences.getInt("horas_antes_notificacion",24);
            long tiempoNotificacionMillis = diaPlanificadoMillis - (horas_antes * 60 * 60 * 1000);
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
        // Si es una sesión recurrente, preguntar si desea editar todas
        if (sesion.recurringGroupId != null && !sesion.recurringGroupId.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.edit_recurring_session)
                    .setMessage(R.string.edit_recurring_message)
                    .setPositiveButton(R.string.edit_all_recurring, (dialog, which) -> mostrarDialogoEditar(sesion, true))
                    .setNegativeButton(R.string.edit_only_this, (dialog, which) -> mostrarDialogoEditar(sesion, false))
                    .setNeutralButton(R.string.cancel, null)
                    .show();
        } else {
            mostrarDialogoEditar(sesion, false);
        }
    }

    private void mostrarDialogoEditar(Sesion sesion, boolean editarTodasRecurrentes) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sesion, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextView tvDiaPlanificado = dialogView.findViewById(R.id.tvDiaPlanificado);

        // Ocultar opciones de recurrencia en edición
        android.widget.CheckBox cbRecurring = dialogView.findViewById(R.id.cbRecurring);
        android.widget.LinearLayout layoutRecurringOptions = dialogView.findViewById(R.id.layoutRecurringOptions);
        cbRecurring.setVisibility(View.GONE);
        layoutRecurringOptions.setVisibility(View.GONE);

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

                    if (editarTodasRecurrentes) {
                        actualizarSesionesRecurrentes(sesion, nombre, diaPlanificadoSeleccionado);
                    } else {
                        actualizarSesion(sesion, nombre, diaPlanificadoSeleccionado);
                    }
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

    private void actualizarSesionesRecurrentes(Sesion sesion, String nombre, long diaPlanificado) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtener todas las sesiones del grupo
            List<Sesion> sesionesGrupo = sesionDao.getSesionesByRecurringGroup(sesion.recurringGroupId);

            // Calcular la diferencia de tiempo si se cambió la hora
            long diferenciaTiempo = diaPlanificado - sesion.diaPlanificado;

            // Actualizar todas las sesiones del grupo
            for (Sesion s : sesionesGrupo) {
                s.nombre = nombre;

                // Si se cambió la fecha/hora, aplicar la diferencia a todas las sesiones
                if (diferenciaTiempo != 0) {
                    s.diaPlanificado += diferenciaTiempo;
                }

                cancelarNotificacion(s);
                crearNotificacion(s);
                sesionDao.update(s);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.all_recurring_sessions_updated, Toast.LENGTH_SHORT).show();
                cargarSesiones();
            });
        });
    }

    @Override
    public void onEliminarClick(Sesion sesion) {
        // Si es una sesión recurrente, preguntar qué desea eliminar
        if (sesion.recurringGroupId != null && !sesion.recurringGroupId.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Sesión")
                    .setMessage(R.string.delete_recurring_message)
                    .setPositiveButton(R.string.delete_all_recurring, (dialog, which) -> eliminarSesionesRecurrentes(sesion.recurringGroupId))
                    .setNegativeButton(R.string.delete_only_this, (dialog, which) -> eliminarSesion(sesion))
                    .setNeutralButton(R.string.cancel, null)
                    .show();
        } else {
            // Sesión individual, eliminación normal
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Sesión")
                    .setMessage("¿Estás seguro de eliminar '" + sesion.nombre + "'?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarSesion(sesion))
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
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

    private void eliminarSesionesRecurrentes(String recurringGroupId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtener todas las sesiones del grupo para cancelar sus notificaciones
            List<Sesion> sesionesGrupo = sesionDao.getSesionesByRecurringGroup(recurringGroupId);
            for (Sesion sesion : sesionesGrupo) {
                cancelarNotificacion(sesion);
            }

            // Eliminar todas las sesiones del grupo
            sesionDao.deleteByRecurringGroup(recurringGroupId);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.recurring_sessions_deleted, Toast.LENGTH_SHORT).show();
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
            isUpdatingSpinner = true; // Activar bandera para evitar trigger del listener
            android.widget.ArrayAdapter<String> spinnerAdapter = (android.widget.ArrayAdapter<String>) spinnerWeekFilter.getAdapter();
            spinnerAdapter.clear();
            spinnerAdapter.addAll(labels);
            spinnerAdapter.notifyDataSetChanged();
            // Restaurar la posición previamente seleccionada (o semana actual si es la primera vez)
            spinnerWeekFilter.setSelection(currentWeekFilterPosition);
            isUpdatingSpinner = false; // Desactivar bandera
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

