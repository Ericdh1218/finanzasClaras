package com.example.finanzasclaras;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetallePersonaFragment extends Fragment {

    private ComprasViewModel comprasViewModel;
    private TarjetasViewModel tarjetasViewModel;
    private PersonasViewModel personasViewModel;
    private ComprasAdapter adapter;
    private String personaId;
    private String personaNombre;

    private List<Compra> comprasActuales = new ArrayList<>();
    private List<Tarjeta> tarjetasActuales = new ArrayList<>(); // <-- Nueva lista para buscar las fechas

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_persona, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            personaId     = getArguments().getString("personaId");
            personaNombre = getArguments().getString("personaNombre");
        }

        final Persona[] personaSeleccionada = new Persona[1];

        TextView tvNombre          = view.findViewById(R.id.tv_nombre_detalle);
        TextView tvTotalPendiente  = view.findViewById(R.id.tv_total_pendiente);
        TextView tvNumCompras      = view.findViewById(R.id.tv_num_compras_detalle);
        RecyclerView rv            = view.findViewById(R.id.rv_compras);

        tvNombre.setText(personaNombre);

        // Agregamos los métodos onAbonar y onEditar que corregimos antes
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

        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);
        tarjetasViewModel = new ViewModelProvider(this).get(TarjetasViewModel.class);
        personasViewModel = new ViewModelProvider(this).get(PersonasViewModel.class);

        comprasViewModel.cargarComprasPorPersona(personaId);

        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) {
                this.comprasActuales = compras;
                adapter.setCompras(compras);
                actualizarResumen(compras, tvTotalPendiente, tvNumCompras);
            }
        });

        // Guardamos las tarjetas para poder leer sus fechas
        tarjetasViewModel.getTarjetas().observe(getViewLifecycleOwner(), tarjetas -> {
            if (tarjetas != null) {
                this.tarjetasActuales = tarjetas;
                adapter.setTarjetas(tarjetas);
            }
        });

        personasViewModel.getPersonas().observe(getViewLifecycleOwner(), personas -> {
            for (Persona p : personas) {
                if (p.getId().equals(personaId)) {
                    personaSeleccionada[0] = p;
                    tvNombre.setText(p.getNombre());
                    break;
                }
            }
        });


        ImageButton btnCompartir = view.findViewById(R.id.btn_compartir_reporte);
        if (btnCompartir != null) {
            btnCompartir.setOnClickListener(v -> {
                if (comprasActuales == null || comprasActuales.isEmpty()) {
                    Toast.makeText(getContext(), "No hay compras pendientes para reportar", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder reporte = new StringBuilder();
                reporte.append("*REPORTE DE CUENTAS \n\n");
                reporte.append("Hola *").append(personaNombre).append("*, este es el desglose de tus compras pendientes de pago:\n\n");

                double totalDeuda = 0;
                double totalDelMes = 0; // NUEVA VARIABLE PARA SUMAR LO DE ESTE MES

                for (Compra c : comprasActuales) {
                    reporte.append("• *").append(c.getDescripcion()).append("*\n");
                    reporte.append("  Saldo restante: ").append(String.format("$%,.2f", c.getSaldoPendiente())).append("\n");

                    // Calculamos cuánto corresponde pagar este periodo para esta compra
                    double pagoActual = (c.getTipoPago() != null && c.getTipoPago().equals("Contado")) ?
                            c.getSaldoPendiente() : Math.min(c.getMontoPorCuota(), c.getSaldoPendiente());

                    // Sumamos este pago al total del mes
                    totalDelMes += pagoActual;

                    if (c.getTotalCuotas() > 0) {
                        reporte.append("  Esquema: ").append(c.getCuotasPagadas()).append("/").append(c.getTotalCuotas()).append(" pagos\n");

                        String tipoPeriodo = (c.getTipoPago() != null && c.getTipoPago().equals("Quincenas")) ? "esta quincena" : "este mes";
                        reporte.append("  Pago ").append(tipoPeriodo).append(": ").append(String.format("$%,.2f", pagoActual)).append("\n");
                    }

                    Tarjeta tarjetaAsociada = null;
                    if (tarjetasActuales != null) {
                        for (Tarjeta t : tarjetasActuales) {
                            if (t.getId().equals(c.getTarjetaId())) {
                                tarjetaAsociada = t;
                                break;
                            }
                        }
                    }

                    if (tarjetaAsociada != null) {
                        String fechaPagoSugerida = calcularFechaPagoIdeal(tarjetaAsociada);
                        reporte.append("  🗓 Pago sugerido: ").append(fechaPagoSugerida).append("\n");
                    }

                    reporte.append("\n");
                    totalDeuda += c.getSaldoPendiente();
                }

                reporte.append("-----------------------------\n");
                // AQUÍ AGREGAMOS LA NUEVA LÍNEA DEL TOTAL DEL MES
                reporte.append("📅 *TOTAL DEL MES:* ").append(String.format("$%,.2f", totalDelMes)).append("\n");
                reporte.append("💰 *TOTAL PENDIENTE:* ").append(String.format("$%,.2f", totalDeuda)).append("\n\n");
                reporte.append("_Enviado automáticamente desde mi app CardControl._");

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, reporte.toString());
                startActivity(Intent.createChooser(intent, "Compartir reporte con:"));
            });
        }

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_compra);
        if (fab != null) {
            fab.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_detallePersona_to_agregarCompra));
        }
    }

    // 👇 Modificamos el método para incluir el Total del Mes 👇
    private void actualizarResumen(List<Compra> compras, TextView tvTotal, TextView tvNum) {
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
        tvNum.setText(String.valueOf(compras.size()));

        // Buscamos el TextView nuevo y le ponemos el texto
        TextView tvTotalMes = getView() != null ? getView().findViewById(R.id.tv_total_mes_persona) : null;
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

                        // AQUÍ AGREGAMOS LOS DOS NUEVOS PARÁMETROS
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

    // 👇 LA MAGIA MATEMÁTICA: Calcula la próxima fecha de pago y le resta 1 día 👇
    private String calcularFechaPagoIdeal(Tarjeta tarjeta) {
        Calendar cal = Calendar.getInstance();
        int hoy = cal.get(Calendar.DAY_OF_MONTH);

        if (tarjeta.isQuincenal()) {
            if (hoy <= 14) {
                cal.set(Calendar.DAY_OF_MONTH, 14); // 1 día antes del 15
            } else if (hoy == 15) {
                // Si hoy es 15, el próximo pago es fin de mes (le restamos 1)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                int ultimoDia = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (hoy >= ultimoDia - 1) {
                    // Si ya estamos a un día de fin de mes, salta al 14 del otro mes
                    cal.add(Calendar.MONTH, 1);
                    cal.set(Calendar.DAY_OF_MONTH, 14);
                } else {
                    cal.set(Calendar.DAY_OF_MONTH, ultimoDia);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
        } else {
            int diaPagoLimite = tarjeta.getFechaPago();
            int diaIdeal = diaPagoLimite - 1;

            if (diaIdeal <= 0) {
                // Si se paga los días 1, la fecha ideal es el último día del mes anterior
                if (hoy >= diaPagoLimite) {
                    cal.add(Calendar.MONTH, 1);
                }
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                if (hoy > diaIdeal) {
                    cal.add(Calendar.MONTH, 1);
                }
                cal.set(Calendar.DAY_OF_MONTH, diaIdeal);
            }
        }

        // Formato: "10 de junio"
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd 'de' MMMM", new Locale("es", "MX"));
        return sdf.format(cal.getTime());
    }
}