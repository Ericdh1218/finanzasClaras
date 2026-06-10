package com.example.finanzasclaras;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private PersonasViewModel personasViewModel;
    private ComprasViewModel comprasViewModel;
    private TarjetasViewModel tarjetasViewModel;

    private TextView tvTotalGeneral, tvDeudaPropia, tvDeudaTerceros;
    private List<Persona> listaPersonas = new ArrayList<>();
    private List<Compra> listaCompras = new ArrayList<>();
    private List<Tarjeta> listaTarjetas = new ArrayList<>();
    private UrgentesAdapter adapterUrgentes;
    private TextView tvStatusUrgentes;
    private TextView tvTituloUrgentes;
    private TextView tvTotalMes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI inicialización
        tvTotalGeneral  = view.findViewById(R.id.tv_total_general);
        tvDeudaPropia   = view.findViewById(R.id.tv_deuda_propia);
        tvDeudaTerceros = view.findViewById(R.id.tv_deuda_terceros);
        tvStatusUrgentes = view.findViewById(R.id.tv_status_urgentes);
        tvTituloUrgentes = view.findViewById(R.id.titulo_urgentes);
        tvTotalMes = view.findViewById(R.id.tv_total_del_mes);

        // Configuración segura del RecyclerView de Urgentes
        RecyclerView rvUrgentes = view.findViewById(R.id.rv_urgentes);
        if (rvUrgentes != null) {
            adapterUrgentes = new UrgentesAdapter(this::mostrarDialogAbono);
            rvUrgentes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvUrgentes.setAdapter(adapterUrgentes);
        }

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar);
        fab.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_agregarCompra));

        // Tabs
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_inicio);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager_inicio);
        viewPager.setAdapter(new HomePagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(position == 0 ? "Por persona" : "Por tarjeta")).attach();

        // ViewModels
        personasViewModel = new ViewModelProvider(this).get(PersonasViewModel.class);
        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);
        tarjetasViewModel = new ViewModelProvider(this).get(TarjetasViewModel.class);

        comprasViewModel.cargarTodasLasCompras();

        personasViewModel.getPersonas().observe(getViewLifecycleOwner(), personas -> {
            if (personas != null) {
                listaPersonas = personas;
                calcularDashboard();
            }
        });

        tarjetasViewModel.getTarjetas().observe(getViewLifecycleOwner(), tarjetas -> listaTarjetas = tarjetas);

        comprasViewModel.getCompras().observe(getViewLifecycleOwner(), compras -> {
            if (compras != null) {
                listaCompras = compras;
                calcularDashboard();
                filtrarUrgentes();
            }
        });
    }

    private void filtrarUrgentes() {
        List<Compra> urgentes = new ArrayList<>();
        Calendar hoy = Calendar.getInstance();
        Calendar limite = Calendar.getInstance();
        limite.add(Calendar.DAY_OF_YEAR, 10);

        for (Compra c : listaCompras) {
            // 1. Ignorar si la compra ya está completamente liquidada
            if (c.isLiquidada()) continue;

            // 2. Filtro Ninja: ¿Ya se pagó este periodo?
            if (c.getFechaUltimoPago() != null) {
                Calendar ultimoPago = Calendar.getInstance();
                ultimoPago.setTime(c.getFechaUltimoPago().toDate());

                if (c.getTipoPago() != null && c.getTipoPago().equals("Quincenas")) {
                    // Si es quincenal y pagó hace menos de 14 días, la escondemos
                    Calendar hace14Dias = Calendar.getInstance();
                    hace14Dias.add(Calendar.DAY_OF_YEAR, -14);
                    if (ultimoPago.after(hace14Dias)) continue;
                } else {
                    // Si es mensual y pagó en este mismo mes, la escondemos
                    if (ultimoPago.get(Calendar.MONTH) == hoy.get(Calendar.MONTH) &&
                            ultimoPago.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)) {
                        continue;
                    }
                }
            }

            // 3. Cálculo de días urgentes
            int diaPago = obtenerDiaPagoDeTarjeta(c.getTarjetaId());
            Calendar fechaPago = Calendar.getInstance();
            fechaPago.set(Calendar.DAY_OF_MONTH, diaPago);
            if (fechaPago.before(hoy)) fechaPago.add(Calendar.MONTH, 1);

            if (fechaPago.after(hoy) && fechaPago.before(limite)) {
                String nombre = "Desconocido";
                for (Persona p : listaPersonas) {
                    if (p.getId().equals(c.getPersonaId())) {
                        nombre = p.getNombre();
                        break;
                    }
                }
                c.setNombrePersonaTemporal(nombre);
                urgentes.add(c);
            }
        }

        // 4. Actualización de la UI
        RecyclerView rvUrgentes = getView() != null ? getView().findViewById(R.id.rv_urgentes) : null;
        if (rvUrgentes != null) {
            if (urgentes.isEmpty()) {
                rvUrgentes.setVisibility(View.GONE);
                tvTituloUrgentes.setVisibility(View.GONE);
                tvStatusUrgentes.setVisibility(View.VISIBLE);
            } else {
                rvUrgentes.setVisibility(View.VISIBLE);
                tvStatusUrgentes.setVisibility(View.GONE);
                tvTituloUrgentes.setVisibility(View.VISIBLE);
                adapterUrgentes.setCompras(urgentes);
            }
        }
    }

    private void mostrarDialogAbono(Compra compra) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_abono, null);

        // Calculamos la cuota exacta de este periodo
        double cuota = (compra.getTipoPago() != null && !compra.getTipoPago().equals("Contado"))
                ? Math.min(compra.getMontoPorCuota(), compra.getSaldoPendiente())
                : 0;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Abono — " + compra.getDescripcion())
                .setView(dialogView)
                .setPositiveButton("Registrar", (dialog, which) -> {
                    TextInputEditText etMonto = dialogView.findViewById(R.id.et_monto_abono);
                    if (etMonto != null && etMonto.getText() != null && !etMonto.getText().toString().isEmpty()) {
                        comprasViewModel.registrarAbono(compra.getId(), compra.getPersonaId(),
                                Double.parseDouble(etMonto.getText().toString()), "");
                    }
                })
                .setNegativeButton("Cancelar", null);

        // Agregar el botón extra a la izquierda si la cuota aplica
        if (cuota > 0) {
            String tipo = (compra.getTipoPago() != null && compra.getTipoPago().equals("Quincenas")) ? "Quincena" : "Mes";
            builder.setNeutralButton("Pagar 1 " + tipo, (dialog, which) -> {
                comprasViewModel.registrarPagoCuotaAutomatica(compra, compra.getPersonaId(), cuota, "Pago automático de 1 " + tipo);
            });
        }

        // Importante: Mostrar el diálogo AL FINAL, después de configurarlo
        builder.show();
    }

    private int obtenerDiaPagoDeTarjeta(String tarjetaId) {
        for (Tarjeta t : listaTarjetas) {
            if (t.getId().equals(tarjetaId)) return t.getFechaPago();
        }
        return 1;
    }

    private void calcularDashboard() {
        if (listaPersonas.isEmpty() || listaCompras.isEmpty()) return;

        double deudaMia = 0, deudaOtros = 0, totalGeneral = 0;
        double totalDelMes = 0;

        for (Compra compra : listaCompras) {
            totalGeneral += compra.getSaldoPendiente();
            double aPagarEsteMes = (compra.getTipoPago() != null && compra.getTipoPago().equals("Contado")) ?
                    compra.getSaldoPendiente() : Math.min(compra.getMontoPorCuota(), compra.getSaldoPendiente());
            totalDelMes += aPagarEsteMes;

            String nombre = "";
            for (Persona p : listaPersonas) {
                if (p.getId().equals(compra.getPersonaId())) {
                    nombre = p.getNombre();
                    break;
                }
            }
            if (nombre.toLowerCase().contains("eric")) deudaMia += compra.getSaldoPendiente();
            else deudaOtros += compra.getSaldoPendiente();
        }

        tvDeudaPropia.setText(String.format("$%,.2f", deudaMia));
        tvDeudaTerceros.setText(String.format("$%,.2f", deudaOtros));
        tvTotalGeneral.setText(String.format("$%,.2f", totalGeneral));
        if (tvTotalMes != null) {
            tvTotalMes.setText(String.format("$%,.2f", totalDelMes));
        }
    }
}