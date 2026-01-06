package com.example.practicaandroid;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProgresoDetalleActivity extends AppCompatActivity {

    private EjercicioDao ejercicioDao;
    private SesionEjercicioDao sesionEjercicioDao;
    private SesionDao sesionDao;

    private TextView tvNombreEjercicio, tvTipoEjercicio, tvDescripcionEjercicio;
    private TextView tvEstadisticas, tvNoData;
    private RecyclerView recyclerViewHistorial;
    private ProgresoHistorialAdapter adapter;
    private LineChart lineChart;
    private MaterialCardView cardGrafica;

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
        lineChart = findViewById(R.id.lineChart);
        cardGrafica = findViewById(R.id.cardGrafica);

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

                // Configurar gráfica en UI thread
                configurarGraficaEnUIThread(historial, historialSE);
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
                totalRepeticiones += se.repeticiones * se.series;
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

    private void configurarGraficaEnUIThread(List<ProgresoHistorialAdapter.ItemHistorial> historial,
                                            List<SesionEjercicio> historialSE) {
        if (historial.isEmpty() || historial.size() < 2) {
            cardGrafica.setVisibility(View.GONE);
            return;
        }

        cardGrafica.setVisibility(View.VISIBLE);

        List<Entry> entries = new ArrayList<>();
        List<String> fechasFormateadas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Crear entradas según el tipo de ejercicio
        // Procesar del más antiguo al más reciente
        for (int i = historial.size() - 1; i >= 0; i--) {
            ProgresoHistorialAdapter.ItemHistorial item = historial.get(i);
            SesionEjercicio se = item.sesionEjercicio;
            Sesion sesion = item.sesion;

            float valor = 0;
            boolean agregarPunto = false;

            if (ejercicio.tipo.equals("strength_type")) {
                // Gráfica de peso
                if (se.peso > 0) {
                    valor = se.peso;
                    agregarPunto = true;
                }
            } else if (ejercicio.tipo.equals("cardio_type")) {
                // Gráfica de cardio: mostrar velocidad media en km/min (distancia / duración)
                if (se.distanciaKm > 0 && se.duracionSegundos > 0) {
                    float minutos = se.duracionSegundos / 60f;
                    if (minutos > 0f) {
                        valor = se.distanciaKm / minutos; // km por minuto
                        agregarPunto = true;
                    }
                }
                // Si falta distancia o duración, no añadimos punto para evitar valores engañosos
            } else {
                // Para flexibilidad, mostrar repeticiones
                if (se.repeticiones > 0) {
                    valor = se.repeticiones;
                    agregarPunto = true;
                } else if (se.series > 0) {
                    valor = se.series;
                    agregarPunto = true;
                }
            }

            if (agregarPunto) {
                entries.add(new Entry(entries.size(), valor));
                fechasFormateadas.add(sdf.format(new Date(sesion.fechaRealizada)));
            }
        }

        if (entries.isEmpty() || entries.size() < 2) {
            cardGrafica.setVisibility(View.GONE);
            return;
        }

        // Configurar el dataset
        LineDataSet dataSet = new LineDataSet(entries, getEtiquetaGrafica());

        // Colores
        int colorPrimario = getResources().getColor(com.google.android.material.R.color.design_default_color_primary, null);
        dataSet.setColor(colorPrimario);
        dataSet.setCircleColor(colorPrimario);
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colorPrimario);
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configurar apariencia de la gráfica
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Configurar eje X (fechas)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        final List<String> fechas = fechasFormateadas;
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < fechas.size()) {
                    return fechas.get(index);
                }
                return "";
            }
        });

        // Configurar eje Y
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configurar leyenda
        lineChart.getLegend().setEnabled(true);

        // Animar la gráfica
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private String getEtiquetaGrafica() {
        if (ejercicio.tipo.equals("strength_type")) {
            return getString(R.string.weight_format, 0f).replace("0.0", "").trim();
        } else if (ejercicio.tipo.equals("cardio_type")) {
            // Determinar si es distancia o duración basándose en el historial ya cargado
            return "km/min";
        } else {
            return getString(R.string.reps);
        }
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

