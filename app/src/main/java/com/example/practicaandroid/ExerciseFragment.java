package com.example.practicaandroid;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.practicaandroid.adapter.EjercicioAdapter;
import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.util.TextResolver;
import com.example.practicaandroid.util.TipoEjercicioSpinnerItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExerciseFragment extends Fragment implements EjercicioAdapter.OnEjercicioClickListener {

    private EjercicioDao ejercicioDao;
    private EjercicioAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    public ExerciseFragment() {
        // Required empty public constructor
    }

    public static ExerciseFragment newInstance() {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar base de datos
        ejercicioDao = AppDatabase.getInstance(requireContext()).ejercicioDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EjercicioAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Botón flotante para crear nuevo ejercicio
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarDialogoCrear());

        // Configurar SearchView
        SearchView searchView = view.findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Buscar ejercicios...");
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

        // Cargar ejercicios
        cargarEjercicios();
    }

    private void cargarEjercicios() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.getAll();
            requireActivity().runOnUiThread(() -> adapter.setEjercicios(ejercicios));
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ejercicio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        // Configurar Spinner con los tipos
        List<TipoEjercicioSpinnerItem> listaTipos = new ArrayList<>();
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
                requireContext(),
                android.R.layout.simple_spinner_item,
                listaTipos
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Ejercicio")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    TipoEjercicioSpinnerItem itemSeleccionado = (TipoEjercicioSpinnerItem) spinnerTipo.getSelectedItem();
                    String tipo = itemSeleccionado.getClaveDb();

                    if (nombre.isEmpty()) {
                        Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    crearEjercicio(nombre, descripcion, tipo);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void crearEjercicio(String nombre, String descripcion, String tipo) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Ejercicio ejercicio = new Ejercicio(nombre, descripcion, tipo);
            ejercicioDao.insert(ejercicio);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Ejercicio creado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }

    @Override
    public void onEditarClick(Ejercicio ejercicio) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ejercicio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        // Prellenar campos con datos actuales
        Context context = requireContext();
        etNombre.setText(TextResolver.resolveTextFromDB(context, ejercicio.nombre));
        etDescripcion.setText(TextResolver.resolveTextFromDB(context,ejercicio.descripcion));

        // Configurar Spinner con los tipos
        List<TipoEjercicioSpinnerItem> listaTipos = new ArrayList<>();
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
                requireContext(),
                android.R.layout.simple_spinner_item,
                listaTipos
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(spinnerAdapter);

        // Seleccionar el tipo actual en el spinner
        int posicionSeleccionada = 0;
        for (int i = 0; i < listaTipos.size(); i++) {
            if (listaTipos.get(i).getClaveDb().equalsIgnoreCase(ejercicio.tipo)) {
                posicionSeleccionada = i;
                break;
            }
        }
        spinnerTipo.setSelection(posicionSeleccionada);

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar Ejercicio")
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    TipoEjercicioSpinnerItem itemSeleccionado = (TipoEjercicioSpinnerItem) spinnerTipo.getSelectedItem();
                    String tipo = itemSeleccionado.getClaveDb();

                    if (nombre.isEmpty()) {
                        Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarEjercicio(ejercicio, nombre, descripcion, tipo);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void actualizarEjercicio(Ejercicio ejercicio, String nombre, String descripcion, String tipo) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ejercicio.nombre = nombre;
            ejercicio.descripcion = descripcion;
            ejercicio.tipo = tipo;
            ejercicioDao.update(ejercicio);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }

    @Override
    public void onEliminarClick(Ejercicio ejercicio) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Ejercicio")
                .setMessage("¿Estás seguro de eliminar '" + TextResolver.resolveTextFromDB(requireContext(), ejercicio.nombre) + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEjercicio(ejercicio))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void eliminarEjercicio(Ejercicio ejercicio) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ejercicioDao.delete(ejercicio);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                cargarEjercicios();
            });
        });
    }
}