package com.example.loggingym.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://soa.alfatechnologies.com.ar/";

    private static volatile ApiClient instancia;

    private final LoginApi loginApi;
    private final RegistroApi registroApi;

    private ApiClient() {
        OkHttpClient cliente = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        loginApi = retrofit.create(LoginApi.class);
        registroApi = retrofit.create(RegistroApi.class);
    }

    public static ApiClient obtener() {
        if (instancia == null) {
            synchronized (ApiClient.class) {
                if (instancia == null) {
                    instancia = new ApiClient();
                }
            }
        }
        return instancia;
    }

    public LoginApi obtenerLoginApi() {
        return loginApi;
    }

    public RegistroApi obtenerRegistroApi() {
        return registroApi;
    }
}
