package com.example.loggingym;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import android.util.Log;

public class cantidadPersonasGym extends AppCompatActivity {

    private static final int CAPACIDAD_MAXIMA = 50;
    private static final double UMBRAL_AMARILLO = 0.6;
    private static final double UMBRAL_ROJO = 0.85;

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

        TextView textoCantidad = findViewById(R.id.texto_cantidad);
        ProgressBar barraOcupacion = findViewById(R.id.barra_ocupacion);
        barraOcupacion.setMax(CAPACIDAD_MAXIMA);

        actualizarVista(textoCantidad, barraOcupacion, 0);

        String brokerUri = "tcp://127.0.0.1:1883";
        String clientId = "AppGym_" + System.currentTimeMillis();

        MqttAndroidClient mqttClient = new MqttAndroidClient(getApplicationContext(), brokerUri, clientId);

        mqttClient.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d("MQTT", "Conectado al broker");
                mqttClient.subscribe("gimnasio/ocupacion/cantidad", 0);
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e("MQTT", "Fallo la conexión", exception);
            }
        });

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                Log.d("MQTT", "Llegó: " + payload + " en topic: " + topic);
                try {
                    int lecturaSensor = Integer.parseInt(payload);
                    runOnUiThread(() -> actualizarVista(textoCantidad, barraOcupacion, lecturaSensor));
                } catch (NumberFormatException e) {
                    Log.e("MQTT", "Payload no numérico: " + payload, e);
                }
            }
            @Override public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Se perdió la conexión", cause);
            }
            @Override public void deliveryComplete(IMqttDeliveryToken token) { }
        });
    }
    public void Regresar(View v){
        Intent intento = new Intent(this, ActividadMenu.class);
        startActivity(intento);
    }

    // total = lo que reporta el sensor + la gente logueada en la app ahora mismo
    private void actualizarVista(TextView textoCantidad, ProgressBar barraOcupacion, int lecturaSensor) {
        int total = lecturaSensor + SesionHelper.obtenerPersonasLogueadas(this);
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