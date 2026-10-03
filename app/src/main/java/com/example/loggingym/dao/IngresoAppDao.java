package com.example.loggingym.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.example.loggingym.IngresoApp;

@Dao
public interface IngresoAppDao {

    @Insert
    void registrarIngreso(IngresoApp ingreso);
}
