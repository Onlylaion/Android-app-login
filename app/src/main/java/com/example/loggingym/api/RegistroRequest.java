package com.example.loggingym.api;

// Body de POST /registrar
public class RegistroRequest {
    public String dni;
    public String clave;
    public String nombre;
    public String apellido;

    public RegistroRequest(String dni, String clave, String nombre, String apellido) {
        this.dni = dni;
        this.clave = clave;
        this.nombre = nombre;
        this.apellido = apellido;
    }
}
