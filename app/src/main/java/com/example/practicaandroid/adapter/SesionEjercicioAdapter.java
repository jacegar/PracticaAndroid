package com.example.practicaandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;

import java.util.ArrayList;
import java.util.List;

public class SesionEjercicioAdapter extends RecyclerView.Adapter<SesionEjercicioAdapter.ViewHolder> {

    private List<EjercicioConDatos> ejercicios = new ArrayList<>();
    private OnSesionEjercicioClickListener listener;
    private Context context;

    public interface OnSesionEjercicioClickListener {
        void onEditarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio);
        void onEliminarClick(SesionEjercicio sesionEjercicio, Ejercicio ejercicio);
        void onMarcarCompletadoClick(SesionEjercicio sesionEjercicio);
    }

    public static class EjercicioConDatos {
        public SesionEjercicio sesionEjercicio;
        public Ejercicio ejercicio;

        public EjercicioConDatos(SesionEjercicio sesionEjercicio, Ejercicio ejercicio) {
            this.sesionEjercicio = sesionEjercicio;
            this.ejercicio = ejercicio;
        }
    }

    public SesionEjercicioAdapter(Context context, OnSesionEjercicioClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setEjercicios(List<EjercicioConDatos> ejercicios) {
        this.ejercicios = ejercicios != null ? ejercicios : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sesion_ejercicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EjercicioConDatos item = ejercicios.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTipo, tvDatos;
        CheckBox checkCompletado;
        Button btnEditar, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvDatos = itemView.findViewById(R.id.tvDatos);
            checkCompletado = itemView.findViewById(R.id.checkCompletado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        void bind(EjercicioConDatos item, OnSesionEjercicioClickListener listener) {
            SesionEjercicio se = item.sesionEjercicio;
            Ejercicio ejercicio = item.ejercicio;

            tvNombre.setText(ejercicio.nombre);
            tvTipo.setText(ejercicio.tipo);

            // Mostrar datos según el tipo
            StringBuilder datos = new StringBuilder();
            if ("FUERZA".equals(ejercicio.tipo)) {
                if (se.series > 0) datos.append(se.series).append(" series");
                if (se.repeticiones > 0) {
                    if (datos.length() > 0) datos.append(" × ");
                    datos.append(se.repeticiones).append(" reps");
                }
                if (se.peso > 0) {
                    if (datos.length() > 0) datos.append(" - ");
                    datos.append(se.peso).append(" kg");
                }
            } else if ("CARDIO".equals(ejercicio.tipo)) {
                if (se.duracionSegundos > 0) {
                    int minutos = se.duracionSegundos / 60;
                    datos.append(minutos).append(" min");
                }
                if (se.distanciaKm > 0) {
                    if (datos.length() > 0) datos.append(" - ");
                    datos.append(se.distanciaKm).append(" km");
                }
            } else if ("FLEXIBILIDAD".equals(ejercicio.tipo)) {
                datos.append("Ejercicio de flexibilidad");
            }

            if (datos.length() == 0) {
                datos.append("Sin datos configurados");
            }

            tvDatos.setText(datos.toString());

            checkCompletado.setChecked(se.completado);
            checkCompletado.setOnClickListener(v -> listener.onMarcarCompletadoClick(se));

            btnEditar.setOnClickListener(v -> listener.onEditarClick(se, ejercicio));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(se, ejercicio));
        }
    }
}

