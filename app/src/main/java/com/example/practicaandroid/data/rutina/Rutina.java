package com.example.practicaandroid.data.rutina;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rutinas")
public class Rutina {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre;
    public String descripcion;
    public long fechaCreacion;

    public Rutina(String nombre, String descripcion, long fechaCreacion){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }
}
