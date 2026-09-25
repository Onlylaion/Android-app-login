package com.example.loggingym;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pagos")
public class Pago {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String usuarioDni;
    public double monto;
    public long fecha;
    public String concepto;

    public Pago(String usuarioDni, double monto, long fecha, String concepto) {
        this.usuarioDni = usuarioDni;
        this.monto = monto;
        this.fecha = fecha;
        this.concepto = concepto;
    }
}
