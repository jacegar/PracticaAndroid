package com.example.practicaandroid;

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

import com.example.practicaandroid.adapter.SesionEjercicioAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SesionEjerciciosActivity extends AppCompatActivity implements SesionEjercicioAdapter.OnSesionEjercicioClickListener {

    private SesionEjercicioDao sesionEjercicioDao;
    private EjercicioDao ejercicioDao;
    private SesionEjercicioAdapter adapter;
    private long sesionId;
    private String sesionNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion_ejercicios);

        // Obtener ID de la sesión desde el Intent
        sesionId = getIntent().getLongExtra("sesionId", -1);
        sesionNombre = getIntent().getStringExtra("sesionNombre");

        if (sesionId == -1) {
            Toast.makeText(this, "Error: Sesión no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar base de datos
        sesionEjercicioDao = AppDatabase.getInstance(this).sesionEjercicioDao();
        ejercicioDao = AppDatabase.getInstance(this).ejercicioDao();

        // Configurar título
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Ejercicios: " + sesionNombre);

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionEjercicioAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Botón flotante para añadir ejercicio
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarDialogoSeleccionarEjercicio());

        // Cargar ejercicios de la sesión
        cargarEjerciciosSesion();
    }

    private void cargarEjerciciosSesion() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SesionEjercicio> sesionEjercicios = sesionEjercicioDao.getBySesion(sesionId);

            // Cargar los datos completos de cada ejercicio
            List<SesionEjercicioAdapter.EjercicioConDatos> ejerciciosConDatos = new ArrayList<>();
            for (SesionEjercicio se : sesionEjercicios) {
                Ejercicio ejercicio = ejercicioDao.getById(se.ejercicioId);
                if (ejercicio != null) {
                    ejerciciosConDatos.add(new SesionEjercicioAdapter.EjercicioConDatos(se, ejercicio));
                }
            }

            runOnUiThread(() -> adapter.setEjercicios(ejerciciosConDatos));
        });
    }

    private void mostrarDialogoSeleccionarEjercicio() {
        // Cargar todos los ejercicios disponibles
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ejercicio> todosEjercicios = ejercicioDao.getAll();

            if (todosEjercicios.isEmpty()) {
                runOnUiThread(() ->
                    Toast.makeText(this, "No hay ejercicios disponibles. Crea algunos primero.", Toast.LENGTH_LONG).show()
                );
                return;
            }

            // Crear array de nombres para el diálogo
            String[] nombresEjercicios = new String[todosEjercicios.size()];
            for (int i = 0; i < todosEjercicios.size(); i++) {
                nombresEjercicios[i] = todosEjercicios.get(i).nombre + " (" + todosEjercicios.get(i).tipo + ")";
            }

            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Seleccionar Ejercicio")
                        .setItems(nombresEjercicios, (dialog, which) -> {
                            Ejercicio ejercicioSeleccionado = todosEjercicios.get(which);
                            mostrarDialogoAñadirDatos(ejercicioSeleccionado);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        });
    }

    private void mostrarDialogoAñadirDatos(Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anadir_ejercicio_sesion, null);

        TextView tvNombre = dialogView.findViewById(R.id.tvNombreEjercicio);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoEjercicio);

        tvNombre.setText(ejercicio.nombre);
        tvTipo.setText("Tipo: " + ejercicio.tipo);

        // Campos según el tipo de ejercicio
        View layoutFuerza = dialogView.findViewById(R.id.layoutFuerza);
        View layoutCardio = dialogView.findViewById(R.id.layoutCardio);

        EditText etSeries = dialogView.findViewById(R.id.etSeries);
        EditText etRepeticiones = dialogView.findViewById(R.id.etRepeticiones);
        EditText etPeso = dialogView.findViewById(R.id.etPeso);
        EditText etDuracion = dialogView.findViewById(R.id.etDuracion);
        EditText etDistancia = dialogView.findViewById(R.id.etDistancia);

        // Mostrar campos según el tipo
        if ("FUERZA".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.VISIBLE);
            layoutCardio.setVisibility(View.GONE);
        } else if ("CARDIO".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.VISIBLE);
        } else {
            // FLEXIBILIDAD - sin campos específicos por ahora
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle("Añadir Ejercicio a la Sesión")
                .setView(dialogView)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    // Obtener siguiente orden
                    Executors.newSingleThreadExecutor().execute(() -> {
                        List<SesionEjercicio> existentes = sesionEjercicioDao.getBySesion(sesionId);
                        int siguienteOrden = existentes.size();

                        SesionEjercicio sesionEjercicio = new SesionEjercicio(sesionId, ejercicio.id, siguienteOrden);

                        // Asignar valores según el tipo
                        if ("FUERZA".equals(ejercicio.tipo)) {
                            try {
                                String seriesStr = etSeries.getText().toString().trim();
                                String repsStr = etRepeticiones.getText().toString().trim();
                                String pesoStr = etPeso.getText().toString().trim();

                                sesionEjercicio.series = seriesStr.isEmpty() ? 0 : Integer.parseInt(seriesStr);
                                sesionEjercicio.repeticiones = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
                                sesionEjercicio.peso = pesoStr.isEmpty() ? 0 : Float.parseFloat(pesoStr);
                            } catch (NumberFormatException e) {
                                runOnUiThread(() ->
                                    Toast.makeText(this, "Error en los valores numéricos", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }
                        } else if ("CARDIO".equals(ejercicio.tipo)) {
                            try {
                                String duracionStr = etDuracion.getText().toString().trim();
                                String distanciaStr = etDistancia.getText().toString().trim();

                                sesionEjercicio.duracionSegundos = duracionStr.isEmpty() ? 0 : Integer.parseInt(duracionStr) * 60; // convertir minutos a segundos
                                sesionEjercicio.distanciaKm = distanciaStr.isEmpty() ? 0 : Float.parseFloat(distanciaStr);
                            } catch (NumberFormatException e) {
                                runOnUiThread(() ->
                                    Toast.makeText(this, "Error en los valores numéricos", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }
                        }

                        sesionEjercicioDao.insert(sesionEjercicio);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Ejercicio añadido a la sesión", Toast.LENGTH_SHORT).show();
                            cargarEjerciciosSesion();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anadir_ejercicio_sesion, null);

        TextView tvNombre = dialogView.findViewById(R.id.tvNombreEjercicio);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoEjercicio);

        tvNombre.setText(ejercicio.nombre);
        tvTipo.setText("Tipo: " + ejercicio.tipo);

        // Campos según el tipo de ejercicio
        View layoutFuerza = dialogView.findViewById(R.id.layoutFuerza);
        View layoutCardio = dialogView.findViewById(R.id.layoutCardio);

        EditText etSeries = dialogView.findViewById(R.id.etSeries);
        EditText etRepeticiones = dialogView.findViewById(R.id.etRepeticiones);
        EditText etPeso = dialogView.findViewById(R.id.etPeso);
        EditText etDuracion = dialogView.findViewById(R.id.etDuracion);
        EditText etDistancia = dialogView.findViewById(R.id.etDistancia);

        // Mostrar campos según el tipo y prellenar
        if ("FUERZA".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.VISIBLE);
            layoutCardio.setVisibility(View.GONE);
            etSeries.setText(String.valueOf(sesionEjercicio.series));
            etRepeticiones.setText(String.valueOf(sesionEjercicio.repeticiones));
            etPeso.setText(String.valueOf(sesionEjercicio.peso));
        } else if ("CARDIO".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.VISIBLE);
            etDuracion.setText(String.valueOf(sesionEjercicio.duracionSegundos / 60)); // convertir segundos a minutos
            etDistancia.setText(String.valueOf(sesionEjercicio.distanciaKm));
        } else {
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle("Editar Datos del Ejercicio")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        // Actualizar valores según el tipo
                        if ("FUERZA".equals(ejercicio.tipo)) {
                            try {
                                String seriesStr = etSeries.getText().toString().trim();
                                String repsStr = etRepeticiones.getText().toString().trim();
                                String pesoStr = etPeso.getText().toString().trim();

                                sesionEjercicio.series = seriesStr.isEmpty() ? 0 : Integer.parseInt(seriesStr);
                                sesionEjercicio.repeticiones = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
                                sesionEjercicio.peso = pesoStr.isEmpty() ? 0 : Float.parseFloat(pesoStr);
                            } catch (NumberFormatException e) {
                                runOnUiThread(() ->
                                    Toast.makeText(this, "Error en los valores numéricos", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }
                        } else if ("CARDIO".equals(ejercicio.tipo)) {
                            try {
                                String duracionStr = etDuracion.getText().toString().trim();
                                String distanciaStr = etDistancia.getText().toString().trim();

                                sesionEjercicio.duracionSegundos = duracionStr.isEmpty() ? 0 : Integer.parseInt(duracionStr) * 60;
                                sesionEjercicio.distanciaKm = distanciaStr.isEmpty() ? 0 : Float.parseFloat(distanciaStr);
                            } catch (NumberFormatException e) {
                                runOnUiThread(() ->
                                    Toast.makeText(this, "Error en los valores numéricos", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }
                        }

                        sesionEjercicioDao.update(sesionEjercicio);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                            cargarEjerciciosSesion();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEliminarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Ejercicio")
                .setMessage("¿Quitar '" + ejercicio.nombre + "' de esta sesión?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        sesionEjercicioDao.delete(sesionEjercicio);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Ejercicio eliminado de la sesión", Toast.LENGTH_SHORT).show();
                            cargarEjerciciosSesion();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onMarcarCompletadoClick(SesionEjercicio sesionEjercicio) {
        sesionEjercicio.completado = !sesionEjercicio.completado;

        Executors.newSingleThreadExecutor().execute(() -> {
            sesionEjercicioDao.update(sesionEjercicio);

            runOnUiThread(() -> {
                String mensaje = sesionEjercicio.completado ? "Marcado como completado" : "Marcado como pendiente";
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                cargarEjerciciosSesion();
            });
        });
    }
}

