package com.example.loggingym;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

// Android llama a esta clase cuando el usuario entra o sale de la geocerca de la puerta
public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent evento = GeofencingEvent.fromIntent(intent);   // datos del aviso que manda Android
        if (evento == null) return;                                    // no vino nada útil

        if (evento.hasError()) {                                       // Android avisó un problema con la geocerca
            String error = GeofenceStatusCodes.getStatusCodeString(evento.getErrorCode()); // código de error pasado a texto
            Toast.makeText(context, "Error en geocerca: " + error, Toast.LENGTH_LONG).show();
            return;
        }

        Location donde = evento.getTriggeringLocation();               // dónde estaba el celular cuando saltó el aviso
        String coordenadas = (donde != null)
                ? "\nLat: " + donde.getLatitude() + "\nLng: " + donde.getLongitude()
                : "\n(sin coordenadas)";

        int transicion = evento.getGeofenceTransition();               // qué pasó: entró o salió
        if (transicion == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Toast.makeText(context, "Entraste al gimnasio" + coordenadas, Toast.LENGTH_LONG).show();
        } else if (transicion == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context, "Saliste del gimnasio" + coordenadas, Toast.LENGTH_LONG).show();
        }
    }
}