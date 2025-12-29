package com.example.practicaandroid.data.relaciones;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.material.Material;

/**
 * Tabla intermedia: Ejercicio - Material (Muchos a Muchos)
 * Un ejercicio puede necesitar varios materiales
 * Un material puede ser usado en varios ejercicios
 */
@Entity(
    tableName = "ejercicio_material",
    foreignKeys = {
        @ForeignKey(
            entity = Ejercicio.class,
            parentColumns = "id",
            childColumns = "ejercicioId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Material.class,
            parentColumns = "id",
            childColumns = "materialId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("ejercicioId"), @Index("materialId")}
)
public class EjercicioMaterial {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long ejercicioId;
    public long materialId;
    public boolean obligatorio; // true = obligatorio, false = alternativo/opcional

    public EjercicioMaterial(long ejercicioId, long materialId, boolean obligatorio) {
        this.ejercicioId = ejercicioId;
        this.materialId = materialId;
        this.obligatorio = obligatorio;
    }
}

