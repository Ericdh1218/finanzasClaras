package com.example.finanzasclaras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TarjetaAdapter extends RecyclerView.Adapter<TarjetaAdapter.ViewHolder> {

    public interface OnTarjetaClickListener {
        void onTarjetaClick(Tarjeta tarjeta);
        void onEditarClick(Tarjeta tarjeta);
        void onEliminarClick(Tarjeta tarjeta);
    }

    private List<Tarjeta> tarjetas = new ArrayList<>();
    private List<Compra> todasLasCompras = new ArrayList<>();
    private final OnTarjetaClickListener listener;

    public TarjetaAdapter(OnTarjetaClickListener listener) {
        this.listener = listener;
    }

    public void setTarjetas(List<Tarjeta> tarjetas) {
        this.tarjetas = tarjetas;
        notifyDataSetChanged();
    }

    public void setCompras(List<Compra> compras) {
        this.todasLasCompras = compras;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjeta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tarjeta tarjeta = tarjetas.get(position);

        holder.tvNombre.setText(tarjeta.getNombre());

        if (tarjeta.isQuincenal()) {
            holder.tvFechaCorte.setText("Esquema: Quincenal");
            holder.tvFechaPago.setText("Pago: Cada 15 días");
        } else {
            holder.tvFechaCorte.setText("Corte: día " + tarjeta.getFechaCorte());
            holder.tvFechaPago.setText("Pago: día " + tarjeta.getFechaPago());
        }

        double deuda = 0;
        for (Compra c : todasLasCompras) {
            if (c.getTarjetaId().equals(tarjeta.getId())) {
                deuda += c.getSaldoPendiente();
            }
        }

        double limite = tarjeta.getLimite();
        double disponible = limite - deuda;

        holder.tvCreditoDisponible.setText(String.format("Disponible: $%,.2f", disponible));
        holder.tvDeuda.setText(String.format("$%,.2f", deuda));

        // Clics
        holder.itemView.setOnClickListener(v -> listener.onTarjetaClick(tarjeta));
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(tarjeta));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(tarjeta));
    }

    @Override
    public int getItemCount() { return tarjetas.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDeuda, tvFechaCorte, tvFechaPago, tvCreditoDisponible;
        ImageButton btnEditar, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre            = itemView.findViewById(R.id.tv_nombre_tarjeta);
            tvDeuda             = itemView.findViewById(R.id.tv_deuda_tarjeta);
            tvFechaCorte        = itemView.findViewById(R.id.tv_fecha_corte);
            tvFechaPago         = itemView.findViewById(R.id.tv_fecha_pago);
            tvCreditoDisponible = itemView.findViewById(R.id.tv_credito_disponible);
            btnEditar           = itemView.findViewById(R.id.btn_editar_item_tarjeta);
            btnEliminar         = itemView.findViewById(R.id.btn_eliminar_item_tarjeta);
        }
    }
}