 package com.example.finanzasclaras;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AgregarCompraFragment extends Fragment {

    private PersonasViewModel personasViewModel;
    private TarjetasViewModel tarjetasViewModel;
    private ComprasViewModel comprasViewModel;

    private List<Persona> listaPersonas = new ArrayList<>();
    private List<Tarjeta> listaTarjetas = new ArrayList<>();

    private Spinner spinnerPersona;
    private Spinner spinnerTarjeta;
    private Spinner spinnerTipoPago;
    private LinearLayout layoutCuotas;
    private TextInputEditText etMonto, etNumCuotas, etMontoCuota;

    private final String[] tiposDePago = {"Contado", "Meses Sin Intereses (MSI)", "Quincenas", "Meses con Intereses"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agregar_compra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etDescripcion = view.findViewById(R.id.et_descripcion);
        etMonto                         = view.findViewById(R.id.et_monto);
        etNumCuotas                     = view.findViewById(R.id.et_num_cuotas);
        etMontoCuota                    = view.findViewById(R.id.et_monto_cuota);
        spinnerPersona                  = view.findViewById(R.id.spinner_persona);
        spinnerTarjeta                  = view.findViewById(R.id.spinner_tarjeta);
        spinnerTipoPago                 = view.findViewById(R.id.spinner_tipo_pago);
        layoutCuotas                    = view.findViewById(R.id.layout_cuotas);
        MaterialButton btnGuardar       = view.findViewById(R.id.btn_guardar_compra);

        personasViewModel = new ViewModelProvider(this).get(PersonasViewModel.class);
        tarjetasViewModel = new ViewModelProvider(this).get(TarjetasViewModel.class);
        comprasViewModel  = new ViewModelProvider(this).get(ComprasViewModel.class);

        // Configurar Spinner Tipo de Pago
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposDePago);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoPago.setAdapter(adapterTipo);

        // Lógica de mostrar/ocultar y bloquear cuotas
        spinnerTipoPago.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = tiposDePago[position];
                if (tipo.equals("Contado")) {
                    layoutCuotas.setVisibility(View.GONE);
                } else {
                    layoutCuotas.setVisibility(View.VISIBLE);
                    etMontoCuota.setEnabled(true);
                    // Si es "Con Intereses", permitimos escribir la mensualidad manual
                    if (tipo.equals("Meses con Intereses")) {
                        calcularCuotaAutomatica();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Escuchar cambios de texto para calcular división en tiempo real
        TextWatcher calculadorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularCuotaAutomatica();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etMonto.addTextChangedListener(calculadorWatcher);
        etNumCuotas.addTextChangedListener(calculadorWatcher);

        // Llenar spinner de personas
        personasViewModel.getPersonas().observe(getViewLifecycleOwner(), personas -> {
            listaPersonas = personas;
            List<String> nombres = new ArrayList<>();
            for (Persona p : personas) nombres.add(p.getNombre());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPersona.setAdapter(adapter);
        });

        // Llenar spinner de tarjetas
        tarjetasViewModel.getTarjetas().observe(getViewLifecycleOwner(), tarjetas -> {
            listaTarjetas = tarjetas;
            List<String> nombres = new ArrayList<>();
            for (Tarjeta t : tarjetas) nombres.add(t.getNombre());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTarjeta.setAdapter(adapter);
        });

        // Guardar
        btnGuardar.setOnClickListener(v -> {
            String descripcion = etDescripcion.getText().toString().trim();
            String montoStr    = etMonto.getText().toString().trim();
            String tipoPago    = spinnerTipoPago.getSelectedItem().toString();

            if (descripcion.isEmpty() || montoStr.isEmpty() || listaPersonas.isEmpty() || listaTarjetas.isEmpty()) {
                Toast.makeText(getContext(), "Faltan datos", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto = Double.parseDouble(montoStr);
            int cuotas = 1;
            int cuotasPagadas = 0;
            double montoPorCuota = monto;

            if (!tipoPago.equals("Contado")) {
                String cuotasStr = etNumCuotas.getText().toString().trim();
                String montoCuotaStr = etMontoCuota.getText().toString().trim();

                // Leemos el nuevo campo de tu XML
                TextInputEditText etCuotasPagadas = view.findViewById(R.id.et_cuotas_pagadas);
                String yaPagadasStr = etCuotasPagadas != null ? etCuotasPagadas.getText().toString().trim() : "";

                if (cuotasStr.isEmpty() || montoCuotaStr.isEmpty()) {
                    Toast.makeText(getContext(), "Ingresa las cuotas", Toast.LENGTH_SHORT).show();
                    return;
                }

                cuotas = Integer.parseInt(cuotasStr);
                montoPorCuota = Double.parseDouble(montoCuotaStr);

                if (!yaPagadasStr.isEmpty()) {
                    cuotasPagadas = Integer.parseInt(yaPagadasStr);
                }
            }

            Persona persona  = listaPersonas.get(spinnerPersona.getSelectedItemPosition());
            Tarjeta tarjeta  = listaTarjetas.get(spinnerTarjeta.getSelectedItemPosition());

            // Llamada al ViewModel enviando TODAS las variables, incluyendo cuotasPagadas al final
            comprasViewModel.agregarCompra(persona.getId(), tarjeta.getId(), descripcion, monto, tipoPago, cuotas, montoPorCuota, cuotasPagadas);

            Toast.makeText(getContext(), "Compra guardada", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void calcularCuotaAutomatica() {
        String tipoPago = spinnerTipoPago.getSelectedItem().toString();
        // Solo calcular si no es manual
        if (tipoPago.equals("Meses Sin Intereses (MSI)") || tipoPago.equals("Quincenas")) {
            String montoStr = etMonto.getText().toString().trim();
            String cuotasStr = etNumCuotas.getText().toString().trim();

            if (!montoStr.isEmpty() && !cuotasStr.isEmpty()) {
                double monto = Double.parseDouble(montoStr);
                int cuotas = Integer.parseInt(cuotasStr);
                if (cuotas > 0) {
                    double cuotaCalc = monto / cuotas;
                    // Redondear a 2 decimales
                    etMontoCuota.setText(String.format(java.util.Locale.US, "%.2f", cuotaCalc));
                }
            } else {
                etMontoCuota.setText("");
            }
        }
    }
}