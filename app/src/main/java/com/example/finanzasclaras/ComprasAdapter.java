package com.example.finanzasclaras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComprasAdapter extends RecyclerView.Adapter<ComprasAdapter.ViewHolder> {

    public interface OnAbonarClickListener {
        void onAbonar(Compra compra);
        void onEditar(Compra compra);
    }

    private List<Compra> compras = new ArrayList<>();
    private List<Tarjeta> tarjetas = new ArrayList<>();
    private final OnAbonarClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "MX"));

    public ComprasAdapter(OnAbonarClickListener listener) {
        this.listener = listener;
    }

    public void setCompras(List<Compra> compras) {
        this.compras = compras;
        notifyDataSetChanged();
    }

    public void setTarjetas(List<Tarjeta> tarjetas) {
        this.tarjetas = tarjetas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compra, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Compra compra = compras.get(position);

        holder.tvDescripcion.setText(compra.getDescripcion());
        holder.tvSaldoPendiente.setText(String.format("$%,.2f", compra.getSaldoPendiente()));

        // --- LOGICA DEL MOTOR DE CUOTAS PARA MOSTRAR DETALLES ---
        String detallesPago = "";
        String tipo = compra.getTipoPago() != null ? compra.getTipoPago() : "Contado";

        switch (tipo) {
            case "Contado":
                detallesPago = String.format("Total: $%,.2f | Contado", compra.getMonto());
                break;

            case "Meses Sin Intereses (MSI)":
                detallesPago = String.format("Total: $%,.2f | MSI: %d/%d ($%,.2f/mes)",
                        compra.getMonto(),
                        compra.getCuotasPagadas(),
                        compra.getTotalCuotas(),
                        compra.getMontoPorCuota());
                break;

            case "Quincenas":
                detallesPago = String.format("Total: $%,.2f | Quincenas: %d/%d ($%,.2f/quincena)",
                        compra.getMonto(),
                        compra.getCuotasPagadas(),
                        compra.getTotalCuotas(),
                        compra.getMontoPorCuota());
                break;

            case "Meses con Intereses":
                detallesPago = String.format("Total original: $%,.2f | Con Intereses: %d/%d ($%,.2f/mes)",
                        compra.getMonto(),
                        compra.getCuotasPagadas(),
                        compra.getTotalCuotas(),
                        compra.getMontoPorCuota());
                break;
        }

        holder.tvMontoOriginal.setText(detallesPago);
        // --------------------------------------------------------

        // Fecha
        if (compra.getFecha() != null) {
            holder.tvFecha.setText(sdf.format(compra.getFecha().toDate()));
        }

        // Nombre de la tarjeta (Esta lógica se queda igual)
        String nombreTarjeta = "Sin tarjeta";
        for (Tarjeta t : tarjetas) {
            if (t.getId().equals(compra.getTarjetaId())) {
                nombreTarjeta = t.getNombre();
                break;
            }
        }
        holder.tvTarjeta.setText(nombreTarjeta);

        // Barra de progreso (cuánto falta por pagar)
        int progreso = 0;
        if (compra.getMonto() > 0) {
            double pagado = compra.getMonto() - compra.getSaldoPendiente();
            progreso = (int) ((pagado / compra.getMonto()) * 100);
        }
        holder.progressSaldo.setProgress(progreso);

        holder.btnAbonar.setOnClickListener(v -> listener.onAbonar(compra));
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(compra));
    }

    @Override
    public int getItemCount() { return compras.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvMontoOriginal, tvSaldoPendiente, tvFecha, tvTarjeta;
        ProgressBar progressSaldo;
        MaterialButton btnAbonar,btnEditar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion    = itemView.findViewById(R.id.tv_descripcion);
            tvMontoOriginal  = itemView.findViewById(R.id.tv_monto_original);
            tvSaldoPendiente = itemView.findViewById(R.id.tv_saldo_pendiente);
            tvFecha          = itemView.findViewById(R.id.tv_fecha_compra);
            tvTarjeta        = itemView.findViewById(R.id.tv_tarjeta_compra);
            progressSaldo    = itemView.findViewById(R.id.progress_saldo);
            btnAbonar        = itemView.findViewById(R.id.btn_abonar);
            btnEditar = itemView.findViewById(R.id.btn_editar_compra);
        }
    }
}