package com.example.loggingym;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

public final class SesionHelper {

    private static final String PREFS_NOMBRE = "loggingym_prefs";
    private static final String CLAVE_USUARIO_DNI = "usuario_dni";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_CANTIDAD_PERSONAS_GYM = "cantidad_personas_gym";

    private SesionHelper() {
    }

    public static void saveUsuario(Context context, String dni) {
        obtenerPrefs(context).edit()
                .putString(CLAVE_USUARIO_DNI, dni)
                .apply();
    }

    public static void guardarToken(Context context, String token) {
        obtenerPrefs(context).edit()
                .putString(CLAVE_TOKEN, token)
                .apply();
    }

    public static String obtenerToken(Context context) {
        return obtenerPrefs(context).getString(CLAVE_TOKEN, null);
    }

    public static String obtenerUsuarioActual(Context context) {
        return obtenerPrefs(context).getString(CLAVE_USUARIO_DNI, null);
    }

    public static boolean haySesionActiva(Context context) {
        return obtenerUsuarioActual(context) != null && !tokenVencido(obtenerToken(context));
    }

    /**
     * Lee el campo "exp" del JWT (sin verificar la firma, eso lo hace el servidor)
     * para no entrar al menú con un token que el servidor va a rechazar.
     */
    private static boolean tokenVencido(String token) {
        if (token == null) {
            return true;
        }
        try {
            String payload = token.split("\\.")[1];
            byte[] json = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            long exp = new JSONObject(new String(json)).getLong("exp");
            return System.currentTimeMillis() / 1000 >= exp;
        } catch (Exception e) {
            return true;
        }
    }

    public static void cerrarSesion(Context context) {
        obtenerPrefs(context).edit()
                .remove(CLAVE_USUARIO_DNI)
                .remove(CLAVE_TOKEN)
                .apply();
    }

    /** Cierra la sesión y vuelve al login, limpiando el historial de pantallas. */
    public static void cerrarSesionYVolverAlLogin(Activity activity) {
        cerrarSesion(activity);
        MQTTManager.obtener(activity).desconectar();
        Intent intento = new Intent(activity, MainActivity.class);
        intento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intento);
        activity.finish();
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
