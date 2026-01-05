package com.example.practicaandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.practicaandroid.adapter.EjercicioProgressAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ProgressFragment extends Fragment implements EjercicioProgressAdapter.OnEjercicioProgressClickListener {

    private EjercicioDao ejercicioDao;
    private SesionEjercicioDao sesionEjercicioDao;
    private EjercicioProgressAdapter adapter;
    private RecyclerView recyclerView;

    public ProgressFragment() {
        // Required empty public constructor
    }

    public static ProgressFragment newInstance() {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar DAOs
        AppDatabase db = AppDatabase.getInstance(requireContext());
        ejercicioDao = db.ejercicioDao();
        sesionEjercicioDao = db.sesionEjercicioDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewProgress);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EjercicioProgressAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Configurar SearchView
        SearchView searchView = view.findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint(getString(R.string.search_exercises_progress));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.filtrar(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filtrar(newText);
                    return true;
                }
            });
        }

        // Cargar ejercicios con progreso
        cargarEjerciciosConProgreso();
    }

    private void cargarEjerciciosConProgreso() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.getAll();
            List<EjercicioProgressAdapter.EjercicioConProgreso> listaConProgreso = new ArrayList<>();

            for (Ejercicio ejercicio : ejercicios) {
                // Obtener historial del ejercicio
                List<SesionEjercicio> historial = sesionEjercicioDao.getHistorialEjercicio(ejercicio.id);

                // Solo mostrar ejercicios que se hayan realizado al menos una vez
                if (!historial.isEmpty()) {
                    int vecesRealizado = historial.size();

                    listaConProgreso.add(new EjercicioProgressAdapter.EjercicioConProgreso(
                            ejercicio, vecesRealizado, "", ""
                    ));
                }
            }

            requireActivity().runOnUiThread(() -> adapter.setEjercicios(listaConProgreso));
        });
    }

    @Override
    public void onEjercicioClick(Ejercicio ejercicio) {
        // Abrir activity de detalle de progreso
        Intent intent = new Intent(requireContext(), ProgresoDetalleActivity.class);
        intent.putExtra("ejercicio_id", ejercicio.id);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos al volver a la pantalla
        cargarEjerciciosConProgreso();
    }
}


