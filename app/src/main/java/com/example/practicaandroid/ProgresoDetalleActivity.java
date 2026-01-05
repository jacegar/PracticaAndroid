package com.example.practicaandroid;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.adapter.ProgresoHistorialAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.example.practicaandroid.util.TextResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ProgresoDetalleActivity extends AppCompatActivity {

    private EjercicioDao ejercicioDao;
    private SesionEjercicioDao sesionEjercicioDao;
    private SesionDao sesionDao;

    private TextView tvNombreEjercicio, tvTipoEjercicio, tvDescripcionEjercicio;
    private TextView tvEstadisticas, tvNoData;
    private RecyclerView recyclerViewHistorial;
    private ProgresoHistorialAdapter adapter;

    private Ejercicio ejercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso_detalle);

        // Habilitar botón de volver
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar DAOs
        AppDatabase db = AppDatabase.getInstance(this);
        ejercicioDao = db.ejercicioDao();
        sesionEjercicioDao = db.sesionEjercicioDao();
        sesionDao = db.sesionDao();

        // Inicializar vistas
        tvNombreEjercicio = findViewById(R.id.tvNombreEjercicio);
        tvTipoEjercicio = findViewById(R.id.tvTipoEjercicio);
        tvDescripcionEjercicio = findViewById(R.id.tvDescripcionEjercicio);
        tvEstadisticas = findViewById(R.id.tvEstadisticas);
        tvNoData = findViewById(R.id.tvNoData);
        recyclerViewHistorial = findViewById(R.id.recyclerViewHistorial);

        // Obtener ID del ejercicio
        long ejercicioId = getIntent().getLongExtra("ejercicio_id", -1);
        if (ejercicioId == -1) {
            finish();
            return;
        }

        // Cargar datos
        cargarDatosEjercicio(ejercicioId);
    }

    private void cargarDatosEjercicio(long ejercicioId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ejercicio = ejercicioDao.getById(ejercicioId);
            if (ejercicio == null) {
                runOnUiThread(this::finish);
                return;
            }

            // Obtener historial
            List<SesionEjercicio> historialSE = sesionEjercicioDao.getHistorialEjercicio(ejercicioId);
            List<ProgresoHistorialAdapter.ItemHistorial> historial = new ArrayList<>();

            for (SesionEjercicio se : historialSE) {
                Sesion sesion = sesionDao.getSesionById(se.sesionId);
                if (sesion != null) {
                    historial.add(new ProgresoHistorialAdapter.ItemHistorial(se, sesion));
                }
            }

            // Calcular estadísticas
            String estadisticas = calcularEstadisticas(ejercicio, historialSE);

            runOnUiThread(() -> {
                // Mostrar datos del ejercicio
                tvNombreEjercicio.setText(TextResolver.resolveTextFromDB(this, ejercicio.nombre));
                tvTipoEjercicio.setText(getString(R.string.type_colon,
                        TextResolver.resolve(this, ejercicio.tipo)));

                if (ejercicio.descripcion != null && !ejercicio.descripcion.isEmpty()) {
                    tvDescripcionEjercicio.setText(TextResolver.resolveTextFromDB(this, ejercicio.descripcion));
                } else {
                    tvDescripcionEjercicio.setVisibility(View.GONE);
                }

                // Mostrar estadísticas
                tvEstadisticas.setText(estadisticas);

                // Configurar RecyclerView de historial
                if (historial.isEmpty()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    recyclerViewHistorial.setVisibility(View.GONE);
                } else {
                    tvNoData.setVisibility(View.GONE);
                    recyclerViewHistorial.setVisibility(View.VISIBLE);
                    recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
                    adapter = new ProgresoHistorialAdapter(this, ejercicio.tipo);
                    recyclerViewHistorial.setAdapter(adapter);
                    adapter.setHistorial(historial);
                }
            });
        });
    }

    private String calcularEstadisticas(Ejercicio ejercicio, List<SesionEjercicio> historial) {
        StringBuilder stats = new StringBuilder();

        stats.append(getString(R.string.times_performed_format, historial.size())).append("\n\n");

        if (historial.isEmpty()) {
            stats.append(getString(R.string.no_progress_data));
            return stats.toString();
        }

        if (ejercicio.tipo.equals("strength_type")) {
            // Estadísticas de fuerza
            float maxPeso = 0;
            float totalPeso = 0;
            int countPeso = 0;
            int totalSeries = 0;
            int totalRepeticiones = 0;

            for (SesionEjercicio se : historial) {
                if (se.peso > 0) {
                    if (se.peso > maxPeso) maxPeso = se.peso;
                    totalPeso += se.peso;
                    countPeso++;
                }
                totalSeries += se.series;
                totalRepeticiones += se.repeticiones;
            }

            if (maxPeso > 0) {
                stats.append(getString(R.string.max_weight_format, maxPeso)).append("\n");
                float promedioPeso = totalPeso / countPeso;
                stats.append(getString(R.string.avg_weight_format, promedioPeso)).append("\n");
            }

            if (totalSeries > 0) {
                stats.append(getString(R.string.total_sets_format, totalSeries)).append("\n");
            }

            if (totalRepeticiones > 0) {
                stats.append(getString(R.string.total_reps_format, totalRepeticiones)).append("\n");
            }

        } else if (ejercicio.tipo.equals("cardio_type")) {
            // Estadísticas de cardio
            float maxDistancia = 0;
            float totalDistancia = 0;
            int maxDuracion = 0;
            int totalDuracion = 0;

            for (SesionEjercicio se : historial) {
                if (se.distanciaKm > maxDistancia) maxDistancia = se.distanciaKm;
                totalDistancia += se.distanciaKm;

                if (se.duracionSegundos > maxDuracion) maxDuracion = se.duracionSegundos;
                totalDuracion += se.duracionSegundos;
            }

            if (maxDistancia > 0) {
                stats.append(getString(R.string.max_distance_format, maxDistancia)).append("\n");
                stats.append(getString(R.string.total_distance_format, totalDistancia)).append("\n");
            }

            if (maxDuracion > 0) {
                int minutos = maxDuracion / 60;
                stats.append(getString(R.string.max_duration_format, minutos)).append("\n");
                int totalMinutos = totalDuracion / 60;
                stats.append(getString(R.string.total_duration_format, totalMinutos)).append("\n");
            }

        } else {
            // Estadísticas generales para flexibilidad u otros
            int totalSeries = 0;
            int totalRepeticiones = 0;
            int totalDuracion = 0;

            for (SesionEjercicio se : historial) {
                totalSeries += se.series;
                totalRepeticiones += se.repeticiones;
                totalDuracion += se.duracionSegundos;
            }

            if (totalSeries > 0) {
                stats.append(getString(R.string.total_sets_format, totalSeries)).append("\n");
            }

            if (totalRepeticiones > 0) {
                stats.append(getString(R.string.total_reps_format, totalRepeticiones)).append("\n");
            }

            if (totalDuracion > 0) {
                int totalMinutos = totalDuracion / 60;
                stats.append(getString(R.string.total_duration_format, totalMinutos)).append("\n");
            }
        }

        return stats.toString().trim();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

