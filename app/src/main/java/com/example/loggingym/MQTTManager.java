package com.example.loggingym;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.os.HandlerThread;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import info.mqtt.android.service.MqttAndroidClient;

public final class MQTTManager {

    private static final String TAG = "MQTT";
    private static final String BROKER_URI = "wss://soa.alfatechnologies.com.ar/mqtt";
    private static final String PREFS_NOMBRE = "mqtt_prefs";
    private static final String CLAVE_CLIENT_ID = "client_id";

    public static final String TOPIC_COMANDO = "gimnasio/puerta/abrirPuerta";

    public static final String TOPIC_OCUPACION = "gimnasio/cantidadPersonas";

    private static final String[] TOPICS_SUSCRIPCION = {TOPIC_OCUPACION};

    public interface Listener {
        void onMensaje(String topic, String payload);
    }

    private static MQTTManager instancia;

    private final MqttAndroidClient cliente;
    private final Map<String, List<Listener>> listenersPorTopic = new ConcurrentHashMap<>();
    private final Map<String, String> ultimoValor = new ConcurrentHashMap<>();
    private final HandlerThread hiloMqtt = new HandlerThread("MQTTManager-Hilo");
    private final Handler hiloSecundario;
    private volatile boolean conectando = false;



    public static synchronized MQTTManager obtener(Context context) {
        if (instancia == null) {
            instancia = new MQTTManager(context.getApplicationContext());
        }
        return instancia;
    }

    private static String obtenerClientId(Context appContext) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
        String id = prefs.getString(CLAVE_CLIENT_ID, null);
        if (id == null) {
            id = "AppGym_" + UUID.randomUUID();
            prefs.edit().putString(CLAVE_CLIENT_ID, id).apply();
        }
        return id;
    }

    private MQTTManager(Context appContext) {
        hiloMqtt.start();
        hiloSecundario = new Handler(hiloMqtt.getLooper());
        cliente = new MqttAndroidClient(appContext, BROKER_URI, obtenerClientId(appContext));

        cliente.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconexion, String serverURI) {
                Log.d(TAG, reconexion ? "Reconectado al broker" : "Conectado al broker");
                for (String topic : TOPICS_SUSCRIPCION) {
                    cliente.subscribe(topic, 0);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Llegó: " + payload + " en topic: " + topic);
                ultimoValor.put(topic, payload);

                List<Listener> listeners = listenersPorTopic.get(topic);
                if (listeners == null) {
                    return;
                }
                hiloSecundario.post(() -> {
                    for (Listener l : listeners) {
                        l.onMensaje(topic, payload);
                    }
                });
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "Se perdió la conexión", cause);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void conectar() {
        if (cliente.isConnected() || conectando) {
            return;
        }
        conectando = true;

        MqttConnectOptions opciones = new MqttConnectOptions();
        opciones.setAutomaticReconnect(true);
        opciones.setCleanSession(true);

        cliente.connect(opciones, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                conectando = false;
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                conectando = false;
                Log.e(TAG, "Fallo la conexión", exception);
            }
        });
    }

    public void desconectar() {
        if (cliente.isConnected()) {
            cliente.disconnect();
        }
        ultimoValor.clear();
    }

    public void publicar(String topic, String payload) {
        if (!cliente.isConnected()) {
            Log.w(TAG, "No se publicó en " + topic + ": sin conexión");
            return;
        }
        cliente.publish(topic, new MqttMessage(payload.getBytes()));
        Log.d(TAG, "Publicado en " + topic + ": " + payload);
    }

    public void agregarListener(String topic, Listener listener) {
        listenersPorTopic
                .computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>())
                .add(listener);

        String ultimo = ultimoValor.get(topic);
        if (ultimo != null) {
            hiloSecundario.post(() -> listener.onMensaje(topic, ultimo));
        }
    }

    public void quitarListener(String topic, Listener listener) {
        List<Listener> listeners = listenersPorTopic.get(topic);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
}
