package com.example.practicaandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.sesion.Sesion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.SesionViewHolder> {

    private List<Sesion> sesiones = new ArrayList<>();
    private OnSesionClickListener listener;

    public interface OnSesionClickListener {
        void onEditarClick(Sesion sesion);
        void onEliminarClick(Sesion sesion);
        void onMarcarCompletadaClick(Sesion sesion);
        void onVerEjerciciosClick(Sesion sesion);
    }

    public SesionAdapter(OnSesionClickListener listener) {
        this.listener = listener;
    }

    public void setSesiones(List<Sesion> sesiones) {
        this.sesiones = sesiones != null ? sesiones : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SesionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sesion, parent, false);
        return new SesionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SesionViewHolder holder, int position) {
        Sesion sesion = sesiones.get(position);
        holder.bind(sesion, listener);
    }

    @Override
    public int getItemCount() {
        return sesiones.size();
    }

    static class SesionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDiaPlanificado, tvEstado;
        Button btnEditar, btnEliminar, btnCompletar;

        SesionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDiaPlanificado = itemView.findViewById(R.id.tvDiaPlanificado);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnCompletar = itemView.findViewById(R.id.btnCompletar);
        }

        void bind(Sesion sesion, OnSesionClickListener listener) {
            tvNombre.setText(sesion.nombre);

            // Formatear día planificado
            SimpleDateFormat sdfDia = new SimpleDateFormat("EEEE", Locale.getDefault());
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String diaSemana = sdfDia.format(new Date(sesion.diaPlanificado));
            String fechaCompleta = sdfFecha.format(new Date(sesion.diaPlanificado));

            // Capitalizar primera letra del día
            diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);

            // Añadir indicador de recurrencia si es una sesión recurrente
            String textoFecha = diaSemana + " - " + fechaCompleta;
            if (sesion.recurringGroupId != null && !sesion.recurringGroupId.isEmpty()) {
                textoFecha += " " + itemView.getContext().getString(R.string.recurring_indicator);
            }
            tvDiaPlanificado.setText(textoFecha);

            // Mostrar estado
            if (sesion.fechaRealizada == 0) {
                tvEstado.setText("⏳ Pendiente");
                tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                btnCompletar.setText("Completar");
            } else {
                SimpleDateFormat sdfCompletada = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String fechaCompletada = sdfCompletada.format(new Date(sesion.fechaRealizada));
                tvEstado.setText("✓ Completada: " + fechaCompletada);
                tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                btnCompletar.setText("Desmarcar");
            }

            // Click en la tarjeta completa para ver ejercicios
            itemView.setOnClickListener(v -> listener.onVerEjerciciosClick(sesion));

            btnEditar.setOnClickListener(v -> listener.onEditarClick(sesion));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(sesion));
            btnCompletar.setOnClickListener(v -> listener.onMarcarCompletadaClick(sesion));
        }
    }
}

