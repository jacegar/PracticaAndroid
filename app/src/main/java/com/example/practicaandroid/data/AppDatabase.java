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
    version = 3,
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
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sesiones ADD COLUMN recurringGroupId TEXT");
        }
    };

    private static final  RoomDatabase.Callback RoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                EjercicioDao dao = INSTANCE.ejercicioDao();
                List<Ejercicio> ejercicioList = new ArrayList<>();

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_press_banca",
                        "res:ejercicio_res_desc_press_banca",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_press_inclinado_mancuernas",
                        "res:ejercicio_res_desc_press_inclinado_mancuernas",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_aperturas_mancuernas",
                        "res:ejercicio_res_desc_aperturas_mancuernas",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_jalon_pecho",
                        "res:ejercicio_res_desc_jalon_pecho",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_remo_barra",
                        "res:ejercicio_res_desc_remo_barra",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_remo_polea_baja",
                        "res:ejercicio_res_desc_remo_polea_baja",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_peso_muerto",
                        "res:ejercicio_res_desc_peso_muerto",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_sentadilla_barra",
                        "res:ejercicio_res_desc_sentadilla_barra",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_prensa_piernas",
                        "res:ejercicio_res_desc_prensa_piernas",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_extension_cuadriceps",
                        "res:ejercicio_res_desc_extension_cuadriceps",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_curl_femoral",
                        "res:ejercicio_res_desc_curl_femoral",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_elevaciones_gemelos_maquina",
                        "res:ejercicio_res_desc_elevaciones_gemelos_maquina",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_press_militar",
                        "res:ejercicio_res_desc_press_militar",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_elevaciones_laterales",
                        "res:ejercicio_res_desc_elevaciones_laterales",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_curl_biceps_barra",
                        "res:ejercicio_res_desc_curl_biceps_barra",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_curl_biceps_mancuernas",
                        "res:ejercicio_res_desc_curl_biceps_mancuernas",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_extension_triceps_polea",
                        "res:ejercicio_res_desc_extension_triceps_polea",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_press_frances",
                        "res:ejercicio_res_desc_press_frances",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_abdominales_maquina",
                        "res:ejercicio_res_desc_abdominales_maquina",
                        "strength_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_cinta_correr",
                        "res:ejercicio_res_desc_cinta_correr",
                        "cardio_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_bicicleta_estatica",
                        "res:ejercicio_res_desc_bicicleta_estatica",
                        "cardio_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_eliptica",
                        "res:ejercicio_res_desc_eliptica",
                        "cardio_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_remo_ergometro",
                        "res:ejercicio_res_desc_remo_ergometro",
                        "cardio_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_escalera_gimnasio",
                        "res:ejercicio_res_desc_escalera_gimnasio",
                        "cardio_type"
                ));


                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_estiramiento_isquiotibiales",
                        "res:ejercicio_res_desc_estiramiento_isquiotibiales",
                        "flexibility_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_estiramiento_cuadriceps",
                        "res:ejercicio_res_desc_estiramiento_cuadriceps",
                        "flexibility_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_estiramiento_gluteos",
                        "res:ejercicio_res_desc_estiramiento_gluteos",
                        "flexibility_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_estiramiento_pectoral",
                        "res:ejercicio_res_desc_estiramiento_pectoral",
                        "flexibility_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_estiramiento_dorsal",
                        "res:ejercicio_res_desc_estiramiento_dorsal",
                        "flexibility_type"
                ));

                ejercicioList.add(new Ejercicio(
                        "res:ejercicio_res_name_movilidad_hombros",
                        "res:ejercicio_res_desc_movilidad_hombros",
                        "flexibility_type"
                ));

                dao.insertAll(ejercicioList);
            });
        }
    };

}
