package com.example.loggingym.api;

import android.content.Context;

import com.example.loggingym.SesionHelper;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "https://soagym.alfatechnologies.com.ar/";

    private static volatile UserApi userApi;

    private ApiClient() {
    }

    public static UserApi obtener(Context context) {
        if (userApi == null) {
            synchronized (ApiClient.class) {
                if (userApi == null) {
                    userApi = crearRetrofit(context.getApplicationContext()).create(UserApi.class);
                }
            }
        }
        return userApi;
    }

    private static Retrofit crearRetrofit(Context appContext) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient cliente = new OkHttpClient.Builder()
                // agrega "Authorization: Bearer <token>" si hay sesión iniciada
                .addInterceptor(chain -> {
                    String token = SesionHelper.obtenerToken(appContext);
                    Request original = chain.request();
                    if (token == null) {
                        return chain.proceed(original);
                    }
                    return chain.proceed(original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build());
                })
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** 401 = token inválido o vencido, 403 = usuario dado de baja: hay que volver a loguearse. */
    public static boolean sesionRechazada(Response<?> respuesta) {
        return respuesta.code() == 401 || respuesta.code() == 403;
    }

    /**
     * Extrae el mensaje de error que manda FastAPI ({"detail": "..."}).
     * Si no se puede leer, devuelve el mensaje por defecto.
     */
    public static String mensajeDeError(Response<?> respuesta, String porDefecto) {
        try (ResponseBody cuerpo = respuesta.errorBody()) {
            if (cuerpo == null) {
                return porDefecto;
            }
            Object detalle = new JSONObject(cuerpo.string()).opt("detail");
            // en errores de validación (422) "detail" es una lista, no un texto
            return detalle instanceof String ? (String) detalle : porDefecto;
        } catch (Exception e) {
            return porDefecto;
        }
    }
}
