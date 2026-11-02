package com.example.loggingym.api;

// Respuesta de POST /registrar: {ok, estado} si es 200/500, {error} si es 400/409
public class RegistroResponse {
    public boolean ok;
    public String estado;
    public String error;
}
