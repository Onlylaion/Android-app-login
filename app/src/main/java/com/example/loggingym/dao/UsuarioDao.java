package com.example.loggingym.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.loggingym.Usuario;

@Dao
public interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE dni = :dni AND contrasenia = :contrasenia LIMIT 1")
    Usuario validarLogin(String dni, String contrasenia);

    @Insert
    void insertar(Usuario usuario);
}
