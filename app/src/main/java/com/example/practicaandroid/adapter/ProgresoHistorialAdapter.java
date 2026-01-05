package com.example.practicaandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.sesion.Sesion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgresoHistorialAdapter extends RecyclerView.Adapter<ProgresoHistorialAdapter.HistorialViewHolder> {

    private List<ItemHistorial> historial = new ArrayList<>();
    private Context context;
    private String tipoEjercicio;

    public ProgresoHistorialAdapter(Context context, String tipoEjercicio) {
        this.context = context;
        this.tipoEjercicio = tipoEjercicio;
    }

    public void setHistorial(List<ItemHistorial> historial) {
        this.historial = historial != null ? historial : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progreso_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        ItemHistorial item = historial.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historial.size();
    }

    class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvNombreSesion, tvDatosProgreso;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvNombreSesion = itemView.findViewById(R.id.tvNombreSesion);
            tvDatosProgreso = itemView.findViewById(R.id.tvDatosProgreso);
        }

        public void bind(ItemHistorial item) {
            // Formatear fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvFecha.setText(sdf.format(new Date(item.sesion.fechaRealizada)));

            tvNombreSesion.setText(item.sesion.nombre);

            // Mostrar datos según el tipo de ejercicio
            StringBuilder datos = new StringBuilder();
            SesionEjercicio se = item.sesionEjercicio;

            if (tipoEjercicio.equals("strength_type")) {
                // Ejercicio de fuerza
                if (se.series > 0) {
                    datos.append(context.getString(R.string.sets_format, se.series)).append("\n");
                }
                if (se.repeticiones > 0) {
                    datos.append(context.getString(R.string.reps_format, se.repeticiones)).append("\n");
                }
                if (se.peso > 0) {
                    datos.append(context.getString(R.string.weight_format, se.peso)).append("\n");
                }
            } else if (tipoEjercicio.equals("cardio_type")) {
                // Ejercicio de cardio
                if (se.duracionSegundos > 0) {
                    int minutos = se.duracionSegundos / 60;
                    int segundos = se.duracionSegundos % 60;
                    datos.append(context.getString(R.string.duration_format, minutos, segundos)).append("\n");
                }
                if (se.distanciaKm > 0) {
                    datos.append(context.getString(R.string.distance_format, se.distanciaKm)).append("\n");
                }
            } else {
                // Ejercicio de flexibilidad u otro
                if (se.series > 0) {
                    datos.append(context.getString(R.string.sets_format, se.series)).append("\n");
                }
                if (se.repeticiones > 0) {
                    datos.append(context.getString(R.string.reps_format, se.repeticiones)).append("\n");
                }
                if (se.duracionSegundos > 0) {
                    int minutos = se.duracionSegundos / 60;
                    int segundos = se.duracionSegundos % 60;
                    datos.append(context.getString(R.string.duration_format, minutos, segundos)).append("\n");
                }
            }

            if (datos.length() == 0) {
                datos.append(context.getString(R.string.no_data_recorded));
            } else {
                // Eliminar el último salto de línea
                datos.setLength(datos.length() - 1);
            }

            tvDatosProgreso.setText(datos.toString());
        }
    }

    public static class ItemHistorial {
        public SesionEjercicio sesionEjercicio;
        public Sesion sesion;

        public ItemHistorial(SesionEjercicio sesionEjercicio, Sesion sesion) {
            this.sesionEjercicio = sesionEjercicio;
            this.sesion = sesion;
        }
    }
}

