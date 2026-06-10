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

public class tab_tarjetas extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_tarjetas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_tarjetas_resumen);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        TarjetasViewModel tarjetasVM = new ViewModelProvider(requireParentFragment())
                .get(TarjetasViewModel.class);
        ComprasViewModel comprasVM = new ViewModelProvider(requireParentFragment())
                .get(ComprasViewModel.class);

        // Reutilizamos el adapter declarando explícitamente los 3 métodos en blanco
        TarjetaAdapter adapter = new TarjetaAdapter(new TarjetaAdapter.OnTarjetaClickListener() {
            @Override
            public void onTarjetaClick(Tarjeta tarjeta) {
                // En el tab solo mostramos, no navegamos
            }

            @Override
            public void onEditarClick(Tarjeta tarjeta) {
                // No hacemos nada desde el resumen
            }

            @Override
            public void onEliminarClick(Tarjeta tarjeta) {
                // No hacemos nada desde el resumen
            }
        });

        rv.setAdapter(adapter);

        tarjetasVM.getTarjetas().observe(getViewLifecycleOwner(),
                adapter::setTarjetas);

        comprasVM.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) adapter.setCompras(compras);
        });
    }
}