package com.example.practicaandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.EjercicioSelectorAdapter;
import com.example.practicaandroid.adapter.SesionEjercicioAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
        tvTitulo.setText(getString(R.string.exercises_colon, sesionNombre));

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
                    Toast.makeText(this, R.string.no_exercises_available, Toast.LENGTH_LONG).show()
                );
                return;
            }

            runOnUiThread(() -> mostrarDialogoConBuscador(todosEjercicios));
        });
    }

    private void mostrarDialogoConBuscador(List<Ejercicio> todosEjercicios) {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_seleccionar_ejercicio, null);

        // Referencias a las vistas
        androidx.appcompat.widget.SearchView searchView = dialogView.findViewById(R.id.searchView);
        Spinner spinnerTipoFiltro = dialogView.findViewById(R.id.spinnerTipoFiltro);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewEjercicios);
        TextView tvNoResultados = dialogView.findViewById(R.id.tvNoResultados);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        EjercicioSelectorAdapter selectorAdapter = new EjercicioSelectorAdapter(ejercicio -> {
            // Cerrar el diálogo y mostrar el diálogo de añadir datos
            mostrarDialogoAñadirDatos(ejercicio);
        });
        recyclerView.setAdapter(selectorAdapter);

        // Lista filtrada de ejercicios
        List<Ejercicio> ejerciciosFiltrados = new ArrayList<>(todosEjercicios);

        // Configurar spinner de tipos
        List<String> tipos = new ArrayList<>();
        tipos.add(getString(R.string.all_types));
        tipos.add("FUERZA");
        tipos.add("CARDIO");
        tipos.add("FLEXIBILIDAD");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tipos);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoFiltro.setAdapter(spinnerAdapter);

        // Crear el AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.select_exercise)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Función para aplicar filtros
        final String[] textoActual = {""};
        final String[] tipoActual = {getString(R.string.all_types)};

        Runnable aplicarFiltros = () -> {
            List<Ejercicio> resultados = todosEjercicios.stream()
                    .filter(ej -> {
                        // Filtro por tipo
                        if (!tipoActual[0].equals(getString(R.string.all_types)) &&
                            !ej.tipo.equals(tipoActual[0])) {
                            return false;
                        }

                        // Filtro por texto (nombre o descripción)
                        if (!textoActual[0].isEmpty()) {
                            String texto = textoActual[0].toLowerCase();
                            boolean matchNombre = ej.nombre.toLowerCase().contains(texto);
                            boolean matchDescripcion = ej.descripcion != null &&
                                    ej.descripcion.toLowerCase().contains(texto);
                            return matchNombre || matchDescripcion;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            selectorAdapter.setEjercicios(resultados);

            if (resultados.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvNoResultados.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvNoResultados.setVisibility(View.GONE);
            }
        };

        // Configurar SearchView
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_exercises));

        // Configurar color del texto a negro usando findViewById
        android.widget.EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(getResources().getColor(android.R.color.black, null));
            searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                textoActual[0] = newText;
                aplicarFiltros.run();
                return true;
            }
        });

        // Configurar Spinner
        spinnerTipoFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoActual[0] = tipos.get(position);
                aplicarFiltros.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Mostrar todos los ejercicios inicialmente
        selectorAdapter.setEjercicios(todosEjercicios);

        dialog.show();
    }

    private void mostrarDialogoAñadirDatos(Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anadir_ejercicio_sesion, null);

        TextView tvNombre = dialogView.findViewById(R.id.tvNombreEjercicio);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoEjercicio);

        tvNombre.setText(ejercicio.nombre);
        tvTipo.setText(getString(R.string.type_colon, ejercicio.tipo));

        // Campos según el tipo de ejercicio
        View layoutFuerza = dialogView.findViewById(R.id.layoutFuerza);
        View layoutCardio = dialogView.findViewById(R.id.layoutCardio);

        EditText etSeries = dialogView.findViewById(R.id.etSeries);
        EditText etRepeticiones = dialogView.findViewById(R.id.etRepeticiones);
        EditText etPeso = dialogView.findViewById(R.id.etPeso);
        EditText etDuracion = dialogView.findViewById(R.id.etDuracion);
        EditText etDistancia = dialogView.findViewById(R.id.etDistancia);

        // Obtener la unidad de peso configurada y establecer el hint
        TextInputLayout tilPeso = dialogView.findViewById(R.id.tilPeso);
        String weightUnit = SettingsFragment.getWeightUnit(this);
        tilPeso.setHint(getString(R.string.weight_hint, weightUnit));

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
                .setTitle(R.string.add_exercise_to_session)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
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
                                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
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
                .setNegativeButton(R.string.cancel, null)
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

        // Obtener la unidad de peso configurada y establecer el hint
        TextInputLayout tilPeso = dialogView.findViewById(R.id.tilPeso);
        String weightUnit = SettingsFragment.getWeightUnit(this);
        tilPeso.setHint(getString(R.string.weight_hint, weightUnit));

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
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onEliminarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_exercise)
                .setMessage(getString(R.string.remove_exercise_from_session, ejercicio.nombre))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        sesionEjercicioDao.delete(sesionEjercicio);

                        runOnUiThread(() -> {
                            Toast.makeText(this, R.string.exercise_removed_from_session, Toast.LENGTH_SHORT).show();
                            cargarEjerciciosSesion();
                        });
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onMarcarCompletadoClick(SesionEjercicio sesionEjercicio) {
        sesionEjercicio.completado = !sesionEjercicio.completado;

        Executors.newSingleThreadExecutor().execute(() -> {
            sesionEjercicioDao.update(sesionEjercicio);

            runOnUiThread(() -> {
                String mensaje = sesionEjercicio.completado ?
                        getString(R.string.marked_as_completed) :
                        getString(R.string.marked_as_pending);
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                cargarEjerciciosSesion();
            });
        });
    }
}

