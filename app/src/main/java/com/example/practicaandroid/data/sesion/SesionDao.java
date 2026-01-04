package com.example.practicaandroid.data.sesion;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SesionDao {
    @Insert
    long insert(Sesion sesion);

    @Update
    void update(Sesion sesion);

    @Delete
    void delete(Sesion sesion);

    @Query("SELECT * FROM sesiones WHERE rutinaId = :rutinaId")
    List<Sesion> getSesionByRutina(long rutinaId);

    @Query("SELECT * FROM sesiones WHERE id = :sesionId")
    Sesion getSesionById(long sesionId);

    //sesiones planificadas en un rango de fechas
    @Query("SELECT * FROM sesiones WHERE diaPlanificado BETWEEN :inicio AND :fin")
    List<Sesion> getSesionesEnRango(long inicio, long fin);

    //Sesiones completadas
    @Query("SELECT * FROM sesiones WHERE fechaRealizada != 0 ORDER BY fechaRealizada DESC")
    List<Sesion> getSesionesCompletadas();

    //sesiones completadas en un rango de fechas (Para estadísticas)
    @Query("SELECT * FROM sesiones WHERE fechaRealizada BETWEEN :inicio AND :fin ORDER BY fechaRealizada DESC")
    List<Sesion> getSesionesCompletadasEnRango(long inicio, long fin);

    //Proxima sesion no planificada
    @Query("SELECT * FROM sesiones WHERE rutinaId = :rutinaId AND" +
            " (fechaRealizada IS NULL OR fechaRealizada = 0) ORDER BY diaPlanificado ASC LIMIT 1")
    Sesion getProximaSesionDeRutina(long rutinaId);


    @Query("SELECT * FROM sesiones WHERE diaPlanificado > :timestamp")
    List<Sesion> getSesionesFuturas(long timestamp);

    // Métodos para sesiones recurrentes
    @Query("SELECT * FROM sesiones WHERE recurringGroupId = :groupId")
    List<Sesion> getSesionesByRecurringGroup(String groupId);

    @Query("DELETE FROM sesiones WHERE recurringGroupId = :groupId")
    void deleteByRecurringGroup(String groupId);

    @Query("UPDATE sesiones SET nombre = :nuevoNombre WHERE recurringGroupId = :groupId")
    void updateNombreByRecurringGroup(String groupId, String nuevoNombre);
}
