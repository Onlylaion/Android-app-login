package com.example.loggingym;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CantidadPersonasGym extends AppCompatActivity {

    private static final int CAPACIDAD_MAXIMA = 50;
    private static final double UMBRAL_AMARILLO = 0.6;
    private static final double UMBRAL_ROJO = 0.85;

    private TextView textoCantidad;
    private ProgressBar barraOcupacion;

    private final MQTTManager.Listener listenerOcupacion = (topic, payload) -> {
        try {
            actualizarVista(Integer.parseInt(payload));
        } catch (NumberFormatException e) {
            Log.e("MQTT", "Payload no numérico: " + payload, e);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cantidad_personas_gym);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textoCantidad = findViewById(R.id.texto_cantidad);
        barraOcupacion = findViewById(R.id.barra_ocupacion);
        barraOcupacion.setMax(CAPACIDAD_MAXIMA);

        actualizarVista(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MQTTManager mqtt = MQTTManager.obtener(this);
        mqtt.conectar();
        mqtt.agregarListener(MQTTManager.TOPIC_OCUPACION, listenerOcupacion);
    }

    @Override
    protected void onStop() {
        MQTTManager.obtener(this).quitarListener(MQTTManager.TOPIC_OCUPACION, listenerOcupacion);
        super.onStop();
    }
    public void regresar(View v){
        /*Intent intento = new Intent(this, ActividadMenu.class);
        startActivity(intento);*/
        finish();
    }

    // total = lo que reporta el sensor + la gente logueada en la app ahora mismo
    private void actualizarVista(int lecturaSensor) {
        int total = lecturaSensor + SesionHelper.obtenerPersonasEnGym(this);
        textoCantidad.setText(total + " / " + CAPACIDAD_MAXIMA);
        barraOcupacion.setProgress(total);
        barraOcupacion.setProgressTintList(ColorStateList.valueOf(colorSegunOcupacion(total)));
    }

    // menos de 60% ocupado: verde, entre 60% y 85%: amarillo, más de 85%: rojo
    private int colorSegunOcupacion(int cantidad) {
        double porcentaje = (double) cantidad / CAPACIDAD_MAXIMA;
        if (porcentaje >= UMBRAL_ROJO) {
            return Color.parseColor("#F44336");
        } else if (porcentaje >= UMBRAL_AMARILLO) {
            return Color.parseColor("#FFC107");
        } else {
            return Color.parseColor("#4CAF50");
        }
    }
}