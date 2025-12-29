package com.example.practicaandroid.data.relaciones;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EjercicioMaterialDao {

    @Insert
    long insert(EjercicioMaterial ejercicioMaterial);

    @Delete
    void delete(EjercicioMaterial ejercicioMaterial);

    @Query("SELECT * FROM ejercicio_material WHERE ejercicioId = :ejercicioId")
    List<EjercicioMaterial> getByEjercicio(long ejercicioId);

    @Query("SELECT * FROM ejercicio_material WHERE materialId = :materialId")
    List<EjercicioMaterial> getByMaterial(long materialId);

    // Eliminar todas las relaciones de un ejercicio (útil para actualizar)
    @Query("DELETE FROM ejercicio_material WHERE ejercicioId = :ejercicioId")
    void deleteByEjercicio(long ejercicioId);

    // Obtener ejercicios que usan un material específico
    @Query("SELECT e.* FROM ejercicios e " +
           "INNER JOIN ejercicio_material em ON e.id = em.ejercicioId " +
           "WHERE em.materialId = :materialId")
    List<com.example.practicaandroid.data.ejercicio.Ejercicio> getEjerciciosQueUsanMaterial(long materialId);
}

