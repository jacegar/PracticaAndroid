package com.example.practicaandroid.data.rutina;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RutinaDao {
    @Insert
    long insert(Rutina rutina);

    @Update
    void update(Rutina rutina);

    @Delete
    void delete(Rutina rutina);

    @Query("SELECT * FROM rutinas WHERE id = :id")
    Rutina getRutinaById(long id);

    @Query("SELECT * FROM rutinas ORDER BY fechaCreacion DESC")
    List<Rutina> getAllRutinas();

    @Query("UPDATE rutinas SET rutinaActiva = 0")
    void desactivarTodasLasRutinas();

    @Query("UPDATE rutinas SET rutinaActiva = 1 WHERE id = :rutinaId")
    void activarRutinaPorId(long rutinaId);

    @Transaction
    default void setUnicaRutinaActiva(long rutinaId) {
        desactivarTodasLasRutinas();
        activarRutinaPorId(rutinaId);
    }

    @Query("SELECT * FROM rutinas WHERE rutinaActiva = 1 LIMIT 1")
    Rutina getRutinaActiva();

    @Query("SELECT COUNT (*) FROM rutinas")
    int count();
}
