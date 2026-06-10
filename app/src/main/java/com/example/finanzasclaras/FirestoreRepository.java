package com.example.finanzasclaras;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.finanzasclaras.Tarjeta;
import com.example.finanzasclaras.Persona;
import com.example.finanzasclaras.Compra;
import com.example.finanzasclaras.Abono;

import java.util.ArrayList;
import java.util.List;

public class FirestoreRepository {

    private static FirestoreRepository instance;
    private final FirebaseFirestore db;

    // Nombres de colecciones
    private static final String COL_TARJETAS = "tarjetas";
    private static final String COL_PERSONAS  = "personas";
    private static final String COL_COMPRAS   = "compras";
    private static final String COL_ABONOS    = "abonos";

    // Singleton: una sola instancia en toda la app
    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    // ─────────────────────────────────────────
    // TARJETAS
    // ─────────────────────────────────────────

    public void getTarjetas(OnListListener<Tarjeta> listener) {
        db.collection(COL_TARJETAS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Tarjeta> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Tarjeta t = doc.toObject(Tarjeta.class);
                        t.setId(doc.getId());
                        lista.add(t);
                    }
                    listener.onSuccess(lista);
                });
    }

    public void addTarjeta(Tarjeta tarjeta, OnActionListener listener) {
        db.collection(COL_TARJETAS)
                .add(tarjeta)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void deleteTarjeta(String tarjetaId, OnActionListener listener) {
        db.collection(COL_TARJETAS).document(tarjetaId)
                .delete()
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    // ─────────────────────────────────────────
    // PERSONAS
    // ─────────────────────────────────────────

    public void getPersonas(OnListListener<Persona> listener) {
        db.collection(COL_PERSONAS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Persona> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Persona p = doc.toObject(Persona.class);
                        p.setId(doc.getId());
                        lista.add(p);
                    }
                    listener.onSuccess(lista);
                });
    }

    public void addPersona(Persona persona, OnActionListener listener) {
        db.collection(COL_PERSONAS)
                .add(persona)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void deletePersona(String personaId, OnActionListener listener) {
        db.collection(COL_PERSONAS).document(personaId)
                .delete()
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    // ─────────────────────────────────────────
    // COMPRAS
    // ─────────────────────────────────────────

    public void getComprasByPersona(String personaId, OnListListener<Compra> listener) {
        db.collection(COL_COMPRAS)
                .whereEqualTo("personaId", personaId)
                .whereEqualTo("liquidada", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Compra> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Compra c = doc.toObject(Compra.class);
                        c.setId(doc.getId());
                        lista.add(c);
                    }
                    listener.onSuccess(lista);
                });
    }
    public void getTodasLasComprasActivas(OnListListener<Compra> listener) {
        db.collection(COL_COMPRAS)
                .whereEqualTo("liquidada", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Compra> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Compra c = doc.toObject(Compra.class);
                        c.setId(doc.getId());
                        lista.add(c);
                    }
                    listener.onSuccess(lista);
                });
    }

    public void getComprasByTarjeta(String tarjetaId, OnListListener<Compra> listener) {
        db.collection(COL_COMPRAS)
                .whereEqualTo("tarjetaId", tarjetaId)
                .whereEqualTo("liquidada", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Compra> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Compra c = doc.toObject(Compra.class);
                        c.setId(doc.getId());
                        lista.add(c);
                    }
                    listener.onSuccess(lista);
                });
    }

    public void addCompra(Compra compra, OnActionListener listener) {
        db.collection(COL_COMPRAS)
                .add(compra)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void deleteCompra(String compraId, OnActionListener listener) {
        db.collection(COL_COMPRAS).document(compraId)
                .delete()
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    // ─────────────────────────────────────────
    // ABONOS
    // ─────────────────────────────────────────

    public void addAbono(Abono abono, OnActionListener listener) {
        // 1. Guardar el abono
        db.collection(COL_ABONOS)
                .add(abono)
                .addOnSuccessListener(ref -> {
                    // 2. Actualizar saldoPendiente de la compra
                    actualizarSaldoCompra(abono.getCompraId(), abono.getMonto(), listener);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getAbonosByCompra(String compraId, OnListListener<Abono> listener) {
        db.collection(COL_ABONOS)
                .whereEqualTo("compraId", compraId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e);
                        return;
                    }
                    List<Abono> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Abono a = doc.toObject(Abono.class);
                        a.setId(doc.getId());
                        lista.add(a);
                    }
                    listener.onSuccess(lista);
                });
    }

    // ─────────────────────────────────────────
    // LÓGICA INTERNA
    // ─────────────────────────────────────────

    private void actualizarSaldoCompra(String compraId, double montoAbono, OnActionListener listener) {
        DocumentReference compraRef = db.collection(COL_COMPRAS).document(compraId);

        // Transacción para leer los datos actuales antes de actualizar
        db.runTransaction(transaction -> {
                    com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(compraRef);

                    double saldoActual = snapshot.getDouble("saldoPendiente");
                    double nuevoSaldo = saldoActual - montoAbono;
                    transaction.update(compraRef, "saldoPendiente", nuevoSaldo);

                    // --- LÓGICA DE CUOTAS INTELIGENTES ---
                    String tipoPago = snapshot.getString("tipoPago");
                    if (tipoPago != null && !tipoPago.equals("Contado")) {
                        Long cuotasPagadasActuales = snapshot.getLong("cuotasPagadas");
                        Double montoPorCuota = snapshot.getDouble("montoPorCuota");

                        if (cuotasPagadasActuales != null && montoPorCuota != null && montoPorCuota > 0) {
                            // Calculamos cuántas cuotas completas cubre este abono (redondeando por si hay decimales)
                            int cuotasCubiertas = (int) Math.round(montoAbono / montoPorCuota);
                            if (cuotasCubiertas > 0) {
                                transaction.update(compraRef, "cuotasPagadas", cuotasPagadasActuales + cuotasCubiertas);
                            }
                        }
                    }
                    // -------------------------------------

                    // Si quedó en 0 o menos, marcar como liquidada
                    if (nuevoSaldo <= 0) {
                        transaction.update(compraRef, "liquidada", true);
                    }
                    return null;
                })
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void updateTarjeta(Tarjeta tarjeta, OnActionListener listener) {
        db.collection(COL_TARJETAS).document(tarjeta.getId())
                .set(tarjeta)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void updatePersona(Persona persona, OnActionListener listener) {
        db.collection(COL_PERSONAS).document(persona.getId())
                .set(persona)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    // ─────────────────────────────────────────
    // INTERFACES CALLBACK
    // ─────────────────────────────────────────

    public interface OnListListener<T> {
        void onSuccess(List<T> lista);
        void onError(Exception e);
    }

    public interface OnActionListener {
        void onSuccess();
        void onError(Exception e);
    }
}