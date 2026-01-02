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

import com.example.practicaandroid.data.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String LANGUAGE_KEY = "language";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso denegado. No recibirás recordatorios de tus sesiones.", Toast.LENGTH_LONG).show();
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
}

