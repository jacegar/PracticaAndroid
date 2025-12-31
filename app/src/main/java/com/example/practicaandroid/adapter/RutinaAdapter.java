package com.example.practicaandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicaandroid.R;
import com.example.practicaandroid.data.rutina.Rutina;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private List<Rutina> rutinas = new ArrayList<>();
    private OnRutinaClickListener listener;

    public interface OnRutinaClickListener {
        void onEditarClick(Rutina rutina);
        void onEliminarClick(Rutina rutina);
        void onVerSesionesClick(Rutina rutina);
        void onActivarClick(Rutina rutina);
    }

    public RutinaAdapter(OnRutinaClickListener listener) {
        this.listener = listener;
    }

    public void setRutinas(List<Rutina> rutinas) {
        this.rutinas = rutinas != null ? rutinas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        Rutina rutina = rutinas.get(position);
        holder.bind(rutina, listener);
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    static class RutinaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvFecha, tvActivar;
        Button btnEditar, btnEliminar;
        LinearLayout layoutActivar;
        ImageView ivActivar;

        RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            //Activar/Desactivar rutina
            tvActivar = itemView.findViewById(R.id.tvActivar);
            layoutActivar = itemView.findViewById(R.id.layoutActivar);
            ivActivar = itemView.findViewById(R.id.ivActivar);
        }

        void bind(Rutina rutina, OnRutinaClickListener listener) {
            tvNombre.setText(rutina.nombre);
            tvDescripcion.setText(rutina.descripcion);

            // Formatear fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fecha = sdf.format(new Date(rutina.fechaCreacion));
            tvFecha.setText("Creada: " + fecha);

            // Click en la tarjeta completa abre las sesiones
            itemView.setOnClickListener(v -> listener.onVerSesionesClick(rutina));

            btnEditar.setOnClickListener(v -> listener.onEditarClick(rutina));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(rutina));

            layoutActivar.setOnClickListener(v ->{
                listener.onActivarClick(rutina);
            });

            if(rutina.rutinaActiva){
                ivActivar.setImageResource(R.drawable.ic_active);
                tvActivar.setText(R.string.deactivate);
            }else{
                ivActivar.setImageResource(R.drawable.ic_inactive);
                tvActivar.setText(R.string.activate);
            }
        }
    }
}

