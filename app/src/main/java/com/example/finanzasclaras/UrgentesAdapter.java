package com.example.finanzasclaras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UrgentesAdapter extends RecyclerView.Adapter<UrgentesAdapter.ViewHolder> {
    private List<Compra> comprasUrgentes = new ArrayList<>();
    private final OnUrgenteClickListener listener;

    public interface OnUrgenteClickListener { void onAbonar(Compra c); }

    public UrgentesAdapter(OnUrgenteClickListener listener) { this.listener = listener; }

    public void setCompras(List<Compra> compras) {
        this.comprasUrgentes = compras;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_urgente, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Compra c = comprasUrgentes.get(position);
        holder.tvDesc.setText(c.getDescripcion());
       // holder.tvMonto.setText(String.format("$%,.2f", c.getSaldoPendiente()));

        holder.tvNombre.setText(c.getNombrePersonaTemporal());

        double montoAMostrar;
        if (c.getTipoPago().equals("Contado")) {
            montoAMostrar = c.getSaldoPendiente();
        } else {
            // Si es a meses/quincenas, mostramos la cuota.
            // Math.min evita que la cuota sea mayor al saldo que queda.
            montoAMostrar = Math.min(c.getMontoPorCuota(), c.getSaldoPendiente());
        }

        holder.tvMonto.setText(String.format("$%,.2f", montoAMostrar));
        holder.itemView.setOnClickListener(v -> listener.onAbonar(c));
    }

    @Override
    public int getItemCount() { return comprasUrgentes.size(); }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvMonto, tvNombre;

        ViewHolder(View v) {
            super(v);
            tvDesc = v.findViewById(R.id.tv_urgente_desc);
            tvMonto = v.findViewById(R.id.tv_urgente_monto);
            tvNombre = v.findViewById(R.id.tv_urgente_nombre);
        }
    }
}