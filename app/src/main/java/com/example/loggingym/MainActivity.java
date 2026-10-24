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

import com.example.loggingym.api.ApiClient;
import com.example.loggingym.api.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText campoDni;
    private EditText campoContrasenia;
    private TextView textoError;
    private Button botonLogin;

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

        botonLogin = findViewById(R.id.boton_login);
        botonLogin.setOnClickListener(v -> intentarLogin());

        Button botonIrRegistro = findViewById(R.id.boton_ir_registro);
        botonIrRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        String dni = campoDni.getText().toString().trim();
        String contrasenia = campoContrasenia.getText().toString().trim();

        if (dni.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Completá DNI y contraseña");
            return;
        }

        textoError.setVisibility(TextView.GONE);
        botonLogin.setEnabled(false);

        ApiClient.obtener().obtenerLoginApi().login(dni, contrasenia).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                botonLogin.setEnabled(true);
                LoginResponse cuerpo = response.body();

                if (response.isSuccessful() && cuerpo != null && cuerpo.ok) {
                    loginExitoso(dni, cuerpo.nombre);
                } else if (response.code() == 401) {
                    mostrarError("DNI o contraseña incorrectos");
                } else if (response.code() == 400) {
                    mostrarError("Datos inválidos");
                } else {
                    mostrarError("Error del servidor (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                botonLogin.setEnabled(true);
                mostrarError("No se pudo conectar con el servidor");
            }
        });
    }

    private void loginExitoso(String dni, String nombre) {
        SesionHelper.saveUsuario(this, dni);
        //SesionHelper.sumarPersonaEnGym(this);
        AppDatabase.obtenerInstancia(this).ingresoAppDao()
                .registrarIngreso(new IngresoApp(dni, nombre, System.currentTimeMillis()));

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
