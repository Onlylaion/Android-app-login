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
import com.example.loggingym.api.dto.LoginRequest;
import com.example.loggingym.api.dto.LoginResponse;

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
    }

    private void intentarLogin() {
        String dni = campoDni.getText().toString().trim();
        String contrasenia = campoContrasenia.getText().toString().trim();

        if (dni.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Completá DNI y contraseña");
            return;
        }

        botonLogin.setEnabled(false);
        textoError.setVisibility(TextView.GONE);

        ApiClient.obtener(this).login(new LoginRequest(dni, contrasenia))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> respuesta) {
                        botonLogin.setEnabled(true);
                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            loginExitoso(dni, respuesta.body().token);
                        } else if (respuesta.code() == 422) {
                            mostrarError("El DNI debe tener 7 u 8 números");
                        } else {
                            mostrarError(ApiClient.mensajeDeError(respuesta, "Error del servidor (" + respuesta.code() + ")"));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        botonLogin.setEnabled(true);
                        mostrarError("No se pudo conectar con el servidor");
                    }
                });
    }

    private void loginExitoso(String dni, String token) {
        SesionHelper.saveUsuario(this, dni);
        SesionHelper.guardarToken(this, token);
        //SesionHelper.sumarPersonaEnGym(this);
        AppDatabase.obtenerInstancia(this).ingresoAppDao()
                .registrarIngreso(new IngresoApp(dni, null, System.currentTimeMillis()));

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
