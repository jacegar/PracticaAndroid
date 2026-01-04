package com.example.practicaandroid.data.sesion;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;

import com.example.practicaandroid.data.rutina.Rutina;

@Entity(
    tableName = "sesiones",
    foreignKeys = @ForeignKey(
        entity = Rutina.class,
        parentColumns = "id",
        childColumns = "rutinaId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("rutinaId")}
)

public class Sesion {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long rutinaId;
    public String nombre;
    public long diaPlanificado; // para notificacion
    public long fechaRealizada; // para historial, 0 si no esta completado
    public String recurringGroupId; // identificador para agrupar sesiones recurrentes, null si no es recurrente

    public Sesion(long rutinaId, String nombre, long diaPlanificado) {
        this.rutinaId = rutinaId;
        this.nombre = nombre;
        this.diaPlanificado = diaPlanificado;
        this.fechaRealizada = 0;
        this.recurringGroupId = null;
    }
}
