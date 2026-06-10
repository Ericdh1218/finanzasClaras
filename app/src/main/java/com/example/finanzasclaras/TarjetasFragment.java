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

public class TarjetasFragment extends Fragment {

    private TarjetasViewModel tarjetasViewModel;
    private ComprasViewModel comprasViewModel;
    private TarjetaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tarjetas, container, false); //Asegúrate de haberle puesto el fitsSystemWindows="true" a este xml
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_tarjetas);
        TextView tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new TarjetaAdapter(new TarjetaAdapter.OnTarjetaClickListener() {
            @Override
            public void onTarjetaClick(Tarjeta tarjeta) {
                Bundle args = new Bundle();
                args.putString("tarjetaId", tarjeta.getId());
                args.putString("tarjetaNombre", tarjeta.getNombre());
                Navigation.findNavController(view).navigate(R.id.action_tarjetas_to_detalle, args);
            }

            @Override
            public void onEditarClick(Tarjeta tarjeta) {
                mostrarDialogEditarTarjeta(tarjeta);
            }

            @Override
            public void onEliminarClick(Tarjeta tarjeta) {
                confirmarEliminacionTarjeta(tarjeta.getId());
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        tarjetasViewModel = new ViewModelProvider(this).get(TarjetasViewModel.class);
        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);

        tarjetasViewModel.getTarjetas().observe(getViewLifecycleOwner(), tarjetas -> {
            adapter.setTarjetas(tarjetas);
            tvEmpty.setVisibility(tarjetas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        comprasViewModel.cargarTodasLasCompras();
        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) adapter.setCompras(compras);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_tarjeta);
        if (fab != null) {
            fab.setOnClickListener(v -> mostrarDialogAgregarTarjeta());
        }

        tarjetasViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                android.widget.Toast.makeText(getContext(), "Error Firebase: " + errorMsg, android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("CuentasClarasError", errorMsg);
            }
        });
    }

    private void mostrarDialogAgregarTarjeta() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_tarjeta, null);
        TextInputEditText etNombre  = dialogView.findViewById(R.id.et_nombre_tarjeta);
        TextInputEditText etBanco   = dialogView.findViewById(R.id.et_banco);
        TextInputEditText etLimite  = dialogView.findViewById(R.id.et_limite);
        TextInputEditText etCorte   = dialogView.findViewById(R.id.et_fecha_corte);
        TextInputEditText etPago    = dialogView.findViewById(R.id.et_fecha_pago);
        com.google.android.material.switchmaterial.SwitchMaterial switchQuincenal = dialogView.findViewById(R.id.switch_quincenal);

        switchQuincenal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etCorte.setVisibility(View.GONE);
                etPago.setVisibility(View.GONE);
            } else {
                etCorte.setVisibility(View.VISIBLE);
                etPago.setVisibility(View.VISIBLE);
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nueva tarjeta")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String banco  = etBanco.getText().toString().trim();
                    boolean esQuincenal = switchQuincenal.isChecked();

                    if (!nombre.isEmpty() && !banco.isEmpty()) {
                        double limite = etLimite.getText().toString().isEmpty() ? 0
                                : Double.parseDouble(etLimite.getText().toString());
                        int corte = 0;
                        int pago = 0;

                        if (!esQuincenal) {
                            corte = etCorte.getText().toString().isEmpty() ? 1 : Integer.parseInt(etCorte.getText().toString());
                            pago = etPago.getText().toString().isEmpty() ? 1 : Integer.parseInt(etPago.getText().toString());
                        }
                        Tarjeta t = new Tarjeta(null, nombre, banco, limite, corte, pago, esQuincenal);
                        tarjetasViewModel.agregarTarjeta(t);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminacionTarjeta(String tarjetaId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Eliminar tarjeta?")
                .setMessage("Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    tarjetasViewModel.eliminarTarjeta(tarjetaId);
                    Toast.makeText(getContext(), "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void mostrarDialogEditarTarjeta(Tarjeta tarjetaActual) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_tarjeta, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.et_nombre_tarjeta);
        TextInputEditText etBanco  = dialogView.findViewById(R.id.et_banco);
        TextInputEditText etLimite = dialogView.findViewById(R.id.et_limite);
        TextInputEditText etCorte  = dialogView.findViewById(R.id.et_fecha_corte);
        TextInputEditText etPago   = dialogView.findViewById(R.id.et_fecha_pago);
        com.google.android.material.switchmaterial.SwitchMaterial switchQuincenal = dialogView.findViewById(R.id.switch_quincenal);

        etNombre.setText(tarjetaActual.getNombre());
        etBanco.setText(tarjetaActual.getBanco());
        etLimite.setText(String.valueOf(tarjetaActual.getLimite()));
        switchQuincenal.setChecked(tarjetaActual.isQuincenal());

        // Dentro del botón "Actualizar"
        if (!tarjetaActual.isQuincenal()) {
            String corteStr = etCorte.getText().toString().trim();
            String pagoStr = etPago.getText().toString().trim();

            // Si está vacío le ponemos un 1, si no, leemos el número
            int diaCorte = corteStr.isEmpty() ? 1 : Integer.parseInt(corteStr);
            int diaPago  = pagoStr.isEmpty() ? 1 : Integer.parseInt(pagoStr);

            tarjetaActual.setFechaCorte(diaCorte);
            tarjetaActual.setFechaPago(diaPago);
        }else {
            etCorte.setText(String.valueOf(tarjetaActual.getFechaCorte()));
            etPago.setText(String.valueOf(tarjetaActual.getFechaPago()));
        }

        switchQuincenal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etCorte.setVisibility(View.GONE);
                etPago.setVisibility(View.GONE);
            } else {
                etCorte.setVisibility(View.VISIBLE);
                etPago.setVisibility(View.VISIBLE);
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar tarjeta")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String banco = etBanco.getText().toString().trim();
                    String limiteStr = etLimite.getText().toString().trim();

                    if (!nombre.isEmpty() && !banco.isEmpty() && !limiteStr.isEmpty()) {
                        double limite = Double.parseDouble(limiteStr);
                        tarjetaActual.setNombre(nombre);
                        tarjetaActual.setBanco(banco);
                        tarjetaActual.setLimite(limite);

                        // 👇 AQUÍ ESTÁ LA CORRECCIÓN 👇
                        if (!tarjetaActual.isQuincenal()) {
                            String corteStr = etCorte.getText().toString().trim();
                            String pagoStr = etPago.getText().toString().trim();

                            // Si está vacío le ponemos un 1 para que no explote
                            int diaCorte = corteStr.isEmpty() ? 1 : Integer.parseInt(corteStr);
                            int diaPago  = pagoStr.isEmpty() ? 1 : Integer.parseInt(pagoStr);

                            tarjetaActual.setFechaCorte(diaCorte);
                            tarjetaActual.setFechaPago(diaPago);
                        }

                        tarjetasViewModel.actualizarTarjeta(tarjetaActual);
                        Toast.makeText(getContext(), "Tarjeta actualizada", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
