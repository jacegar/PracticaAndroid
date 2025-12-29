package com.example.practicaandroid.data.material;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "materiales")
public class Material {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre; // ej: "Mancuernas", "Banco plano", "Barra olímpica"
    public String descripcion;

    public Material(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}

