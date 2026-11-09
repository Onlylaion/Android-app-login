package com.example.loggingym;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class GymApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        aplicarTema(SesionHelper.modoOscuroActivo(this));   // el tema guardado se aplica antes de abrir cualquier pantalla
    }

    // Si el modo cambia, Android recrea las pantallas abiertas con el tema nuevo
    public static void aplicarTema(boolean modoOscuro) {
        AppCompatDelegate.setDefaultNightMode(modoOscuro
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
