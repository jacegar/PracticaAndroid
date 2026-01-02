package com.example.practicaandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "AppSettings";
    private static final String LANGUAGE_KEY = "language";
    private static final String WEIGHT_UNIT_KEY = "weight_unit";

    private AutoCompleteTextView languageAutoComplete;
    private AutoCompleteTextView weightUnitAutoComplete;
    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        languageAutoComplete = view.findViewById(R.id.languageAutoComplete);
        weightUnitAutoComplete = view.findViewById(R.id.weightUnitAutoComplete);

        setupLanguageSelector();
        setupWeightUnitSelector();

        return view;
    }

    private void setupLanguageSelector() {
        // Create language options
        String[] languages = new String[]{
                getString(R.string.language_spanish),
                getString(R.string.language_english)
        };

        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );

        languageAutoComplete.setAdapter(adapter);

        // Set current language
        String currentLanguage = getCurrentLanguageCode();
        if (currentLanguage.equals("es")) {
            languageAutoComplete.setText(getString(R.string.language_spanish), false);
        } else {
            languageAutoComplete.setText(getString(R.string.language_english), false);
        }

        // Set listener for language selection
        languageAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLanguage = (String) parent.getItemAtPosition(position);
            String languageCode;

            if (selectedLanguage.equals(getString(R.string.language_spanish))) {
                languageCode = "es";
            } else {
                languageCode = "en";
            }

            // Only apply if different from current
            if (!languageCode.equals(getCurrentLanguageCode())) {
                // Save language preference
                saveLanguagePreference(languageCode);

                // Apply language change
                setLocale(languageCode);

                // Show confirmation message
                Toast.makeText(requireContext(), R.string.language_changed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getCurrentLanguageCode() {
        return sharedPreferences.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireActivity().getResources().updateConfiguration(config,
                requireActivity().getResources().getDisplayMetrics());

        // Restart activity to apply changes, staying in settings fragment
        android.content.Intent intent = requireActivity().getIntent();
        intent.putExtra("open_settings", true);
        requireActivity().finish();
        requireActivity().startActivity(intent);
    }

    private void setupWeightUnitSelector() {
        // Create weight unit options
        String[] weightUnits = new String[]{
                getString(R.string.weight_unit_kg),
                getString(R.string.weight_unit_lb)
        };

        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                weightUnits
        );

        weightUnitAutoComplete.setAdapter(adapter);

        // Set current weight unit
        String currentWeightUnit = getCurrentWeightUnit();
        if (currentWeightUnit.equals("kg")) {
            weightUnitAutoComplete.setText(getString(R.string.weight_unit_kg), false);
        } else {
            weightUnitAutoComplete.setText(getString(R.string.weight_unit_lb), false);
        }

        // Set listener for weight unit selection
        weightUnitAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedWeightUnit = (String) parent.getItemAtPosition(position);
            String weightUnitCode;

            if (selectedWeightUnit.equals(getString(R.string.weight_unit_kg))) {
                weightUnitCode = "kg";
            } else {
                weightUnitCode = "lb";
            }

            // Only apply if different from current
            if (!weightUnitCode.equals(getCurrentWeightUnit())) {
                // Save weight unit preference
                saveWeightUnitPreference(weightUnitCode);

                // Update the displayed text
                weightUnitAutoComplete.setText(selectedWeightUnit, false);

                // Show confirmation message
                Toast.makeText(requireContext(),
                        getString(R.string.weight_unit) + ": " + weightUnitCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentWeightUnit() {
        return sharedPreferences.getString(WEIGHT_UNIT_KEY, "kg");
    }

    private void saveWeightUnitPreference(String weightUnit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WEIGHT_UNIT_KEY, weightUnit);
        editor.apply();
    }

    public static String getWeightUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(WEIGHT_UNIT_KEY, "kg");
    }
}