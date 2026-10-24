package com.example.loggingym.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LoginApi {

    @GET("login")
    Call<LoginResponse> login(@Query("dni") String dni, @Query("clave") String clave);
}
