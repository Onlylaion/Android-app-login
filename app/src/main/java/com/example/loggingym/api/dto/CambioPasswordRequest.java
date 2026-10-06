package com.example.loggingym.api.dto;

import com.google.gson.annotations.SerializedName;

public class CambioPasswordRequest {

    @SerializedName("password_actual")
    public final String passwordActual;

    @SerializedName("password_nueva")
    public final String passwordNueva;

    public CambioPasswordRequest(String passwordActual, String passwordNueva) {
        this.passwordActual = passwordActual;
        this.passwordNueva = passwordNueva;
    }
}
