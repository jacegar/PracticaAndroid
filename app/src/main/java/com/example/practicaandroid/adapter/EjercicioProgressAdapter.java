package com.example.practicaandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.util.TextResolver;

import java.util.ArrayList;
import java.util.List;

public class EjercicioProgressAdapter extends RecyclerView.Adapter<EjercicioProgressAdapter.ProgressViewHolder> {

    private List<EjercicioConProgreso> ejercicios = new ArrayList<>();
    private List<EjercicioConProgreso> ejerciciosCompletos = new ArrayList<>();
    private OnEjercicioProgressClickListener listener;
    private Context context;

    public interface OnEjercicioProgressClickListener {
        void onEjercicioClick(Ejercicio ejercicio);
    }

    public EjercicioProgressAdapter(Context context, OnEjercicioProgressClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setEjercicios(List<EjercicioConProgreso> ejercicios) {
        this.ejercicios = ejercicios != null ? ejercicios : new ArrayList<>();
        this.ejerciciosCompletos = new ArrayList<>(this.ejercicios);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            ejercicios = new ArrayList<>(ejerciciosCompletos);
        } else {
            String textoBusqueda = texto.toLowerCase().trim();
            List<EjercicioConProgreso> listaFiltrada = new ArrayList<>();

            for (EjercicioConProgreso item : ejerciciosCompletos) {
                Ejercicio ejercicio = item.ejercicio;
                if (TextResolver.resolveTextFromDB(context, ejercicio.nombre).toLowerCase().contains(textoBusqueda) ||
                        TextResolver.resolveTextFromDB(context, ejercicio.tipo).toLowerCase().contains(textoBusqueda) ||
                        (ejercicio.descripcion != null &&
                         TextResolver.resolveTextFromDB(context, ejercicio.descripcion).toLowerCase().contains(textoBusqueda))) {
                    listaFiltrada.add(item);
                }
            }
            ejercicios = listaFiltrada;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        EjercicioConProgreso item = ejercicios.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTipo, tvDescripcion;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEjercicioClick(ejercicios.get(pos).ejercicio);
                }
            });
        }

        public void bind(EjercicioConProgreso item) {
            Ejercicio ejercicio = item.ejercicio;

            tvNombre.setText(TextResolver.resolveTextFromDB(context, ejercicio.nombre));
            tvTipo.setText(context.getString(R.string.type_colon,
                    TextResolver.resolve(context, ejercicio.tipo)));

            if (ejercicio.descripcion != null && !ejercicio.descripcion.isEmpty()) {
                tvDescripcion.setText(TextResolver.resolveTextFromDB(context, ejercicio.descripcion));
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }
        }
    }

    public static class EjercicioConProgreso {
        public Ejercicio ejercicio;
        public int vecesRealizado;
        public String ultimoProgreso;
        public String mejorMarca;

        public EjercicioConProgreso(Ejercicio ejercicio, int vecesRealizado, String ultimoProgreso, String mejorMarca) {
            this.ejercicio = ejercicio;
            this.vecesRealizado = vecesRealizado;
            this.ultimoProgreso = ultimoProgreso;
            this.mejorMarca = mejorMarca;
        }
    }
}

