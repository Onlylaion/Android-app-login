package com.example.loggingym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VerPagos extends AppCompatActivity {

    private static final double MONTO_CUOTA = 15000.0;

    private PagoAdapter pagoAdapter;
    private String dniUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_pagos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dniUsuarioActual = SesionHelper.obtenerUsuarioActual(this);

        RecyclerView recyclerPagos = findViewById(R.id.recycler_pagos);
        recyclerPagos.setLayoutManager(new LinearLayoutManager(this));
        pagoAdapter = new PagoAdapter();
        recyclerPagos.setAdapter(pagoAdapter);

        cargarPagos();

        findViewById(R.id.boton_pagar).setOnClickListener(v -> pagarCuota());
    }

    private void cargarPagos() {
        List<Pago> pagos = AppDatabase.obtenerInstancia(this).pagoDao().listarPorUsuario(dniUsuarioActual);
        pagoAdapter.setData(pagos);
    }

    private void pagarCuota() {
        Pago pago = new Pago(dniUsuarioActual, MONTO_CUOTA, System.currentTimeMillis(), "Cuota mensual");
        AppDatabase.obtenerInstancia(this).pagoDao().insertar(pago);
        cargarPagos();
    }

    public void Regresar(View v){
        Intent intento = new Intent(this, ActividadMenu.class);
        startActivity(intento);
    }
}