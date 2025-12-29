package com.example.practicaandroid.data.musculo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "musculos")
public class Musculo {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre; // ej: "Pectoral mayor", "Bíceps", "Cuádriceps"
    public String grupo; // ej: "Pecho", "Brazos", "Piernas"

    public Musculo(String nombre, String grupo) {
        this.nombre = nombre;
        this.grupo = grupo;
    }
}

