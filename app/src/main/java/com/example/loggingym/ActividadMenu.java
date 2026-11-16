package com.example.loggingym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

public class ActividadMenu extends AppCompatActivity {

    private MaterialSwitch switchModoOscuro;
    private TextView saludo;

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

        saludo = findViewById(R.id.textViewSaludo);
        String nombre = getIntent().getStringExtra("Nombre");
        if (nombre == null) {
            nombre = SesionHelper.obtenerNombreUsuarioActual(this);
        }
        saludo.append(", " + nombre);

        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        switchModoOscuro.setChecked(SesionHelper.modoOscuroActivo(this));   // refleja el valor guardado
        switchModoOscuro.setOnCheckedChangeListener((boton, activo) -> {
            SesionHelper.saveModoOscuro(this, activo);
            GymApp.aplicarTema(activo);
        });
    }

    public void cerrarSesion(View v) {
        SesionHelper.cerrarSesion(this);
        MQTTManager.obtener(this).desconectar();
        Intent intento = new Intent(this, MainActivity.class);
        intento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intento);
        finish();
    }

    public void verPersonasEnGym(View v) {
        Intent intento = new Intent(this, CantidadPersonasGym.class);
        startActivity(intento);
    }

    // Tocar cualquier parte de la tarjeta equivale a tocar el interruptor
    public void alternarModoOscuro(View v) {
        switchModoOscuro.toggle();
    }

    public void goToActivityAperturaPuerta(View v) {
        Intent intent = new Intent(this, AperturaPuerta.class);
        startActivity(intent);
    }
}