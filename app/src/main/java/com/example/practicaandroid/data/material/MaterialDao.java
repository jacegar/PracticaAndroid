package com.example.practicaandroid.data.material;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MaterialDao {
    @Insert
    long insert(Material material);

    @Update
    void update(Material material);

    @Delete
    void delete(Material material);

    @Query("SELECT * FROM materiales ORDER BY nombre ASC")
    List<Material> getAll();

    @Query("SELECT * FROM materiales WHERE id = :id")
    Material getById(long id);

    // Obtener materiales necesarios para un ejercicio
    @Query("SELECT m.* FROM materiales m " +
           "INNER JOIN ejercicio_material em ON m.id = em.materialId " +
           "WHERE em.ejercicioId = :ejercicioId")
    List<Material> getMaterialesDeEjercicio(long ejercicioId);

    // Obtener materiales obligatorios para un ejercicio
    @Query("SELECT m.* FROM materiales m " +
           "INNER JOIN ejercicio_material em ON m.id = em.materialId " +
           "WHERE em.ejercicioId = :ejercicioId AND em.obligatorio = 1")
    List<Material> getMaterialesObligatorios(long ejercicioId);
}

