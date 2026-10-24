package com.example.loggingym.api;

// Respuesta de GET /login: {ok, nombre, estado} si es 200, {error} si es 400/401
public class LoginResponse {
    public boolean ok;
    public String nombre;
    public boolean estado;
    public String error;
}
