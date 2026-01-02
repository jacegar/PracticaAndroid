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

    private AutoCompleteTextView languageAutoComplete;
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

        setupLanguageSelector();

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

            // Save language preference
            saveLanguagePreference(languageCode);

            // Apply language change
            setLocale(languageCode);

            // Show confirmation message
            Toast.makeText(requireContext(), R.string.language_changed, Toast.LENGTH_LONG).show();
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
}