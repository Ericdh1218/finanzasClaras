package com.example.finanzasclaras;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ComprasViewModel extends ViewModel {

    private final MutableLiveData<List<Compra>> compras = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final FirestoreRepository repo = FirestoreRepository.getInstance();

    public void cargarComprasPorPersona(String personaId) {
        repo.getComprasByPersona(personaId, new FirestoreRepository.OnListListener<Compra>() {
            @Override
            public void onSuccess(List<Compra> lista) {
                compras.setValue(lista);
            }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void cargarComprasPorTarjeta(String tarjetaId) {
        repo.getComprasByTarjeta(tarjetaId, new FirestoreRepository.OnListListener<Compra>() {
            @Override
            public void onSuccess(List<Compra> lista) {
                compras.setValue(lista);
            }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    // 1. Alineamos el orden de los parámetros para que coincida exactamente con cómo los envías desde la interfaz.
    public void agregarCompra(String personaId, String tarjetaId, String descripcion, double monto, String tipoPago, int totalCuotas, double montoPorCuota, int cuotasPagadas) {

        // 2. Candado de seguridad para el nombre fantasma.
        // Si el usuario olvidó poner el nombre, forzamos un título por defecto.
        if (descripcion == null || descripcion.trim().isEmpty()) {
            descripcion = "Compra sin nombre";
        }

        // 3. Calculamos el saldo inicial restando las cuotas que ya se hayan dado.
        double saldoInicial = monto;
        if (cuotasPagadas > 0 && montoPorCuota > 0) {
            saldoInicial = monto - (cuotasPagadas * montoPorCuota);
        }

        // Evitamos que un error matemático genere deudas negativas.
        if (saldoInicial < 0) {
            saldoInicial = 0;
        }

        com.google.firebase.Timestamp fechaActual = com.google.firebase.Timestamp.now();

        // 4. EL PASO CRUCIAL: Pasamos los parámetros al constructor de Compra en el orden EXACTO que éste exige.
        // Fíjate cómo tarjetaId y personaId están cruzados respecto a cómo los recibe este método,
        // esto es para que encajen perfectamente en la clase Compra.
        Compra nuevaCompra = new Compra(
                null,           // id
                descripcion,    // descripcion (¡El nombre real!)
                tarjetaId,      // tarjetaId
                personaId,      // personaId
                monto,          // monto
                saldoInicial,   // saldoPendiente
                tipoPago,       // tipoPago
                totalCuotas,    // totalCuotas
                cuotasPagadas,  // cuotasPagadas
                montoPorCuota,  // montoPorCuota
                fechaActual,    // fecha
                false           // liquidada
        );

        FirestoreRepository.getInstance().addCompra(nuevaCompra, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() {}
            @Override
            public void onError(Exception e) {}
        });
    }

    public void registrarAbono(String compraId, String personaId, double monto, String nota) {
        // Guardamos el registro del abono en el historial
        Abono abono = new Abono(null, compraId, personaId, monto, Timestamp.now(), nota);

        repo.addAbono(abono, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() {
                // Buscamos la compra a la que le estamos abonando
                List<Compra> listaActual = compras.getValue();
                if (listaActual != null) {
                    for (Compra c : listaActual) {
                        if (c.getId() != null && c.getId().equals(compraId)) {

                            // Matemáticas: Restamos el abono al saldo pendiente
                            double nuevoSaldo = c.getSaldoPendiente() - monto;

                            // Si el saldo es 0 o menor, activamos el interruptor del historial
                            boolean estaLiquidada = (nuevoSaldo <= 0);

                            // Le avisamos a Firebase de los nuevos cambios
                            FirebaseFirestore.getInstance()
                                    .collection("compras")
                                    .document(compraId)
                                    .update(
                                            "saldoPendiente", Math.max(0, nuevoSaldo), // Evitamos saldos negativos
                                            "liquidada", estaLiquidada
                                    )
                                    .addOnFailureListener(e -> error.setValue(e.getMessage()));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void cargarTodasLasCompras() {
        repo.getTodasLasComprasActivas(new FirestoreRepository.OnListListener<Compra>() {
            @Override
            public void onSuccess(List<Compra> lista) {
                compras.setValue(lista);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }
    public void cargarHistorialLiquidadas() {
        FirebaseFirestore.getInstance().collection("compras")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Compra> listaLiquidadas = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Compra c = doc.toObject(Compra.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            // Filtramos directamente aquí las que ya se pagaron
                            if (c.isLiquidada() || c.getSaldoPendiente() <= 0) {
                                listaLiquidadas.add(c);
                            }
                        }
                    }
                    compras.setValue(listaLiquidadas);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
    public void actualizarDatosCompra(String compraId, double montoOriginal, double saldoOriginal,
                                      String nuevaDesc, double nuevoMonto, double nuevaCuota,
                                      int nuevasTotales, int nuevasPagadas) {

        double nuevoSaldo;

        if (nuevasTotales > 0) {
            // ¡LA SOLUCIÓN! Calculamos el saldo estrictamente basado en las cuotas indicadas
            double dineroYaPagadoReal = nuevasPagadas * nuevaCuota;
            nuevoSaldo = nuevoMonto - dineroYaPagadoReal;
        } else {
            // Si es de contado, mantenemos la lógica anterior
            double dineroYaPagado = montoOriginal - saldoOriginal;
            nuevoSaldo = nuevoMonto - dineroYaPagado;
        }

        // Evitamos que el saldo quede en números negativos por algún error de dedo
        nuevoSaldo = Math.max(0, nuevoSaldo);
        boolean estaLiquidada = (nuevoSaldo <= 0);

        FirebaseFirestore.getInstance().collection("compras").document(compraId)
                .update(
                        "descripcion", nuevaDesc,
                        "monto", nuevoMonto,
                        "montoPorCuota", nuevaCuota,
                        "totalCuotas", nuevasTotales,
                        "cuotasPagadas", nuevasPagadas,
                        "saldoPendiente", nuevoSaldo, // Guardamos el saldo corregido
                        "liquidada", estaLiquidada
                )
                .addOnFailureListener(e -> error.setValue("Error al editar: " + e.getMessage()));
    }

    public void registrarPagoCuotaAutomatica(Compra compra, String personaId, double monto, String nota) {
        // 1. Primero registramos el abono en el historial igual que siempre
        registrarAbono(compra.getId(), personaId, monto, nota);

        // 2. Ahora preparamos los nuevos datos de la compra
        double nuevoSaldo = compra.getSaldoPendiente() - monto;
        int nuevasPagadas = compra.getCuotasPagadas() + 1;
        boolean estaLiquidada = nuevoSaldo <= 0;

        // 3. Actualizamos la base de datos con las cuotas +1 y la marca de tiempo de HOY
        FirebaseFirestore.getInstance().collection("compras").document(compra.getId())
                .update(
                        "saldoPendiente", Math.max(0, nuevoSaldo),
                        "cuotasPagadas", nuevasPagadas,
                        "liquidada", estaLiquidada,
                        "fechaUltimoPago", com.google.firebase.firestore.FieldValue.serverTimestamp() // Sello de tiempo de Firebase
                )
                .addOnFailureListener(e -> error.setValue("Error al automatizar cuota: " + e.getMessage()));
    }
    public void eliminarCompra(String compraId) {
        FirebaseFirestore.getInstance().collection("compras").document(compraId)
                .delete()
                .addOnFailureListener(e -> error.setValue("Error al eliminar compra: " + e.getMessage()));
    }
    public LiveData<List<Compra>> getCompras() { return compras; }
    public LiveData<String> getError() { return error; }
}