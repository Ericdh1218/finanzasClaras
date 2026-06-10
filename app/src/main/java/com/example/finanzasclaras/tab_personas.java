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

public class tab_personas extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_personas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_personas_resumen);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        PersonasViewModel personasVM = new ViewModelProvider(requireParentFragment())
                .get(PersonasViewModel.class);
        ComprasViewModel comprasVM = new ViewModelProvider(requireParentFragment())
                .get(ComprasViewModel.class);

        // Reutilizamos el adapter, pero declarando los 3 métodos explícitamente
        PersonaAdapter adapter = new PersonaAdapter(new PersonaAdapter.OnPersonaClickListener() {
            @Override
            public void onPersonaClick(Persona persona) {
                // En el tab solo mostramos, no navegamos
            }

            @Override
            public void onEditarClick(Persona persona) {
                // No hacemos nada desde el resumen
            }

            @Override
            public void onEliminarClick(Persona persona) {
                // No hacemos nada desde el resumen
            }
        });

        rv.setAdapter(adapter);

        personasVM.getPersonas().observe(getViewLifecycleOwner(),
                adapter::setPersonas);

        comprasVM.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) adapter.setCompras(compras);
        });
    }
}