package com.example.loggingym;

import android.content.Context;
import android.content.SharedPreferences;

public final class SesionHelper {

    private static final String PREFS_NOMBRE = "loggingym_prefs";
    private static final String CLAVE_USUARIO_DNI = "usuario_dni";
    private static final String CLAVE_CANTIDAD_PERSONAS_GYM = "cantidad_personas_gym";
    private static final String CLAVE_USUARIO_UBI = "usuario_ubi";
    private static final String CLAVE_MODO_OSCURO = "modo_oscuro";
    private SesionHelper() {
    }

    public static void saveModoOscuro(Context context, boolean activo) {
        obtenerPrefs(context).edit()
                .putBoolean(CLAVE_MODO_OSCURO, activo)
                .apply();
    }

    public static boolean modoOscuroActivo(Context context) {
        return obtenerPrefs(context).getBoolean(CLAVE_MODO_OSCURO, false);   // por defecto, modo claro
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

    public static void saveDentroGeocerca(Context context, boolean dentro) {
        obtenerPrefs(context).edit()
                .putString(CLAVE_USUARIO_UBI, String.valueOf(dentro))
                .apply();
    }

    public static boolean dentroDeGeocerca(Context context) {
        String res = obtenerPrefs(context).getString(CLAVE_USUARIO_UBI, null);

        return res != null && !res.equals("false");
    }

    private static SharedPreferences obtenerPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
    }
}
