package com.example.practicaandroid.data.relaciones;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EjercicioMusculoDao {

    @Insert
    long insert(EjercicioMusculo ejercicioMusculo);

    @Delete
    void delete(EjercicioMusculo ejercicioMusculo);

    @Query("SELECT * FROM ejercicio_musculo WHERE ejercicioId = :ejercicioId")
    List<EjercicioMusculo> getByEjercicio(long ejercicioId);

    @Query("SELECT * FROM ejercicio_musculo WHERE musculoId = :musculoId")
    List<EjercicioMusculo> getByMusculo(long musculoId);

    // Eliminar todas las relaciones de un ejercicio (útil para actualizar)
    @Query("DELETE FROM ejercicio_musculo WHERE ejercicioId = :ejercicioId")
    void deleteByEjercicio(long ejercicioId);

    // Obtener ejercicios que trabajan un músculo específico
    @Query("SELECT e.* FROM ejercicios e " +
           "INNER JOIN ejercicio_musculo em ON e.id = em.ejercicioId " +
           "WHERE em.musculoId = :musculoId")
    List<com.example.practicaandroid.data.ejercicio.Ejercicio> getEjerciciosQueTrabajanMusculo(long musculoId);
}

