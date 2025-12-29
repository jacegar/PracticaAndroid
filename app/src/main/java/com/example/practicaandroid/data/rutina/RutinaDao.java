package com.example.practicaandroid.data.rutina;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
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
}
