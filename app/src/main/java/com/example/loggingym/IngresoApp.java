package com.example.loggingym;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingresos_app")
public class IngresoApp {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String usuarioDni;

    public String usuarioNombre;

    public long timestamp;

    public IngresoApp(String usuarioDni, String usuarioNombre, long timestamp) {
        this.usuarioDni = usuarioDni;
        this.usuarioNombre = usuarioNombre;
        this.timestamp = timestamp;
    }
}
