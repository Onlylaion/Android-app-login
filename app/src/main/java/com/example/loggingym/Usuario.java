package com.example.loggingym;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey
    @NonNull
    public String dni;

    public String contrasenia;

    public String nombre;

    public Usuario(@NonNull String dni, String contrasenia, String nombre) {
        this.dni = dni;
        this.contrasenia = contrasenia;
        this.nombre = nombre;
    }
}
