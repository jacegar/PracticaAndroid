package com.example.practicaandroid.data.ejercicio;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EjercicioDao {
    @Insert
    long insert(Ejercicio ejercicio);

    @Insert
    void insertAll(List<Ejercicio> ejerciciosList);

    @Update
    void update(Ejercicio ejercicio);

    @Delete
    void delete(Ejercicio ejercicio);

    @Query("SELECT * FROM ejercicios ORDER BY nombre ASC")
    List<Ejercicio> getAll();

    @Query("SELECT * FROM ejercicios WHERE id = :id")
    Ejercicio getById(long id);

    @Query("SELECT * FROM ejercicios WHERE tipo = :tipo ORDER BY nombre ASC")
    List<Ejercicio> getByTipo(String tipo);

    // Obtener ejercicios de una sesión específica
    @Query("SELECT e.* FROM ejercicios e " +
           "INNER JOIN sesion_ejercicio se ON e.id = se.ejercicioId " +
           "WHERE se.sesionId = :sesionId ORDER BY se.orden ASC")
    List<Ejercicio> getEjerciciosDeSesion(long sesionId);
}

