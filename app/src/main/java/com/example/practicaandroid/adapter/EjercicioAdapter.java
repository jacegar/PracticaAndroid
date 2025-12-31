package com.example.practicaandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.ejercicio.Ejercicio;

import java.util.ArrayList;
import java.util.List;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private List<Ejercicio> ejercicios = new ArrayList<>();
    private OnEjercicioClickListener listener;

    public interface OnEjercicioClickListener {
        void onEditarClick(Ejercicio ejercicio);
        void onEliminarClick(Ejercicio ejercicio);
    }

    public EjercicioAdapter(OnEjercicioClickListener listener) {
        this.listener = listener;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios != null ? ejercicios : new ArrayList<>();
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
        holder.bind(ejercicio, listener);
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

        void bind(Ejercicio ejercicio, OnEjercicioClickListener listener) {
            tvNombre.setText(ejercicio.nombre);
            tvDescripcion.setText(ejercicio.descripcion.isEmpty() ? "Sin descripción" : ejercicio.descripcion);
            tvTipo.setText("Tipo: " + ejercicio.tipo);

            btnEditar.setOnClickListener(v -> listener.onEditarClick(ejercicio));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(ejercicio));
        }
    }
}
