package com.example.loggingym;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoVH> {

    private List<Pago> pagos = new ArrayList<>();
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public void setData(List<Pago> nuevosPagos) {
        pagos = nuevosPagos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PagoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pago, parent, false);
        return new PagoVH(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoVH holder, int position) {
        holder.bind(pagos.get(position));
    }

    @Override
    public int getItemCount() {
        return pagos.size();
    }

    class PagoVH extends RecyclerView.ViewHolder {
        private final TextView textoConcepto;
        private final TextView textoFecha;
        private final TextView textoMonto;

        PagoVH(@NonNull View itemView) {
            super(itemView);
            textoConcepto = itemView.findViewById(R.id.texto_concepto);
            textoFecha = itemView.findViewById(R.id.texto_fecha);
            textoMonto = itemView.findViewById(R.id.texto_monto);
        }

        void bind(Pago pago) {
            textoConcepto.setText(pago.concepto);
            textoFecha.setText(formatoFecha.format(new Date(pago.fecha)));
            textoMonto.setText(String.format(Locale.getDefault(), "$%.2f", pago.monto));
        }
    }
}
