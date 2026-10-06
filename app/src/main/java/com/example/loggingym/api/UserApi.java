package com.example.loggingym.api;

import com.example.loggingym.api.dto.CambioPasswordRequest;
import com.example.loggingym.api.dto.DashboardResponse;
import com.example.loggingym.api.dto.LoginRequest;
import com.example.loggingym.api.dto.LoginResponse;
import com.example.loggingym.api.dto.ResultadoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Endpoints del backend. El token Bearer lo agrega ApiClient automáticamente
 * en los endpoints protegidos.
 */
public interface UserApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("auth/cambiar-password")
    Call<ResultadoResponse> cambiarPassword(@Body CambioPasswordRequest body);

    @POST("puerta/abrir")
    Call<ResultadoResponse> abrirPuerta();

    @GET("dashboard")
    Call<DashboardResponse> dashboard();
}
