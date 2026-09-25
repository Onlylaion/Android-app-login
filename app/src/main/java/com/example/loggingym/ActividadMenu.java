package com.example.loggingym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import android.util.Log;

public class ActividadMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String brokerUri = "tcp://127.0.0.1:1883";
        String clientId = "AppGym_" + System.currentTimeMillis();

        MqttAndroidClient mqttClient = new MqttAndroidClient(getApplicationContext(), brokerUri, clientId);

        mqttClient.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d("MQTT", "Conectado al broker");
                mqttClient.subscribe("gimnasio/puerta/estado", 0);
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
            }
            @Override public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Se perdió la conexión", cause);
            }
            @Override public void deliveryComplete(IMqttDeliveryToken token) { }
        });
    }

    public void salir(View v){
        SesionHelper.cerrarSesion(this);
        SesionHelper.restarPersonaLogueada(this);
        Intent intento = new Intent(this, MainActivity.class);
        intento.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intento);
        finish();
    }
    public void verEstadistica(View v){
        Intent intento = new Intent(this, cantidadPersonasGym.class);
        startActivity(intento);
    }
    public void verPagos(View v){
        Intent intento = new Intent(this, verPagos.class);
        startActivity(intento);
    }
}