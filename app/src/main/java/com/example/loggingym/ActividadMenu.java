package com.example.loggingym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    }

    public void cerrarSesion(View v) {
        //SesionHelper.restarPersonaEnGym(this);
        SesionHelper.cerrarSesionYVolverAlLogin(this);
    }

    public void verPersonasEnGym(View v) {
        Intent intento = new Intent(this, CantidadPersonasGym.class);
        startActivity(intento);
    }
    public void verPagos(View v) {
        Intent intento = new Intent(this, VerPagos.class);
        startActivity(intento);
    }

    public void goToActivityAperturaPuerta(View v) {
        Intent intent = new Intent(this, AperturaPuerta.class);
        startActivity(intent);
    }
}