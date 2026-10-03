package com.example.loggingym;

import android.content.Context;
import android.content.SharedPreferences;

public final class SesionHelper {

    private static final String PREFS_NOMBRE = "loggingym_prefs";
    private static final String CLAVE_USUARIO_DNI = "usuario_dni";
    private static final String CLAVE_CANTIDAD_PERSONAS_GYM = "cantidad_personas_gym";

    private SesionHelper() {
    }

    public static void saveUsuario(Context context, String dni) {
        obtenerPrefs(context).edit()
                .putString(CLAVE_USUARIO_DNI, dni)
                .apply();
    }

    public static String obtenerUsuarioActual(Context context) {
        return obtenerPrefs(context).getString(CLAVE_USUARIO_DNI, null);
    }

    public static boolean haySesionActiva(Context context) {
        return obtenerUsuarioActual(context) != null;
    }

    public static void cerrarSesion(Context context) {
        obtenerPrefs(context).edit()
                .remove(CLAVE_USUARIO_DNI)
                .apply();
    }

    public static void sumarPersonaEnGym(Context context) {
        int actual = obtenerPersonasEnGym(context);
        obtenerPrefs(context).edit()
                .putInt(CLAVE_CANTIDAD_PERSONAS_GYM, actual + 1)
                .apply();
    }

    public static void restarPersonaEnGym(Context context) {
        int actual = obtenerPersonasEnGym(context);
        obtenerPrefs(context).edit()
                .putInt(CLAVE_CANTIDAD_PERSONAS_GYM, Math.max(0, actual - 1))
                .apply();
    }

    public static int obtenerPersonasEnGym(Context context) {
        return obtenerPrefs(context).getInt(CLAVE_CANTIDAD_PERSONAS_GYM, 0);
    }

    private static SharedPreferences obtenerPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
    }
}
