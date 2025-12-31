package com.example.practicaandroid.data.ejercicio;

public enum TipoEjercicio {
    FUERZA("Fuerza"),
    CARDIO("Cardio"),
    FLEXIBILIDAD("Flexibilidad");

    private final String nombre;

    TipoEjercicio(String nombre){
        this.nombre = nombre;
    }

    public String getNombre(){
        return nombre;
    }

    public static TipoEjercicio fromString(String text){
        for(TipoEjercicio tipo : TipoEjercicio.values()){
            if(tipo.nombre.equalsIgnoreCase(text)){
                return tipo;
            }
        }
        return FUERZA; //Valor por defecto
    }
}
