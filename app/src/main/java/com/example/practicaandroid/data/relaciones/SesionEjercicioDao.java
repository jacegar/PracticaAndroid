package com.example.practicaandroid.data.relaciones;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SesionEjercicioDao {

    @Insert
    long insert(SesionEjercicio sesionEjercicio);

    @Update
    void update(SesionEjercicio sesionEjercicio);

    @Delete
    void delete(SesionEjercicio sesionEjercicio);

    @Query("SELECT * FROM sesion_ejercicio WHERE sesionId = :sesionId ORDER BY orden ASC")
    List<SesionEjercicio> getBySesion(long sesionId);

    @Query("SELECT * FROM sesion_ejercicio WHERE ejercicioId = :ejercicioId ORDER BY id DESC")
    List<SesionEjercicio> getByEjercicio(long ejercicioId);

    @Query("SELECT * FROM sesion_ejercicio WHERE sesionId = :sesionId AND ejercicioId = :ejercicioId")
    SesionEjercicio getBySesionYEjercicio(long sesionId, long ejercicioId);

    // SEGUIMIENTO DE PROGRESO: Historial de un ejercicio específico en sesiones completadas
    @Query("SELECT se.* FROM sesion_ejercicio se " +
           "INNER JOIN sesiones s ON se.sesionId = s.id " +
           "WHERE se.ejercicioId = :ejercicioId AND s.fechaRealizada != 0 " +
           "ORDER BY s.fechaRealizada DESC")
    List<SesionEjercicio> getHistorialEjercicio(long ejercicioId);

    // PROGRESO DE PESO: Ver evolución de pesos levantados
    @Query("SELECT se.* FROM sesion_ejercicio se " +
           "INNER JOIN sesiones s ON se.sesionId = s.id " +
           "WHERE se.ejercicioId = :ejercicioId AND s.fechaRealizada != 0 AND se.peso > 0 " +
           "ORDER BY s.fechaRealizada ASC")
    List<SesionEjercicio> getProgresoPeso(long ejercicioId);

    // PROGRESO CARDIO: Ver evolución de duración y distancia
    @Query("SELECT se.* FROM sesion_ejercicio se " +
           "INNER JOIN sesiones s ON se.sesionId = s.id " +
           "WHERE se.ejercicioId = :ejercicioId AND s.fechaRealizada != 0 " +
           "AND (se.duracionSegundos > 0 OR se.distanciaKm > 0) " +
           "ORDER BY s.fechaRealizada ASC")
    List<SesionEjercicio> getProgresoCardio(long ejercicioId);

    // FRECUENCIA: Contar cuántas veces se ha hecho un ejercicio en un rango de fechas
    @Query("SELECT COUNT(*) FROM sesion_ejercicio se " +
           "INNER JOIN sesiones s ON se.sesionId = s.id " +
           "WHERE se.ejercicioId = :ejercicioId AND s.fechaRealizada BETWEEN :inicio AND :fin")
    int getFrecuenciaEjercicio(long ejercicioId, long inicio, long fin);
}

