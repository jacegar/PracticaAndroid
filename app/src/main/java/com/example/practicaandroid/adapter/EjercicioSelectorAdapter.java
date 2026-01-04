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

public class EjercicioSelectorAdapter extends RecyclerView.Adapter<EjercicioSelectorAdapter.ViewHolder> {

    private List<Ejercicio> ejercicios = new ArrayList<>();
    private OnEjercicioClickListener listener;
    private Context context;

    public interface OnEjercicioClickListener {
        void onEjercicioClick(Ejercicio ejercicio);
    }

    public EjercicioSelectorAdapter(Context context, OnEjercicioClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios != null ? ejercicios : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicios.get(position);
        holder.bind(context, ejercicio, listener);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvTipo;
        TextView tvDescripcion;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }

        void bind(Context context, Ejercicio ejercicio, OnEjercicioClickListener listener) {
            tvNombre.setText(TextResolver.resolveTextFromDB(context, ejercicio.nombre));
            tvTipo.setText(TextResolver.resolve(context, ejercicio.tipo));

            if (ejercicio.descripcion != null && !ejercicio.descripcion.trim().isEmpty()) {
                tvDescripcion.setText(ejercicio.descripcion);
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEjercicioClick(ejercicio);
                }
            });
        }
    }
}

