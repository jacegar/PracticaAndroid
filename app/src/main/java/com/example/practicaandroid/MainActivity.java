package com.example.practicaandroid;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.practicaandroid.data.AppDatabase;
import com.example.practicaandroid.data.sesion.Sesion;
import com.example.practicaandroid.data.sesion.SesionDao;
import com.example.practicaandroid.notifications.SessionReminderWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String LANGUAGE_KEY = "language";
    private SesionDao sesionDao;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, R.string.conceded_notification_permit, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.denied_permit, Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved language before setting content view
        loadLocale();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);

        // Inicializar base de datos
        AppDatabase.getInstance(this);
        sesionDao = AppDatabase.getInstance(this).sesionDao();

        //Navegacion con la barra inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Fragment workoutFragment = new WorkoutFragment();
        Fragment exerciseFragment = new ExerciseFragment();
        Fragment progressFragment = new ProgressFragment();
        Fragment settingsFragment = new SettingsFragment();

        // Check if we should open settings (after language change)
        boolean openSettings = getIntent().getBooleanExtra("open_settings", false);
        if (openSettings) {
            setCurrentFragment(settingsFragment);
            bottomNavigationView.setSelectedItemId(R.id.settings);
        } else {
            setCurrentFragment(workoutFragment);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if(itemId == R.id.workout){
                selectedFragment = workoutFragment;
            }else if (itemId == R.id.exercises) {
                selectedFragment = exerciseFragment;
            }else if (itemId == R.id.progress) {
                selectedFragment = progressFragment;
            }else if (itemId == R.id.settings) {
                selectedFragment = settingsFragment;
            }

            if(selectedFragment != null){
                setCurrentFragment(selectedFragment);
            }

            return true;
        });

        askNotificationPermission();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String languageCode = prefs.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void reprogramarTodasLasNotificaciones() {
        Executors.newSingleThreadExecutor().execute(() -> {

            WorkManager.getInstance(getApplicationContext()).cancelAllWork();

            List<Sesion> sesionesFuturas = sesionDao.getSesionesFuturas(System.currentTimeMillis());

            if (sesionesFuturas != null && !sesionesFuturas.isEmpty()) {
                for (Sesion sesion : sesionesFuturas) {
                    crearNotificacion(sesion);
                }
            }
        });
    }

    private void crearNotificacion(Sesion sesion){
        if(sesion.fechaRealizada == 0) {
            long diaPlanificadoMillis = sesion.diaPlanificado;
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            long horas_antes = sharedPreferences.getInt("horas_antes_notificacion", 24);
            long tiempoNotificacionMillis = diaPlanificadoMillis - (horas_antes * 60 * 60 * 1000);
            long retrasoInicial = tiempoNotificacionMillis - System.currentTimeMillis();

            // Si la sesion es en el futuro, programamos la notificacion
            if (retrasoInicial > 0) {
                String workTag = "sesion-" + sesion.id;

                Data inputData = new Data.Builder()
                        .putLong("id", sesion.id)
                        .putString("nombre", sesion.nombre)
                        .putLong("fecha", sesion.diaPlanificado)
                        .build();

                OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(SessionReminderWorker.class)
                        .setInitialDelay(retrasoInicial, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag(workTag)
                        .build();

                WorkManager.getInstance(getApplicationContext()).enqueue(reminderRequest);
            }
        }
    }
}

