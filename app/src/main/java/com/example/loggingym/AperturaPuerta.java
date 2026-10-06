package com.example.loggingym;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loggingym.api.ApiClient;
import com.example.loggingym.api.dto.ResultadoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AperturaPuerta extends AppCompatActivity {

    private String dni = null;
    private TextView textViewNumeroDni;
    private Button btnComandoAbrir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apertura_puerta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewNumeroDni = findViewById(R.id.textViewNroDni);
        btnComandoAbrir = findViewById(R.id.btnComandoAbrir);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.dni = SesionHelper.obtenerUsuarioActual(this);

        if (this.dni == null) {
            mostrarAvisoSesionFallida("Sesión no encontrada");
            return;
        }

        if (this.dni.length() >= 9) {
            mostrarAvisoSesionFallida("DNI inválido");
            return;
        }

        textViewNumeroDni.setText(dni);
        textViewNumeroDni.setTextColor(Color.BLACK);
        btnComandoAbrir.setEnabled(true);
    }

    public void abrirPuerta(View v) {
        btnComandoAbrir.setEnabled(false);
        ApiClient.obtener(this).abrirPuerta().enqueue(new Callback<ResultadoResponse>() {
            @Override
            public void onResponse(Call<ResultadoResponse> call, Response<ResultadoResponse> respuesta) {
                if (respuesta.isSuccessful()) {
                    mostrarResultado("Abriendo puerta", Color.GREEN);
                } else if (ApiClient.sesionRechazada(respuesta)) {
                    SesionHelper.cerrarSesionYVolverAlLogin(AperturaPuerta.this);
                    return;
                } else {
                    // 503 = el servidor no tiene conexión con el broker MQTT
                    mostrarResultado(ApiClient.mensajeDeError(respuesta, "No se pudo abrir la puerta"), Color.RED);
                }
                btnComandoAbrir.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResultadoResponse> call, Throwable t) {
                mostrarResultado("No se pudo conectar con el servidor", Color.RED);
                btnComandoAbrir.setEnabled(true);
            }
        });
    }

    private void mostrarResultado(String mensaje, int color) {
        textViewNumeroDni.setText(mensaje);
        textViewNumeroDni.setTextColor(color);
    }

    private void mostrarAvisoSesionFallida(String mensaje) {
        textViewNumeroDni.setText(mensaje);
        textViewNumeroDni.setTextColor(Color.RED);
        btnComandoAbrir.setEnabled(false);
    }

    public void backToMenuFromAperturaPuerta(View v) {
        finish();
    }
}