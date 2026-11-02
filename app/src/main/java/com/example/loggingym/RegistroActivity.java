package com.example.loggingym;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loggingym.api.ApiClient;
import com.example.loggingym.api.RegistroRequest;
import com.example.loggingym.api.RegistroResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private EditText campoDni;
    private EditText campoClave;
    private EditText campoNombre;
    private EditText campoApellido;
    private TextView textoError;
    private Button botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        campoDni = findViewById(R.id.input_registro_dni);
        campoClave = findViewById(R.id.input_registro_clave);
        campoNombre = findViewById(R.id.input_registro_nombre);
        campoApellido = findViewById(R.id.input_registro_apellido);
        textoError = findViewById(R.id.texto_error_registro);

        botonRegistrar = findViewById(R.id.boton_registrar);
        botonRegistrar.setOnClickListener(v -> intentarRegistro());

        Button botonVolver = findViewById(R.id.boton_volver);
        botonVolver.setOnClickListener(v -> finish());
    }

    private void intentarRegistro() {
        String dni = campoDni.getText().toString().trim();
        String clave = campoClave.getText().toString().trim();
        String nombre = campoNombre.getText().toString().trim();
        String apellido = campoApellido.getText().toString().trim();

        if (dni.isEmpty() || clave.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            mostrarError("Completá todos los campos");
            return;
        }

        // Misma regla que el Validador DNI de Node-RED
        if (!dni.matches("\\d{7,8}")) {
            mostrarError("El DNI debe tener 7 u 8 dígitos");
            return;
        }

        textoError.setVisibility(TextView.GONE);
        botonRegistrar.setEnabled(false);

        RegistroRequest datos = new RegistroRequest(dni, clave, nombre, apellido);
        ApiClient.obtener().obtenerRegistroApi().registrar(datos).enqueue(new Callback<RegistroResponse>() {
            @Override
            public void onResponse(Call<RegistroResponse> call, Response<RegistroResponse> response) {
                botonRegistrar.setEnabled(true);
                RegistroResponse cuerpo = response.body();

                if (response.isSuccessful() && cuerpo != null && cuerpo.ok) {
                    Toast.makeText(RegistroActivity.this, "Cuenta creada, ya podés ingresar", Toast.LENGTH_LONG).show();
                    finish();
                } else if (response.code() == 409) {
                    mostrarError("Ese DNI ya está registrado");
                } else if (response.code() == 400) {
                    mostrarError("Datos inválidos");
                } else {
                    mostrarError("Error del servidor (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<RegistroResponse> call, Throwable t) {
                botonRegistrar.setEnabled(true);
                mostrarError("No se pudo conectar con el servidor");
            }
        });
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        textoError.setVisibility(TextView.VISIBLE);
    }
}
