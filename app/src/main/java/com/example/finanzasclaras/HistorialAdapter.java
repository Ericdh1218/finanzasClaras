package com.example.finanzasclaras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<Compra> comprasLiquidadas = new ArrayList<>();
    private List<Persona> listaPersonas = new ArrayList<>();

    public void setData(List<Compra> compras, List<Persona> personas) {
        this.comprasLiquidadas = compras;
        this.listaPersonas = personas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Compra compra = comprasLiquidadas.get(position);

        holder.tvDescripcion.setText(compra.getDescripcion());
        holder.tvMonto.setText(String.format("Pagado: $%,.2f", compra.getMonto()));

        // Buscamos el nombre de la persona
        String nombrePersona = "Desconocido";
        for (Persona p : listaPersonas) {
            if (p.getId().equals(compra.getPersonaId())) {
                nombrePersona = p.getNombre();
                break;
            }
        }
        holder.tvNombre.setText("Liquidado por: " + nombrePersona);
    }

    @Override
    public int getItemCount() { return comprasLiquidadas.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvMonto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_historial);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_historial);
            tvMonto = itemView.findViewById(R.id.tv_monto_historial);
        }
    }
}