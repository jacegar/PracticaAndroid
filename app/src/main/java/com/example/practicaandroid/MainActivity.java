package com.example.practicaandroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.practicaandroid.data.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        setCurrentFragment(workoutFragment);

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
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}

