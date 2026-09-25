package com.example.loggingym;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Usuario.class, IngresoApp.class, Pago.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract UsuarioDao usuarioDao();
    public abstract IngresoAppDao ingresoAppDao();
    public abstract PagoDao pagoDao();

    public static AppDatabase obtenerInstancia(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "loggingym_db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                    precargarUsuariosDePrueba(instancia);
                }
            }
        }
        return instancia;
    }

    private static void precargarUsuariosDePrueba(AppDatabase db) {
        new Thread(() -> {
            if (db.usuarioDao().validarLogin("12345678", "1234") == null) {
                db.usuarioDao().insertar(new Usuario("12345678", "1234", "León"));
            }
            if (db.usuarioDao().validarLogin("87654321", "1212") == null) {
                db.usuarioDao().insertar(new Usuario("87654321", "1212", "Leon"));
            }
        }).start();
    }
}
