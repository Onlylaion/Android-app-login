package com.example.loggingym;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class AperturaPuerta extends AppCompatActivity {

    private static final int COLOR_OK = Color.parseColor("#2E7D32");     // verde para estados correctos
    private static final int COLOR_AVISO = Color.parseColor("#EF6C00");  // naranja para avisos

    // Ubicación de la puerta (por ahora fija; más adelante la puede informar la ESP32)
    private static final double PUERTA_LAT = -34.729427;    // la ESP32 está en lo de Nicolás
    private static final double PUERTA_LNG = -58.646709;
    private static final float RADIO_METROS = 100f;         // por debajo de 100 m la geocerca falla seguido
    private static final String ID_GEOCERCA = "PUERTA_GYM"; // nombre para identificar la geocerca

    private String dni = null;                  // DNI del usuario logueado
    private TextView textViewNumeroDni;         // muestra el DNI en pantalla
    private TextView textViewEstado;            // muestra el estado de la conexión MQTT
    private Button btnComandoAbrir;             // botón para abrir la puerta

    private boolean sesionValida = false;           // hay un usuario logueado con DNI válido
    private boolean conectado = false;              // hay conexión con el broker MQTT
    private boolean esperandoConfirmacion = false;  // ya se mandó un pedido y se espera respuesta

    private ActivityResultLauncher<String[]> pedirUbicacion;    // pide ubicación precisa y aproximada juntas
    private ActivityResultLauncher<String> pedirSegundoPlano;   // pide ubicación en segundo plano (va aparte)

    // MQTTManager avisa desde otro hilo, por eso las vistas se actualizan en el hilo principal
    private final MQTTManager.EstadoListener listenerEstado =
            estado -> runOnUiThread(() -> mostrarEstadoConexion(estado));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                                      // la pantalla ocupa todo, incluso bajo las barras
        setContentView(R.layout.activity_apertura_puerta);            // carga el diseño XML
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // evita que el contenido quede tapado
            return insets;
        });

        textViewNumeroDni = findViewById(R.id.textViewNroDni);       // vincula las vistas del XML
        textViewEstado = findViewById(R.id.textViewEstado);
        btnComandoAbrir = findViewById(R.id.btnComandoAbrir);

        registrarLanzadoresDePermisos();   // los lanzadores tienen que crearse en onCreate, si no la app se cae
        verificarPermisos();               // apenas se abre la pantalla, pedimos lo que falte
    }

    @Override
    protected void onStart() {
        super.onStart();
        MQTTManager mqtt = MQTTManager.obtener(this);   // instancia única del cliente MQTT
        esperandoConfirmacion = false;
        mqtt.conectar();                                // se conecta al broker
        mqtt.agregarEstadoListener(listenerEstado);     // escucha cambios de conexión
    }

    @Override
    protected void onStop() {
        super.onStop();
        MQTTManager.obtener(this).quitarEstadoListener(listenerEstado); // deja de escuchar al salir de la pantalla
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.dni = SesionHelper.obtenerUsuarioActual(this);   // lee el usuario guardado al loguearse
        sesionValida = false;

        if (this.dni == null) {                               // nadie logueado
            mostrarAvisoSesionFallida("Sesión no encontrada");
            return;
        }

        if (this.dni.length() >= 9) {                         // un DNI argentino no tiene más de 8 dígitos
            mostrarAvisoSesionFallida("DNI inválido");
            return;
        }

        textViewNumeroDni.setText(dni);
        textViewNumeroDni.setTextColor(ContextCompat.getColor(this, R.color.texto)); // color del tema (se ve en modo claro y oscuro)
        sesionValida = true;
        actualizarBoton();
    }

    // ---------------- Permisos de ubicación ----------------

    private void registrarLanzadoresDePermisos() {
        // Primera tanda: precisa + aproximada
        pedirUbicacion = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                resultado -> {                                                       // respuesta del usuario
                    Boolean fina = resultado.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (fina != null && fina) {                                      // aceptó la precisa
                        pedirPermisoSegundoPlano();                                  // seguimos con segundo plano
                    } else {                                                         // eligió aproximada o rechazó
                        Toast.makeText(this, "Necesitás dar ubicación PRECISA", Toast.LENGTH_LONG).show();
                    }
                });

        // Segunda tanda: segundo plano (Android no deja pedirlo junto con las otras)
        pedirSegundoPlano = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {                                                       // vuelve de Ajustes con la respuesta
                    if (concedido) {
                        permisosListos();                                            // tenemos todo
                    } else {
                        Toast.makeText(this, "Elegí 'Permitir todo el tiempo'", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean tienePermiso(String permiso) {
        return ContextCompat.checkSelfPermission(this, permiso)   // pregunta a Android si el permiso está dado
                == PackageManager.PERMISSION_GRANTED;
    }

    private void verificarPermisos() {
        if (!tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION)) {   // falta la precisa: mostramos el cartel
            pedirUbicacion.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION });       // desde Android 12 van juntas
        } else {                                                         // ya la había aceptado antes
            pedirPermisoSegundoPlano();
        }
    }

    private void pedirPermisoSegundoPlano() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {   // antes de Android 10 no existía este permiso aparte
            permisosListos();
            return;
        }
        if (tienePermiso(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {   // ya estaba dado
            permisosListos();
            return;
        }
        pedirSegundoPlano.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION); // en Android 11+ abre Ajustes
    }

    private void permisosListos() {
        Toast.makeText(this, "Permisos de ubicación OK", Toast.LENGTH_SHORT).show();
        mostrarMiUbicacion();   // muestra dónde está el celular, para comparar con la puerta
        registrarGeocerca();    // y dibuja el círculo alrededor de la puerta
    }

    // ---------------- Lectura de ubicación ----------------

    @SuppressLint("MissingPermission")   // los permisos ya se chequearon antes de llegar acá
    private void mostrarMiUbicacion() {
        FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(this); // cliente de ubicación de Google

        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)   // una sola lectura, con GPS
                .addOnSuccessListener(this, ubicacion -> {
                    if (ubicacion == null) {                                // no hubo señal
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String mensaje = "Latitud: " + ubicacion.getLatitude()
                            + "\nLongitud: " + ubicacion.getLongitude()
                            + "\nPrecisión: ±" + ubicacion.getAccuracy() + " m";   // margen de error en metros
                    new AlertDialog.Builder(this)                           // cartel para corroborar las coordenadas
                            .setTitle("Mi ubicación")
                            .setMessage(mensaje)
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(this, e ->                            // falló la lectura
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ---------------- Geocerca ----------------

    @SuppressLint("MissingPermission")   // solo se llama con todos los permisos concedidos
    private void registrarGeocerca() {
        GeofencingClient cliente = LocationServices.getGeofencingClient(this);   // cliente que maneja las geocercas

        Geofence puerta = new Geofence.Builder()
                .setRequestId(ID_GEOCERCA)                                   // si ya existía una con este nombre, la pisa
                .setCircularRegion(PUERTA_LAT, PUERTA_LNG, RADIO_METROS)     // centro y radio del círculo
                .setExpirationDuration(Geofence.NEVER_EXPIRE)                // queda activa hasta que la borremos
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)                 // avisar al entrar y al salir
                .build();

        GeofencingRequest pedido = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)  // si ya estás adentro al registrarla, avisa igual
                .addGeofence(puerta)
                .build();

        cliente.addGeofences(pedido, crearAvisoGeocerca())                   // Android empieza a vigilar
                .addOnSuccessListener(this, unused ->
                        Toast.makeText(this, "Geocerca registrada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(this, e -> {                           // por ejemplo: falta permiso de segundo plano
                    String detalle = (e instanceof ApiException)
                            ? GeofenceStatusCodes.getStatusCodeString(((ApiException) e).getStatusCode())
                            : e.getMessage();
                    Toast.makeText(this, "No se pudo registrar la geocerca: " + detalle, Toast.LENGTH_LONG).show();
                });
    }

    // Prepara el aviso que Android le va a mandar a GeofenceReceiver
    private PendingIntent crearAvisoGeocerca() {
        Intent intent = new Intent(this, GeofenceReceiver.class);   // a quién avisar
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;              // si ya existía, lo actualiza
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;                    // desde Android 12 es obligatorio: Android le agrega los datos del evento
        }
        return PendingIntent.getBroadcast(this, 0, intent, flags);
    }

    // ---------------- Puerta y MQTT ----------------

    public void abrirPuerta(View v) {
        if (!MQTTManager.obtener(this).publicar(MQTTManager.TOPIC_COMANDO, dni)) {   // manda el DNI a la ESP32
            mostrarEstado("No se pudo enviar: sin conexión", Color.RED);
            return;
        }
        esperandoConfirmacion = true;                  // bloquea el botón hasta tener respuesta
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

    private void mostrarEstado(String mensaje, int color) {
        textViewEstado.setText(mensaje);
        textViewEstado.setTextColor(color);
    }

    // "Abrir" solo se habilita con sesión válida, conexión y sin otro pedido en curso
    private void actualizarBoton() {
        btnComandoAbrir.setEnabled(sesionValida && conectado && !esperandoConfirmacion);
    }

    private void mostrarAvisoSesionFallida(String mensaje) {
        textViewNumeroDni.setText(mensaje);
        textViewNumeroDni.setTextColor(Color.RED);
        actualizarBoton();
    }

    public void backToMenuFromAperturaPuerta(View v) {
        finish();   // vuelve al menú
    }
}