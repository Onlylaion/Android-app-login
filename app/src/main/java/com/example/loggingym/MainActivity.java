package com.example.loggingym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText campoDni;
    private EditText campoContrasenia;
    private TextView textoError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SesionHelper.haySesionActiva(this)) {
            irAMenuPrincipal();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        campoDni = findViewById(R.id.input_dni);
        campoContrasenia = findViewById(R.id.input_contrasenia);
        textoError = findViewById(R.id.texto_error);

        Button botonLogin = findViewById(R.id.boton_login);
        botonLogin.setOnClickListener(v -> intentarLogin());
    }

    private void intentarLogin() {
        String dni = campoDni.getText().toString().trim();
        String contrasenia = campoContrasenia.getText().toString().trim();

        if (dni.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Completá DNI y contraseña");
            return;
        }

        Usuario usuario = AppDatabase.obtenerInstancia(this).usuarioDao().validarLogin(dni, contrasenia);

        if (usuario == null) {
            mostrarError("DNI o contraseña incorrectos");
            return;
        }

        SesionHelper.iniciarSesion(this, usuario.dni);
        SesionHelper.sumarPersonaLogueada(this);
        AppDatabase.obtenerInstancia(this).ingresoAppDao()
                .registrarIngreso(new IngresoApp(usuario.dni, usuario.nombre, System.currentTimeMillis()));

        irAMenuPrincipal();
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        textoError.setVisibility(TextView.VISIBLE);
    }

    private void irAMenuPrincipal() {
        Intent intento = new Intent(this, ActividadMenu.class);
        startActivity(intento);
        finish();
    }
}
