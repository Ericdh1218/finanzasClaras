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

import java.util.List;

public class DetalleTarjetaFragment extends Fragment {

    private ComprasViewModel comprasViewModel;
    private TarjetasViewModel tarjetasViewModel;
    private ComprasAdapter adapter;
    private String tarjetaId;
    private String tarjetaNombre;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_tarjeta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tarjetaId     = getArguments().getString("tarjetaId");
            tarjetaNombre = getArguments().getString("tarjetaNombre");
        }

        final Tarjeta[] tarjetaSeleccionada = new Tarjeta[1];

        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar_detalle_tarjeta);
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.menu_detalle_tarjeta);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_editar) {
                    if (tarjetaSeleccionada[0] != null) mostrarDialogEditarTarjeta(tarjetaSeleccionada[0]);
                    return true;
                } else if (item.getItemId() == R.id.action_eliminar) {
                    confirmarEliminacionTarjeta(tarjetaId);
                    return true;
                }
                return false;
            });
        }

        TextView tvNombre   = view.findViewById(R.id.tv_nombre_tarjeta_detalle);
        TextView tvTotal    = view.findViewById(R.id.tv_total_tarjeta);
        TextView tvDiaPago  = view.findViewById(R.id.tv_dia_pago_detalle);
        TextView tvDiaCorte = view.findViewById(R.id.tv_dia_corte_detalle);
        RecyclerView rv     = view.findViewById(R.id.rv_compras_tarjeta);

        tvNombre.setText(tarjetaNombre);

        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);
        tarjetasViewModel = new ViewModelProvider(this).get(TarjetasViewModel.class);

        // 👇 SOLUCIÓN AL ERROR DE COMPILACIÓN 👇
        adapter = new ComprasAdapter(new ComprasAdapter.OnAbonarClickListener() {
            @Override
            public void onAbonar(Compra compra) {
                mostrarDialogAbono(compra);
            }

            @Override
            public void onEditar(Compra compra) {
                mostrarDialogEditarCompra(compra);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        comprasViewModel.cargarComprasPorTarjeta(tarjetaId);

        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            adapter.setCompras(compras);
            actualizarTotal(compras, tvTotal);
        });

        tarjetasViewModel.getTarjetas().observe(getViewLifecycleOwner(), tarjetas -> {
            adapter.setTarjetas(tarjetas);
            for (Tarjeta t : tarjetas) {
                if (t.getId().equals(tarjetaId)) {
                    tarjetaSeleccionada[0] = t;
                    if (t.isQuincenal()) {
                        tvDiaCorte.setText("Esquema: Quincenal");
                        tvDiaPago.setText("Pago: Cada 15 días");
                    } else {
                        tvDiaPago.setText("Día " + t.getFechaPago());
                        tvDiaCorte.setText("Día " + t.getFechaCorte());
                    }
                    break;
                }
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_compra);
        if (fab != null) {
            fab.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_detalleTarjeta_to_agregarCompra));
        }
    }

    // 👇 Modificamos el método para incluir el Total del Mes 👇
    private void actualizarTotal(List<Compra> compras, TextView tvTotal) {
        double total = 0;
        double totalMes = 0; // NUEVO

        for (Compra c : compras) {
            total += c.getSaldoPendiente();

            // Calculamos lo de este mes
            double pagoMes = (c.getTipoPago() != null && c.getTipoPago().equals("Contado")) ?
                    c.getSaldoPendiente() : Math.min(c.getMontoPorCuota(), c.getSaldoPendiente());
            totalMes += pagoMes;
        }

        tvTotal.setText(String.format("$%,.2f", total));

        // Buscamos el TextView nuevo y le ponemos el texto
        TextView tvTotalMes = getView() != null ? getView().findViewById(R.id.tv_total_mes_tarjeta) : null;
        if (tvTotalMes != null) {
            tvTotalMes.setText(String.format("Mes: $%,.2f", totalMes));
        }
    }

    private void mostrarDialogAbono(Compra compra) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_abono, null);
        TextView tvDesc = dialogView.findViewById(R.id.tv_descripcion_abono);
        tvDesc.setText("Saldo pendiente: $" + String.format("%,.2f", compra.getSaldoPendiente()));

        // Calculamos la cuota exacta de este periodo para el botón automático
        double cuota = (compra.getTipoPago() != null && !compra.getTipoPago().equals("Contado"))
                ? Math.min(compra.getMontoPorCuota(), compra.getSaldoPendiente())
                : 0;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Abono — " + compra.getDescripcion())
                .setView(dialogView)
                .setPositiveButton("Registrar", (dialog, which) -> {
                    TextInputEditText etMonto = dialogView.findViewById(R.id.et_monto_abono);
                    TextInputEditText etNota  = dialogView.findViewById(R.id.et_nota_abono);

                    String montoStr = etMonto.getText().toString().trim();
                    String nota     = etNota.getText().toString().trim();

                    if (!montoStr.isEmpty()) {
                        double monto = Double.parseDouble(montoStr);
                        comprasViewModel.registrarAbono(compra.getId(), compra.getPersonaId(), monto, nota);
                    }
                })
                .setNegativeButton("Cancelar", null);

        // ¡LA MAGIA! Agregamos el botón rápido si la compra es a cuotas
        if (cuota > 0) {
            String tipo = (compra.getTipoPago() != null && compra.getTipoPago().equals("Quincenas")) ? "Quincena" : "Mes";
            builder.setNeutralButton("Pagar 1 " + tipo, (dialog, which) -> {
                // Llama al método automático del ViewModel que creamos en el paso anterior
                comprasViewModel.registrarPagoCuotaAutomatica(compra, compra.getPersonaId(), cuota, "Pago automático de 1 " + tipo);
            });
        }

        // Mostramos el diálogo
        builder.show();
    }

    // 👇 SE AGREGA EL DIALOGO DE EDICIÓN 👇
    // 👇 REEMPLAZA TODO ESTE MÉTODO EN DetalleTarjetaFragment.java 👇
    private void mostrarDialogEditarCompra(Compra compra) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_editar_compra, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.et_editar_nombre);
        TextInputEditText etTotal  = dialogView.findViewById(R.id.et_editar_total);
        TextInputEditText etCuota  = dialogView.findViewById(R.id.et_editar_cuota);
        TextInputEditText etCuotasTotales = dialogView.findViewById(R.id.et_editar_cuotas_totales);
        TextInputEditText etCuotasPagadas = dialogView.findViewById(R.id.et_editar_cuotas_pagadas);

        etNombre.setText(compra.getDescripcion());
        etTotal.setText(String.valueOf(compra.getMonto()));
        etCuota.setText(String.valueOf(compra.getMontoPorCuota()));

        if (etCuotasTotales != null) etCuotasTotales.setText(String.valueOf(compra.getTotalCuotas()));
        if (etCuotasPagadas != null) etCuotasPagadas.setText(String.valueOf(compra.getCuotasPagadas()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar Compra")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombreStr = etNombre.getText().toString().trim();
                    String totalStr  = etTotal.getText().toString().trim();
                    String cuotaStr  = etCuota.getText().toString().trim();
                    String totalesStr = etCuotasTotales != null ? etCuotasTotales.getText().toString().trim() : "0";
                    String pagadasStr = etCuotasPagadas != null ? etCuotasPagadas.getText().toString().trim() : "0";

                    if (!nombreStr.isEmpty() && !totalStr.isEmpty() && !cuotaStr.isEmpty()) {
                        double nuevoTotal = Double.parseDouble(totalStr);
                        double nuevaCuota = Double.parseDouble(cuotaStr);
                        int nuevasTotales = totalesStr.isEmpty() ? 0 : Integer.parseInt(totalesStr);
                        int nuevasPagadas = pagadasStr.isEmpty() ? 0 : Integer.parseInt(pagadasStr);

                        // ¡AQUÍ ESTÁ LA SOLUCIÓN! Ya le mandamos los 8 parámetros
                        comprasViewModel.actualizarDatosCompra(
                                compra.getId(),
                                compra.getMonto(),
                                compra.getSaldoPendiente(),
                                nombreStr,
                                nuevoTotal,
                                nuevaCuota,
                                nuevasTotales,
                                nuevasPagadas
                        );
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Eliminar", (dialog, which) -> {
                    // Doble confirmación de seguridad
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("¿Eliminar compra?")
                            .setMessage("¿Estás seguro de que deseas borrar '" + compra.getDescripcion() + "'? Esta acción no se puede deshacer.")
                            .setPositiveButton("Sí, eliminar", (dialogConfirm, whichConfirm) -> {
                                comprasViewModel.eliminarCompra(compra.getId());
                                Toast.makeText(getContext(), "Compra eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .show();
    }

    private void confirmarEliminacionTarjeta(String tarjetaId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Eliminar tarjeta?")
                .setMessage("Esta acción no se puede deshacer. Se borrará la tarjeta y dejarás de ver su desglose.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    tarjetasViewModel.eliminarTarjeta(tarjetaId);
                    Toast.makeText(getContext(), "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
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

        etNombre.setText(tarjetaActual.getNombre());
        etBanco.setText(tarjetaActual.getBanco());
        etLimite.setText(String.valueOf(tarjetaActual.getLimite()));

        if(tarjetaActual.isQuincenal()) {
            etCorte.setVisibility(View.GONE);
            etPago.setVisibility(View.GONE);
        } else {
            etCorte.setText(String.valueOf(tarjetaActual.getFechaCorte()));
            etPago.setText(String.valueOf(tarjetaActual.getFechaPago()));
        }

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

                        if (!tarjetaActual.isQuincenal()) {
                            tarjetaActual.setFechaCorte(Integer.parseInt(etCorte.getText().toString()));
                            tarjetaActual.setFechaPago(Integer.parseInt(etPago.getText().toString()));
                        }

                        tarjetasViewModel.actualizarTarjeta(tarjetaActual);
                        Toast.makeText(getContext(), "Tarjeta actualizada", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}