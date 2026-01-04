package com.example.practicaandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.util.TextResolver;

import java.util.ArrayList;
import java.util.List;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private List<Ejercicio> ejercicios = new ArrayList<>();
    private List<Ejercicio> ejerciciosCompletos = new ArrayList<>();
    private OnEjercicioClickListener listener;
    private Context context;

    public interface OnEjercicioClickListener {
        void onEditarClick(Ejercicio ejercicio);
        void onEliminarClick(Ejercicio ejercicio);
    }

    public EjercicioAdapter(Context context, OnEjercicioClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios != null ? ejercicios : new ArrayList<>();
        this.ejerciciosCompletos = new ArrayList<>(this.ejercicios);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            ejercicios = new ArrayList<>(ejerciciosCompletos);
        } else {
            String textoBusqueda = texto.toLowerCase().trim();
            List<Ejercicio> listaFiltrada = new ArrayList<>();

            for (Ejercicio ejercicio : ejerciciosCompletos) {
                if (TextResolver.resolve(context, ejercicio.nombre).toLowerCase().contains(textoBusqueda) ||
                    ejercicio.tipo.toLowerCase().contains(textoBusqueda) ||
                    TextResolver.resolve(context, ejercicio.descripcion).toLowerCase().contains(textoBusqueda)) {
                        listaFiltrada.add(ejercicio);
                }
            }

            ejercicios = listaFiltrada;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicios.get(position);
        holder.bind(context, ejercicio, listener);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvTipo;
        Button btnEditar, btnEliminar;

        EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        void bind(Context context, Ejercicio ejercicio, OnEjercicioClickListener listener) {
            tvNombre.setText(TextResolver.resolve(context, ejercicio.nombre));
            tvDescripcion.setText(ejercicio.descripcion.isEmpty() ? "Sin descripción" : TextResolver.resolve(context, ejercicio.descripcion));
            tvTipo.setText("Tipo: " + ejercicio.tipo);

            btnEditar.setOnClickListener(v -> listener.onEditarClick(ejercicio));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(ejercicio));
        }
    }
}
