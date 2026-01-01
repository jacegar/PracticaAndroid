package com.example.practicaandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.rutina.Rutina;
import com.example.practicaandroid.data.rutina.RutinaDao;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WorkoutFragment extends Fragment {

    private RutinaDao rutinaDao;
    private SesionDao sesionDao;
    private EjercicioDao ejercicioDao;

    private CardView cardRutinaActiva;
    private TextView tvNoHayRutinaActiva;
    private TextView tvNombreRutinaActiva, tvDescripcionRutinaActiva;
    private TextView tvNombreProximaSesion, tvEjerciciosProximaSesion;
    private Button btnCompletar, btnEditar, btnRutinas;

    public WorkoutFragment() {
        // Required empty public constructor
    }

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
        // Inflate the layout for this fragment
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
        // Cargamos los datos en onResume para que la UI se actualice si cambiamos algo en otro lado
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
        // Botón para ir a gestionar rutinas
        btnRutinas.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RutinaActivity.class);
            startActivity(intent);
        });

        btnCompletar.setOnClickListener(v -> Toast.makeText(getContext(), "Funcionalidad 'Completar' pendiente", Toast.LENGTH_SHORT).show());
        btnEditar.setOnClickListener(v -> Toast.makeText(getContext(), "Funcionalidad 'Editar' pendiente", Toast.LENGTH_SHORT).show());
    }

    private void cargarInformacionRutinaActiva() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Rutina rutinaActiva = rutinaDao.getRutinaActiva();

            if (rutinaActiva == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::actualizarUIVacio);
                }
            }else {
                Sesion proximaSesion = sesionDao.getProximaSesionDeRutina(rutinaActiva.id);

                List<Ejercicio> ejercicios = null;
                if (proximaSesion != null) {
                    ejercicios = ejercicioDao.getEjerciciosDeSesion(proximaSesion.id);
                }

                final List<Ejercicio> finalEjercicios = ejercicios;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> actualizarUIConDatos(rutinaActiva, proximaSesion, finalEjercicios));
                }
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

        if (sesion != null) {
            String fechaStr = "";
            if (sesion.diaPlanificado > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                fechaStr = " (" + sdf.format(new Date(sesion.diaPlanificado)) + ")";
            }
            tvNombreProximaSesion.setText(sesion.nombre + fechaStr);

            if (ejercicios != null && !ejercicios.isEmpty()) {
                StringBuilder ejerciciosBuilder = new StringBuilder();
                ejerciciosBuilder.append("Ejercicios:\n");

                for (Ejercicio ejercicio : ejercicios) {
                    ejerciciosBuilder.append("- ").append(ejercicio.nombre).append("\n");
                }

                //eliminar ultimo \n
                if (ejerciciosBuilder.length() > 0) {
                    ejerciciosBuilder.setLength(ejerciciosBuilder.length() - 1);
                }

                tvEjerciciosProximaSesion.setText(ejerciciosBuilder.toString());

            } else {
                tvEjerciciosProximaSesion.setText("No hay ejercicios en esta sesión.");
            }
        }else {
            tvNombreProximaSesion.setText("No hay sesiones pendientes.");
            tvEjerciciosProximaSesion.setText("");
        }
    }
}
