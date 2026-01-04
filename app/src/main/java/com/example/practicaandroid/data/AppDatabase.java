package com.example.practicaandroid.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Entidades principales
import com.example.practicaandroid.data.rutina.Rutina;
import com.example.practicaandroid.data.rutina.RutinaDao;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.example.practicaandroid.data.ejercicio.Ejercicio;
import com.example.practicaandroid.data.ejercicio.EjercicioDao;
import com.example.practicaandroid.data.musculo.Musculo;
import com.example.practicaandroid.data.musculo.MusculoDao;
import com.example.practicaandroid.data.material.Material;
import com.example.practicaandroid.data.material.MaterialDao;

// Tablas intermedias (relaciones muchos a muchos)
import com.example.practicaandroid.data.relaciones.SesionEjercicio;
import com.example.practicaandroid.data.relaciones.SesionEjercicioDao;
import com.example.practicaandroid.data.relaciones.EjercicioMusculo;
import com.example.practicaandroid.data.relaciones.EjercicioMusculoDao;
import com.example.practicaandroid.data.relaciones.EjercicioMaterial;
import com.example.practicaandroid.data.relaciones.EjercicioMaterialDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos principal de la aplicación de entrenamiento
 *
 * Estructura:
 * - Rutina → Sesiones (uno a muchos)
 * - Sesión ↔ Ejercicios (muchos a muchos) con datos de progreso
 * - Ejercicio ↔ Músculos (muchos a muchos)
 * - Ejercicio ↔ Material (muchos a muchos)
 */
@Database(
    entities = {
        // Entidades principales
        Rutina.class,
        Sesion.class,
        Ejercicio.class,
        Musculo.class,
        Material.class,

        // Tablas intermedias (relaciones muchos a muchos)
        SesionEjercicio.class,
        EjercicioMusculo.class,
        EjercicioMaterial.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs principales
    public abstract RutinaDao rutinaDao();
    public abstract SesionDao sesionDao();
    public abstract EjercicioDao ejercicioDao();
    public abstract MusculoDao musculoDao();
    public abstract MaterialDao materialDao();

    // DAOs de tablas intermedias
    public abstract SesionEjercicioDao sesionEjercicioDao();
    public abstract EjercicioMusculoDao ejercicioMusculoDao();
    public abstract EjercicioMaterialDao ejercicioMaterialDao();

    // Singleton pattern
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final String DATABASE_NAME = "entrenamiento_database";

    /**
     * Obtiene la instancia única de la base de datos
     * @param context Contexto de la aplicación
     * @return Instancia de AppDatabase
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).addMigrations(MIGRATION_1_2)
                     .addCallback(RoomDatabaseCallback)
                     .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE rutinas ADD COLUMN rutinaActiva INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final  RoomDatabase.Callback RoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            /*
            databaseWriteExecutor.execute(() -> {
                EjercicioDao dao = INSTANCE.ejercicioDao();
                List<Ejercicio> ejercicioList = new ArrayList<>();

                ejercicioList.add(new Ejercicio(
                        "ejercicio_res_nombre_flexiones",
                        "ejercicio_res_desc_flexiones",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "ejercicio_res_nombre_sentadillas",
                        "ejercicio_res_desc_sentadillas",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "ejercicio_res_nombre_plancha",
                        "ejercicio_res_desc_plancha",
                        "cardio_type"
                ));

                dao.insertAll(ejercicioList);
            });
             */
        }
    };

}
