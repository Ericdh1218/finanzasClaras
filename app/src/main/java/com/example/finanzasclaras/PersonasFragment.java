package com.example.finanzasclaras;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class PersonasFragment extends Fragment {

    private PersonasViewModel personasViewModel;
    private ComprasViewModel comprasViewModel;
    private PersonaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_personas);
        TextView tvEmpty = view.findViewById(R.id.tv_empty);

        // Adapter
        adapter = new PersonaAdapter(new PersonaAdapter.OnPersonaClickListener() {
            @Override
            public void onPersonaClick(Persona persona) {
                // Entrar al detalle
                Bundle args = new Bundle();
                args.putString("personaId", persona.getId());
                args.putString("personaNombre", persona.getNombre());
                Navigation.findNavController(view).navigate(R.id.action_personas_to_detalle, args);
            }

            @Override
            public void onEditarClick(Persona persona) {
                mostrarDialogEditarPersona(persona);
            }

            @Override
            public void onEliminarClick(Persona persona) {
                confirmarEliminacionPersona(persona.getId());
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // ViewModels
        personasViewModel = new ViewModelProvider(this).get(PersonasViewModel.class);
        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);

        personasViewModel.getPersonas().observe(getViewLifecycleOwner(), personas -> {
            adapter.setPersonas(personas);
            tvEmpty.setVisibility(personas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        comprasViewModel.cargarTodasLasCompras();

        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) adapter.setCompras(compras);
        });

        // FAB → dialog para agregar persona
        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_persona);
        fab.setOnClickListener(v -> mostrarDialogAgregarPersona());
    }

    private void mostrarDialogAgregarPersona() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_agregar_persona, null);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nueva persona")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    TextInputEditText etNombre   = dialogView.findViewById(R.id.et_nombre);
                    TextInputEditText etTelefono = dialogView.findViewById(R.id.et_telefono);

                    String nombre   = etNombre.getText().toString().trim();
                    String telefono = etTelefono.getText().toString().trim();

                    if (!nombre.isEmpty()) {
                        Persona p = new Persona(null, nombre, telefono);
                        personasViewModel.agregarPersona(p);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ DIÁLOGO DE CONFIRMACIÓN PARA ELIMINAR PERSONA (Añadido)
    private void confirmarEliminacionPersona(String personaId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Eliminar persona?")
                .setMessage("¿Estás seguro de que quieres borrar a esta persona? Se perderá el historial de sus deudas activas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    personasViewModel.eliminarPersona(personaId);
                    Toast.makeText(getContext(), "Persona eliminada", Toast.LENGTH_SHORT).show();
                    // Ojo: Aquí NO ponemos popBackStack() porque ya estamos en la lista principal
                })
                .show();
    }

    // ✏️ DIÁLOGO PARA EDITAR PERSONA (Añadido)
    private void mostrarDialogEditarPersona(Persona personaActual) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_persona, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.et_nombre);
        TextInputEditText etTelefono = dialogView.findViewById(R.id.et_telefono);

        etNombre.setText(personaActual.getNombre());
        etTelefono.setText(personaActual.getTelefono());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar información")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String telefono = etTelefono.getText().toString().trim();

                    if (!nombre.isEmpty()) {
                        personaActual.setNombre(nombre);
                        personaActual.setTelefono(telefono);
                        personasViewModel.actualizarPersona(personaActual);
                        Toast.makeText(getContext(), "Información actualizada", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}