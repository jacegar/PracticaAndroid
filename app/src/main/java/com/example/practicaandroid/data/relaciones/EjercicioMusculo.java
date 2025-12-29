package com.example.practicaandroid.data.relaciones;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.musculo.Musculo;

/**
 * Tabla intermedia: Ejercicio - Músculo (Muchos a Muchos)
 * Un ejercicio trabaja varios músculos
 * Un músculo es trabajado por varios ejercicios
 */
@Entity(
    tableName = "ejercicio_musculo",
    foreignKeys = {
        @ForeignKey(
            entity = Ejercicio.class,
            parentColumns = "id",
            childColumns = "ejercicioId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Musculo.class,
            parentColumns = "id",
            childColumns = "musculoId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("ejercicioId"), @Index("musculoId")}
)
public class EjercicioMusculo {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long ejercicioId;
    public long musculoId;
    public String intensidad; // "PRINCIPAL", "SECUNDARIO"

    public EjercicioMusculo(long ejercicioId, long musculoId, String intensidad) {
        this.ejercicioId = ejercicioId;
        this.musculoId = musculoId;
        this.intensidad = intensidad;
    }
}

