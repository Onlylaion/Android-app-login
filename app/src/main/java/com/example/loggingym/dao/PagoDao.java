package com.example.loggingym.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.loggingym.Pago;

import java.util.List;

@Dao
public interface PagoDao {

    @Insert
    void insertar(Pago pago);

    @Query("SELECT * FROM pagos WHERE usuarioDni = :dni ORDER BY fecha DESC")
    List<Pago> listarPorUsuario(String dni);
}
