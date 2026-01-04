package com.example.practicaandroid.util;

public class TipoEjercicioSpinnerItem {
    private final String claveDb;
    private final String nombreVisible;

    public TipoEjercicioSpinnerItem(String claveDb, String nombreVisible) {
        this.claveDb = claveDb;
        this.nombreVisible = nombreVisible;
    }

    public String getClaveDb() {
        return claveDb;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
