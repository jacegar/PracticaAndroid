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
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.EjercicioSelectorAdapter;
import com.example.practicaandroid.adapter.SesionEjercicioAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.example.practicaandroid.util.TextResolver;
import com.example.practicaandroid.util.TipoEjercicioSpinnerItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SesionEjerciciosActivity extends AppCompatActivity implements SesionEjercicioAdapter.OnSesionEjercicioClickListener {

    private SesionEjercicioDao sesionEjercicioDao;
    private EjercicioDao ejercicioDao;
    private SesionDao sesionDao;
    private SesionEjercicioAdapter adapter;
    private long sesionId;
    private String sesionNombre;
    private Sesion sesionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion_ejercicios);

        // Obtener ID de la sesión desde el Intent
        sesionId = getIntent().getLongExtra("sesionId", -1);
        sesionNombre = getIntent().getStringExtra("sesionNombre");

        if (sesionId == -1) {
            Toast.makeText(this, R.string.not_found_session, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar base de datos
        sesionEjercicioDao = AppDatabase.getInstance(this).sesionEjercicioDao();
        ejercicioDao = AppDatabase.getInstance(this).ejercicioDao();
        sesionDao = AppDatabase.getInstance(this).sesionDao();

        // Cargar información de la sesión
        Executors.newSingleThreadExecutor().execute(() -> {
            sesionActual = sesionDao.getSesionById(sesionId);
        });

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
        SearchView searchView = dialogView.findViewById(R.id.searchView);
        Spinner spinnerTipoFiltro = dialogView.findViewById(R.id.spinnerTipoFiltro);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewEjercicios);
        TextView tvNoResultados = dialogView.findViewById(R.id.tvNoResultados);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Cerrar el diálogo y mostrar el diálogo de añadir datos
        EjercicioSelectorAdapter selectorAdapter = new EjercicioSelectorAdapter(this, this::mostrarDialogoAñadirDatos);
        recyclerView.setAdapter(selectorAdapter);

        // Configurar Spinner de tipos
        List<TipoEjercicioSpinnerItem> listaTipos = new ArrayList<>();
        listaTipos.add(new TipoEjercicioSpinnerItem(
                getString(R.string.all_types),
                getString(R.string.all_types)
        ));
        listaTipos.add(new TipoEjercicioSpinnerItem(
                "strength_type",
                getString(R.string.strength_type)
        ));
        listaTipos.add(new TipoEjercicioSpinnerItem(
                "cardio_type",
                getString(R.string.cardio_type)
        ));
        listaTipos.add(new TipoEjercicioSpinnerItem(
                "flexibility_type",
                getString(R.string.flexibility_type)
        ));

        ArrayAdapter<TipoEjercicioSpinnerItem> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaTipos
        );

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
        final TipoEjercicioSpinnerItem[] tipoActual = {listaTipos.get(0)};

        Runnable aplicarFiltros = () -> {
            String claveTipoActual = tipoActual[0].getClaveDb();

            List<Ejercicio> resultados = todosEjercicios.stream()
                    .filter(ej -> {
                        boolean tipoCoincide = true;

                        // Filtro por tipo
                        if (!claveTipoActual.equals(getString(R.string.all_types)) &&
                                !ej.tipo.equals(claveTipoActual)) {
                            return false;
                        }

                        // Filtro por texto (nombre o descripción)
                        if (!textoActual[0].isEmpty()) {
                            String texto = textoActual[0].toLowerCase();
                            boolean matchNombre = TextResolver.resolveTextFromDB(this, ej.nombre).toLowerCase().contains(texto);
                            boolean matchDescripcion = ej.descripcion != null &&
                                    TextResolver.resolveTextFromDB(this, ej.descripcion).toLowerCase().contains(texto);
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
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(getResources().getColor(android.R.color.black, null));
            searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                tipoActual[0] = (TipoEjercicioSpinnerItem) parent.getItemAtPosition(position);
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

        tvNombre.setText(TextResolver.resolveTextFromDB(this,ejercicio.nombre));
        tvTipo.setText(getString(R.string.type_colon, TextResolver.resolve(this, ejercicio.tipo)));

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
        if ("strength_type".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.VISIBLE);
            layoutCardio.setVisibility(View.GONE);
        } else if ("cardio_type".equals(ejercicio.tipo)) {
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
                    // Verificar si es una sesión recurrente
                    if (sesionActual != null && sesionActual.recurringGroupId != null && !sesionActual.recurringGroupId.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.edit_recurring_session)
                                .setMessage(R.string.edit_recurring_message)
                                .setPositiveButton(R.string.edit_all_recurring, (d, w) ->
                                    añadirEjercicioConDatos(ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, true))
                                .setNegativeButton(R.string.edit_only_this, (d, w) ->
                                    añadirEjercicioConDatos(ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, false))
                                .setNeutralButton(R.string.cancel, null)
                                .show();
                    } else {
                        añadirEjercicioConDatos(ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, false);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void añadirEjercicioConDatos(Ejercicio ejercicio, EditText etSeries, EditText etRepeticiones,
                                         EditText etPeso, EditText etDuracion, EditText etDistancia,
                                         boolean aplicarATodasRecurrentes) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Crear el ejercicio base con los datos
                SesionEjercicio sesionEjercicioBase = crearSesionEjercicioConDatos(
                        sesionId, ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia);

                if (sesionEjercicioBase == null) {
                    return; // Error en los datos
                }

                if (aplicarATodasRecurrentes && sesionActual != null && sesionActual.recurringGroupId != null) {
                    // Obtener todas las sesiones del grupo
                    List<com.example.practicaandroid.data.sesion.Sesion> sesionesGrupo =
                            sesionDao.getSesionesByRecurringGroup(sesionActual.recurringGroupId);

                    // Añadir el ejercicio a cada sesión del grupo
                    for (com.example.practicaandroid.data.sesion.Sesion s : sesionesGrupo) {
                        List<SesionEjercicio> existentes = sesionEjercicioDao.getBySesion(s.id);
                        int siguienteOrden = existentes.size();

                        SesionEjercicio nuevoEjercicio = new SesionEjercicio(s.id, ejercicio.id, siguienteOrden);
                        copiarDatosSesionEjercicio(sesionEjercicioBase, nuevoEjercicio);
                        sesionEjercicioDao.insert(nuevoEjercicio);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.all_recurring_sessions_updated, Toast.LENGTH_SHORT).show();
                        cargarEjerciciosSesion();
                    });
                } else {
                    // Solo añadir a esta sesión
                    sesionEjercicioDao.insert(sesionEjercicioBase);

                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.exercise_added_to_session, Toast.LENGTH_SHORT).show();
                        cargarEjerciciosSesion();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private SesionEjercicio crearSesionEjercicioConDatos(long sesionId, Ejercicio ejercicio,
                                                         EditText etSeries, EditText etRepeticiones,
                                                         EditText etPeso, EditText etDuracion,
                                                         EditText etDistancia) {
        List<SesionEjercicio> existentes = sesionEjercicioDao.getBySesion(sesionId);
        int siguienteOrden = existentes.size();

        SesionEjercicio sesionEjercicio = new SesionEjercicio(sesionId, ejercicio.id, siguienteOrden);

        // Asignar valores según el tipo
        if ("strength_type".equals(ejercicio.tipo)) {
            try {
                String seriesStr = etSeries.getText().toString().trim();
                String repsStr = etRepeticiones.getText().toString().trim();
                String pesoStr = etPeso.getText().toString().trim();

                sesionEjercicio.series = seriesStr.isEmpty() ? 0 : Integer.parseInt(seriesStr);
                sesionEjercicio.repeticiones = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr) ;
                sesionEjercicio.peso = pesoStr.isEmpty() ? 0 : Float.parseFloat(pesoStr);
            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
                );
                return null;
            }
        } else if ("cardio_type".equals(ejercicio.tipo)) {
            try {
                String duracionStr = etDuracion.getText().toString().trim();
                String distanciaStr = etDistancia.getText().toString().trim();

                sesionEjercicio.duracionSegundos = duracionStr.isEmpty() ? 0 : Integer.parseInt(duracionStr) * 60;
                sesionEjercicio.distanciaKm = distanciaStr.isEmpty() ? 0 : Float.parseFloat(distanciaStr);
            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
                );
                return null;
            }
        }

        return sesionEjercicio;
    }

    private void copiarDatosSesionEjercicio(SesionEjercicio origen, SesionEjercicio destino) {
        destino.series = origen.series;
        destino.repeticiones = origen.repeticiones;
        destino.peso = origen.peso;
        destino.duracionSegundos = origen.duracionSegundos;
        destino.distanciaKm = origen.distanciaKm;
        destino.completado = origen.completado;
    }

    @Override
    public void onEditarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anadir_ejercicio_sesion, null);

        TextView tvNombre = dialogView.findViewById(R.id.tvNombreEjercicio);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoEjercicio);

        tvNombre.setText(TextResolver.resolveTextFromDB(this,ejercicio.nombre));
        tvTipo.setText(getString(R.string.exercise_type_label, TextResolver.resolve(this, ejercicio.tipo)));

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
        if ("strength_type".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.VISIBLE);
            layoutCardio.setVisibility(View.GONE);
            etSeries.setText(String.valueOf(sesionEjercicio.series));
            etRepeticiones.setText(String.valueOf(sesionEjercicio.repeticiones));
            etPeso.setText(String.valueOf(sesionEjercicio.peso));
        } else if ("cardio_type".equals(ejercicio.tipo)) {
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.VISIBLE);
            etDuracion.setText(String.valueOf(sesionEjercicio.duracionSegundos / 60)); // convertir segundos a minutos
            etDistancia.setText(String.valueOf(sesionEjercicio.distanciaKm));
        } else {
            layoutFuerza.setVisibility(View.GONE);
            layoutCardio.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_exercise_data)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    // Verificar si es una sesión recurrente
                    if (sesionActual != null && sesionActual.recurringGroupId != null && !sesionActual.recurringGroupId.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.edit_recurring_session)
                                .setMessage(R.string.edit_recurring_message)
                                .setPositiveButton(R.string.edit_all_recurring, (d, w) ->
                                    actualizarEjercicioConDatos(sesionEjercicio, ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, true))
                                .setNegativeButton(R.string.edit_only_this, (d, w) ->
                                    actualizarEjercicioConDatos(sesionEjercicio, ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, false))
                                .setNeutralButton(R.string.cancel, null)
                                .show();
                    } else {
                        actualizarEjercicioConDatos(sesionEjercicio, ejercicio, etSeries, etRepeticiones, etPeso, etDuracion, etDistancia, false);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void actualizarEjercicioConDatos(SesionEjercicio sesionEjercicio, Ejercicio ejercicio,
                                             EditText etSeries, EditText etRepeticiones, EditText etPeso,
                                             EditText etDuracion, EditText etDistancia, boolean aplicarATodasRecurrentes) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Actualizar valores según el tipo
                if ("strength_type".equals(ejercicio.tipo)) {
                    String seriesStr = etSeries.getText().toString().trim();
                    String repsStr = etRepeticiones.getText().toString().trim();
                    String pesoStr = etPeso.getText().toString().trim();

                    sesionEjercicio.series = seriesStr.isEmpty() ? 0 : Integer.parseInt(seriesStr);
                    sesionEjercicio.repeticiones = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
                    sesionEjercicio.peso = pesoStr.isEmpty() ? 0 : Float.parseFloat(pesoStr);
                } else if ("cardio_type".equals(ejercicio.tipo)) {
                    String duracionStr = etDuracion.getText().toString().trim();
                    String distanciaStr = etDistancia.getText().toString().trim();

                    sesionEjercicio.duracionSegundos = duracionStr.isEmpty() ? 0 : Integer.parseInt(duracionStr) * 60;
                    sesionEjercicio.distanciaKm = distanciaStr.isEmpty() ? 0 : Float.parseFloat(distanciaStr);
                }

                if (aplicarATodasRecurrentes && sesionActual != null && sesionActual.recurringGroupId != null) {
                    // Obtener todas las sesiones del grupo
                    List<Sesion> sesionesGrupo =
                            sesionDao.getSesionesByRecurringGroup(sesionActual.recurringGroupId);

                    // Actualizar el ejercicio en cada sesión del grupo
                    for (Sesion s : sesionesGrupo) {
                        // Buscar si existe este ejercicio en la sesión
                        List<SesionEjercicio> ejerciciosSesion = sesionEjercicioDao.getBySesion(s.id);
                        for (SesionEjercicio se : ejerciciosSesion) {
                            if (se.ejercicioId == ejercicio.id) {
                                // Actualizar con los nuevos valores
                                copiarDatosSesionEjercicio(sesionEjercicio, se);
                                sesionEjercicioDao.update(se);
                                break;
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.all_recurring_sessions_updated, Toast.LENGTH_SHORT).show();
                        cargarEjerciciosSesion();
                    });
                } else {
                    // Solo actualizar esta sesión
                    sesionEjercicioDao.update(sesionEjercicio);

                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.data_updated, Toast.LENGTH_SHORT).show();
                        cargarEjerciciosSesion();
                    });
                }
            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                    Toast.makeText(this, R.string.numeric_values_error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onEliminarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
        // Verificar si es una sesión recurrente
        if (sesionActual != null && sesionActual.recurringGroupId != null && !sesionActual.recurringGroupId.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_exercise)
                    .setMessage(getString(R.string.remove_exercise_from_session, TextResolver.resolveTextFromDB(this,ejercicio.nombre)))
                    .setPositiveButton(R.string.delete_all_recurring, (dialog, which) ->
                        eliminarEjercicioDeTodasRecurrentes(ejercicio))
                    .setNegativeButton(R.string.delete_only_this, (dialog, which) ->
                        eliminarEjercicio(sesionEjercicio))
                    .setNeutralButton(R.string.cancel, null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_exercise)
                    .setMessage(getString(R.string.remove_exercise_from_session, TextResolver.resolveTextFromDB(this, ejercicio.nombre)))
                    .setPositiveButton(R.string.delete, (dialog, which) -> eliminarEjercicio(sesionEjercicio))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void eliminarEjercicio(SesionEjercicio sesionEjercicio) {
        Executors.newSingleThreadExecutor().execute(() -> {
            sesionEjercicioDao.delete(sesionEjercicio);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.exercise_removed_from_session, Toast.LENGTH_SHORT).show();
                cargarEjerciciosSesion();
            });
        });
    }

    private void eliminarEjercicioDeTodasRecurrentes(Ejercicio ejercicio) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtener todas las sesiones del grupo
            List<Sesion> sesionesGrupo =
                    sesionDao.getSesionesByRecurringGroup(sesionActual.recurringGroupId);

            // Eliminar el ejercicio de cada sesión del grupo
            for (Sesion s : sesionesGrupo) {
                List<SesionEjercicio> ejerciciosSesion = sesionEjercicioDao.getBySesion(s.id);
                for (SesionEjercicio se : ejerciciosSesion) {
                    if (se.ejercicioId == ejercicio.id) {
                        sesionEjercicioDao.delete(se);
                        break;
                    }
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.all_recurring_sessions_updated, Toast.LENGTH_SHORT).show();
                cargarEjerciciosSesion();
            });
        });
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

