package com.example.practicaandroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.rutina.Rutina;
import com.example.practicaandroid.data.rutina.RutinaDao;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WorkoutFragment extends Fragment {

    private RutinaDao rutinaDao;
    private SesionDao sesionDao;
    private EjercicioDao ejercicioDao;

    private CardView cardRutinaActiva;
    private TextView tvNoHayRutinaActiva;
    private TextView tvNombreRutinaActiva, tvDescripcionRutinaActiva;
    private TextView tvNombreProximaSesion, tvEjerciciosProximaSesion;
    private Button btnCompletar, btnEditar, btnRutinas;

    private Rutina rutinaActivaActual;
    private Sesion proximaSesionActual;
    private long diaPlanificadoSeleccionado; // Para el diálogo de edición

    public WorkoutFragment() { /* Required empty public constructor */ }

    public static WorkoutFragment newInstance() {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context != null) {
            AppDatabase db = AppDatabase.getInstance(context);
            rutinaDao = db.rutinaDao();
            sesionDao = db.sesionDao();
            ejercicioDao = db.ejercicioDao();
        }
        inicializarVistas(view);
        configurarListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarInformacionRutinaActiva();
    }

    private void inicializarVistas(View view) {
        cardRutinaActiva = view.findViewById(R.id.cardRutinaActiva);
        tvNombreRutinaActiva = view.findViewById(R.id.tvNombreRutinaActiva);
        tvDescripcionRutinaActiva = view.findViewById(R.id.tvDescripcionRutinaActiva);
        tvNombreProximaSesion = view.findViewById(R.id.tvNombreProximaSesion);
        tvEjerciciosProximaSesion = view.findViewById(R.id.tvEjerciciosProximaSesion);
        btnCompletar = view.findViewById(R.id.btnCompletar);
        btnEditar = view.findViewById(R.id.btnEditar);
        tvNoHayRutinaActiva = view.findViewById(R.id.tvNoHayRutinaActiva);
        btnRutinas = view.findViewById(R.id.btnRutinas);
    }

    private void configurarListeners() {
        cardRutinaActiva.setOnClickListener(v -> {
            if (rutinaActivaActual != null) {
                Intent intent = new Intent(getActivity(), SesionActivity.class);
                intent.putExtra("rutinaId", rutinaActivaActual.id);
                intent.putExtra("rutinaNombre", rutinaActivaActual.nombre);
                startActivity(intent);
            }
        });

        btnRutinas.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RutinaActivity.class);
            startActivity(intent);
        });

        btnCompletar.setOnClickListener(v -> {
            if (proximaSesionActual != null) {
                final Context context = getContext();
                if (context == null) return;
                String parte1 = context.getString(R.string.want_complete_session1);
                String parte2 = context.getString(R.string.want_complete_session2);
                String mensaje = parte1 + proximaSesionActual.nombre + parte2;

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.complete_session)
                        .setMessage(mensaje)
                        .setPositiveButton(R.string.yes_complete, (dialog, which) -> marcarSesionComoCompletada(proximaSesionActual))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        btnEditar.setOnClickListener(v -> {
            if (proximaSesionActual != null) {
                mostrarDialogoEditarSesion(proximaSesionActual);
            }
        });
    }


    private void marcarSesionComoCompletada(Sesion sesion) {
        sesion.fechaRealizada = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            sesionDao.update(sesion);
            String workTag = "sesion-" + sesion.id;
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(workTag);
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::cargarInformacionRutinaActiva);
            }
        });
        Toast.makeText(getContext(), R.string.completed_session, Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoEditarSesion(Sesion sesion) {
        final Context context = getContext();
        if (context == null) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sesion, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextView tvDiaPlanificado = dialogView.findViewById(R.id.tvDiaPlanificado);

        etNombre.setText(sesion.nombre);
        diaPlanificadoSeleccionado = sesion.diaPlanificado;
        actualizarTextoDiaPlanificado(tvDiaPlanificado);

        tvDiaPlanificado.setOnClickListener(v -> mostrarSelectorFechaHora(tvDiaPlanificado));

        new AlertDialog.Builder(context)
                .setTitle(R.string.edit_session)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(context, R.string.name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    actualizarSesion(sesion, nombre, diaPlanificadoSeleccionado);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    private void actualizarSesion(Sesion sesion, String nombre, long diaPlanificado) {
        sesion.nombre = nombre;
        sesion.diaPlanificado = diaPlanificado;
        Executors.newSingleThreadExecutor().execute(() -> {
            sesionDao.update(sesion);
            // Recargamos la información para que los cambios se reflejen inmediatamente
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::cargarInformacionRutinaActiva);
            }
        });
        Toast.makeText(getContext(), R.string.updated_session, Toast.LENGTH_SHORT).show();
    }


    private void mostrarSelectorFechaHora(TextView tvDiaPlanificado) {
        final Context context = getContext();
        if (context == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(diaPlanificadoSeleccionado);

        new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(context, (timeView, hourOfDay, minute) -> {
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
        tv.setText(sdf.format(new Date(diaPlanificadoSeleccionado)));
    }


    private void cargarInformacionRutinaActiva() {
        Executors.newSingleThreadExecutor().execute(() -> {
            rutinaActivaActual = rutinaDao.getRutinaActiva();

            if (rutinaActivaActual == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::actualizarUIVacio);
                }
                return;
            }

            proximaSesionActual = sesionDao.getProximaSesionDeRutina(rutinaActivaActual.id);
            List<Ejercicio> ejercicios = null;
            if (proximaSesionActual != null) {
                ejercicios = ejercicioDao.getEjerciciosDeSesion(proximaSesionActual.id);
            }

            final List<Ejercicio> finalEjercicios = ejercicios;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> actualizarUIConDatos(rutinaActivaActual, proximaSesionActual, finalEjercicios));
            }
        });
    }

    private void actualizarUIVacio() {
        cardRutinaActiva.setVisibility(View.GONE);
        tvNoHayRutinaActiva.setVisibility(View.VISIBLE);
    }

    private void actualizarUIConDatos(Rutina rutina, Sesion sesion, List<Ejercicio> ejercicios) {
        tvNoHayRutinaActiva.setVisibility(View.GONE);
        cardRutinaActiva.setVisibility(View.VISIBLE);
        tvNombreRutinaActiva.setText(rutina.nombre);
        tvDescripcionRutinaActiva.setText(rutina.descripcion);

        this.rutinaActivaActual = rutina;
        this.proximaSesionActual = sesion;

        if (sesion != null) {
            String fechaStr = "";
            if (sesion.diaPlanificado > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                fechaStr = " (" + sdf.format(new Date(sesion.diaPlanificado)) + ")";
            }
            tvNombreProximaSesion.setText(sesion.nombre + fechaStr);

            btnCompletar.setVisibility(View.VISIBLE);
            btnEditar.setVisibility(View.VISIBLE);

            if (ejercicios != null && !ejercicios.isEmpty()) {
                StringBuilder ejerciciosBuilder = new StringBuilder();
                final Context context = getContext();
                if (context == null) return;
                String str_ejercicios = context.getString(R.string.exercises);

                ejerciciosBuilder.append(str_ejercicios + ":\n");
                for (Ejercicio ejercicio : ejercicios) {
                    ejerciciosBuilder.append("- ").append(ejercicio.nombre).append("\n");
                }
                if (ejerciciosBuilder.length() > 0) {
                    ejerciciosBuilder.setLength(ejerciciosBuilder.length() - 1);
                }
                tvEjerciciosProximaSesion.setText(ejerciciosBuilder.toString());
            } else {
                tvEjerciciosProximaSesion.setText(R.string.no_exercises_in_session);
            }
        } else {
            tvNombreProximaSesion.setText(R.string.congratulations_no_sessions);
            tvEjerciciosProximaSesion.setText("");
            btnCompletar.setVisibility(View.GONE);
            btnEditar.setVisibility(View.GONE);
        }
    }
}
