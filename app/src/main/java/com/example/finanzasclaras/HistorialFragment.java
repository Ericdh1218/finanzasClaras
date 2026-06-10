package com.example.finanzasclaras;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {

    private ComprasViewModel comprasViewModel;
    private PersonasViewModel personasViewModel;
    private HistorialAdapter adapter;

    private List<Persona> personasActuales = new ArrayList<>();
    private List<Compra> comprasActuales = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvHistorial = view.findViewById(R.id.rv_historial);
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistorialAdapter();
        rvHistorial.setAdapter(adapter);

        comprasViewModel = new ViewModelProvider(this).get(ComprasViewModel.class);
        personasViewModel = new ViewModelProvider(this).get(PersonasViewModel.class);

        // 👇 AHORA LLAMAMOS AL NUEVO MÉTODO QUE BUSCA LAS LIQUIDADAS 👇
        comprasViewModel.cargarHistorialLiquidadas();

        personasViewModel.getPersonas().observe(getViewLifecycleOwner(), personas -> {
            if (personas != null) {
                personasActuales = personas;
                filtrarYMostrar();
            }
        });

        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) {
                comprasActuales = compras;
                filtrarYMostrar();
            }
        });
    }

    private void filtrarYMostrar() {
        // Esperamos solo a que carguen los nombres de las personas
        if (personasActuales.isEmpty()) return;

        // Mandamos la lista directa al adaptador.
        // Si no hay compras liquidadas, simplemente se mostrará en blanco sin romperse.
        List<Compra> liquidadas = new ArrayList<>();
        if (comprasActuales != null) {
            for (Compra c : comprasActuales) {
                if (c.isLiquidada() || c.getSaldoPendiente() <= 0) {
                    liquidadas.add(c);
                }
            }
        }

        adapter.setData(liquidadas, personasActuales);
    }
}