package com.example.loggingym.api.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardResponse {

    @SerializedName("estado_puerta")
    public EstadoPuerta estadoPuerta;

    @SerializedName("ultimos_eventos")
    public List<Evento> ultimosEventos;

    public static class EstadoPuerta {
        public String tipo;   // ABIERTA, CERRADA o DESCONOCIDO
        public String fecha;  // ISO 8601, puede ser null
    }

    public static class Evento {
        public String tipo;
        public String fecha;
        public String dni;    // null si el evento lo informó el ESP32
    }
}
