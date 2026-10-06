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

public class AperturaPuerta extends AppCompatActivity {

    private String dni = null;
    private TextView textViewNumeroDni;
    private Button btnComandoAbrir;

    private final MQTTManager.Listener listenerApertura = (topic, payload) -> {
      try {
          mostrarPuertaAbierta();
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
    };


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
    protected void onStart() {
        super.onStart();
        MQTTManager mqtt = MQTTManager.obtener(this);
        mqtt.conectar();
        mqtt.agregarListener(MQTTManager.TOPIC_PUERTA_ESTADO, listenerApertura);
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
        MQTTManager.obtener(this).publicar(MQTTManager.TOPIC_COMANDO, dni);
    }

    public void mostrarPuertaAbierta() {
        textViewNumeroDni.setText("Abriendo puerta");
        textViewNumeroDni.setTextColor(Color.GREEN);
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