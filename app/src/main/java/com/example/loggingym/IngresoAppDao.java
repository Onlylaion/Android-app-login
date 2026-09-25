package com.example.loggingym;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface IngresoAppDao {

    @Insert
    void registrarIngreso(IngresoApp ingreso);
}
