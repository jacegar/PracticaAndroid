package com.example.practicaandroid.data.ejercicio;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ejercicios")
public class Ejercicio {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre;
    public String descripcion;
    public String tipo; //el tipo de ejercicio (fuerza, cardio, flexibilidad, etc.)

    public Ejercicio(String nombre, String descripcion, String tipo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }
}
