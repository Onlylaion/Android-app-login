package com.example.loggingym.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegistroApi {

    @POST("registrar")
    Call<RegistroResponse> registrar(@Body RegistroRequest datos);
}
