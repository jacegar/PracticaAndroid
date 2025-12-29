package com.example.practicaandroid.data.relaciones;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.ejercicio.Ejercicio;

/**
 * Tabla intermedia: Sesión - Ejercicio (Muchos a Muchos)
 * Guarda los datos de progreso: peso, series, repeticiones, duración, distancia
 */
@Entity(
    tableName = "sesion_ejercicio",
    foreignKeys = {
        @ForeignKey(
            entity = Sesion.class,
            parentColumns = "id",
            childColumns = "sesionId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Ejercicio.class,
            parentColumns = "id",
            childColumns = "ejercicioId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("sesionId"), @Index("ejercicioId")}
)
public class SesionEjercicio {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long sesionId;
    public long ejercicioId;

    // Datos para seguimiento de progreso
    public int series;
    public int repeticiones;
    public float peso; // en kg (para ejercicios de fuerza)
    public int duracionSegundos; // para cardio
    public float distanciaKm; // para cardio
    public int orden; // orden del ejercicio en la sesión
    public boolean completado;

    public SesionEjercicio(long sesionId, long ejercicioId, int orden) {
        this.sesionId = sesionId;
        this.ejercicioId = ejercicioId;
        this.orden = orden;
        this.series = 0;
        this.repeticiones = 0;
        this.peso = 0;
        this.duracionSegundos = 0;
        this.distanciaKm = 0;
        this.completado = false;
    }
}
