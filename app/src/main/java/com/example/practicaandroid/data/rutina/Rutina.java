package com.example.practicaandroid.data.rutina;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rutinas")
public class Rutina {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre;
    public String descripcion;
    public long fechaCreacion;
    @ColumnInfo(defaultValue = "false")
    public boolean rutinaActiva;

    public Rutina(String nombre, String descripcion, long fechaCreacion, boolean rutinaActiva){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.rutinaActiva = rutinaActiva;
    }
}
