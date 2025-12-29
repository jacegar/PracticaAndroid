package com.example.practicaandroid.data.musculo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MusculoDao {
    @Insert
    long insert(Musculo musculo);

    @Update
    void update(Musculo musculo);

    @Delete
    void delete(Musculo musculo);

    @Query("SELECT * FROM musculos ORDER BY grupo, nombre ASC")
    List<Musculo> getAll();

    @Query("SELECT * FROM musculos WHERE id = :id")
    Musculo getById(long id);

    @Query("SELECT * FROM musculos WHERE grupo = :grupo ORDER BY nombre ASC")
    List<Musculo> getByGrupo(String grupo);

    // Obtener músculos trabajados por un ejercicio
    @Query("SELECT m.* FROM musculos m " +
           "INNER JOIN ejercicio_musculo em ON m.id = em.musculoId " +
           "WHERE em.ejercicioId = :ejercicioId")
    List<Musculo> getMusculosDeEjercicio(long ejercicioId);
}

