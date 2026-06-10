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

public class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.ViewHolder> {

    // 1. Interfaz limpia con solo las 3 acciones necesarias
    public interface OnPersonaClickListener {
        void onPersonaClick(Persona persona);
        void onEditarClick(Persona persona);
        void onEliminarClick(Persona persona);
    }

    private List<Persona> personas = new ArrayList<>();
    private List<Compra> todasLasCompras = new ArrayList<>();
    private final OnPersonaClickListener listener;

    public PersonaAdapter(OnPersonaClickListener listener) {
        this.listener = listener;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
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
                .inflate(R.layout.item_persona, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Persona persona = personas.get(position);

        // Inicial del nombre
        String inicial = persona.getNombre().substring(0, 1).toUpperCase();
        holder.tvInicial.setText(inicial);
        holder.tvNombre.setText(persona.getNombre());

        // Calcular deuda y número de compras de esta persona
        double deuda = 0;
        int numCompras = 0;
        for (Compra c : todasLasCompras) {
            if (c.getPersonaId().equals(persona.getId())) {
                deuda += c.getSaldoPendiente();
                numCompras++;
            }
        }

        // Asignar los textos de deuda y compras
        holder.tvDeuda.setText(String.format("$%,.2f", deuda));
        holder.tvNumCompras.setText(numCompras + " compra(s) pendiente(s)");

        // 2. Configurar los clics correctamente una sola vez
        holder.itemView.setOnClickListener(v -> listener.onPersonaClick(persona));
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(persona));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(persona));
    }

    @Override
    public int getItemCount() { return personas.size(); }

    // 3. Declarar e enlazar los ImageButtons
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicial, tvNombre, tvDeuda, tvNumCompras;
        ImageButton btnEditar, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInicial    = itemView.findViewById(R.id.tv_inicial);
            tvNombre     = itemView.findViewById(R.id.tv_nombre_persona);
            tvDeuda      = itemView.findViewById(R.id.tv_deuda_persona);
            tvNumCompras = itemView.findViewById(R.id.tv_num_compras);

            // Enlazando los botones del XML
            btnEditar    = itemView.findViewById(R.id.btn_editar_item);
            btnEliminar  = itemView.findViewById(R.id.btn_eliminar_item);
        }
    }
}