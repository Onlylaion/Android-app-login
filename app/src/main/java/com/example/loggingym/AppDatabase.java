package com.example.loggingym;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.loggingym.dao.IngresoAppDao;
import com.example.loggingym.dao.PagoDao;
import com.example.loggingym.dao.UsuarioDao;

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
                }
            }
        }
        return instancia;
    }
}
