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

    private static final int COLOR_OK = Color.parseColor("#2E7D32");
    private static final int COLOR_AVISO = Color.parseColor("#EF6C00");

    private String dni = null;
    private TextView textViewNumeroDni;
    private TextView textViewEstado;
    private Button btnComandoAbrir;

    private boolean sesionValida = false;
    private boolean conectado = false;
    private boolean esperandoConfirmacion = false;

    private final MQTTManager.EstadoListener listenerEstado = this::mostrarEstadoConexion;

    // solo reacciona si el usuario pidió abrir: evita mostrar un "Puerta abierta" viejo
    private final MQTTManager.Listener listenerApertura = (topic, payload) -> {
        if (!esperandoConfirmacion) {
            return;
        }
        esperandoConfirmacion = false;
        mostrarPuertaAbierta();
        actualizarBoton();
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
        textViewEstado = findViewById(R.id.textViewEstado);
        btnComandoAbrir = findViewById(R.id.btnComandoAbrir);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MQTTManager mqtt = MQTTManager.obtener(this);
        esperandoConfirmacion = false;
        mqtt.conectar();
        mqtt.agregarEstadoListener(listenerEstado);
        mqtt.agregarListener(MQTTManager.TOPIC_PUERTA_ESTADO, listenerApertura);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MQTTManager mqtt = MQTTManager.obtener(this);
        mqtt.quitarEstadoListener(listenerEstado);
        mqtt.quitarListener(MQTTManager.TOPIC_PUERTA_ESTADO, listenerApertura);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.dni = SesionHelper.obtenerUsuarioActual(this);
        sesionValida = false;

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
        sesionValida = true;
        actualizarBoton();
    }

    public void abrirPuerta(View v) {
        if (!MQTTManager.obtener(this).publicar(MQTTManager.TOPIC_COMANDO, dni)) {
            mostrarEstado("No se pudo enviar: sin conexión", Color.RED);
            return;
        }
        esperandoConfirmacion = true;
        mostrarEstado("Abriendo puerta…", COLOR_AVISO);
        actualizarBoton();
    }

    private void mostrarEstadoConexion(MQTTManager.Estado estado) {
        conectado = estado == MQTTManager.Estado.CONECTADO;
        switch (estado) {
            case CONECTANDO:
                mostrarEstado("Conectando con el servidor…", Color.GRAY);
                break;
            case CONECTADO:
                mostrarEstado("Conectado", COLOR_OK);
                break;
            case RECONECTANDO:
                mostrarEstado("Conexión perdida, reconectando…", COLOR_AVISO);
                break;
            default:
                mostrarEstado("Sin conexión con el servidor", Color.RED);
        }
        actualizarBoton();
    }

    public void mostrarPuertaAbierta() {
        mostrarEstado("Puerta abierta", COLOR_OK);
    }

    private void mostrarEstado(String mensaje, int color) {
        textViewEstado.setText(mensaje);
        textViewEstado.setTextColor(color);
    }

    // "Abrir" solo se puede tocar con sesión válida, conexión y sin otro pedido en curso
    private void actualizarBoton() {
        btnComandoAbrir.setEnabled(sesionValida && conectado && !esperandoConfirmacion);
    }

    private void mostrarAvisoSesionFallida(String mensaje) {
        textViewNumeroDni.setText(mensaje);
        textViewNumeroDni.setTextColor(Color.RED);
        actualizarBoton();
    }

    public void backToMenuFromAperturaPuerta(View v) {
        finish();
    }
}