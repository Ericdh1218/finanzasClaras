package com.example.finanzasclaras;
import com.google.firebase.Timestamp;

public class Abono {

    private String id;
    private String compraId;        // a qué compra pertenece este abono
    private String personaId;       // quién abonó (para filtrar rápido)
    private double monto;           // cuánto abonó
    private Timestamp fecha;
    private String nota;            // opcional: "me pagó por transferencia"

    public Abono() {}

    public Abono(String id, String compraId, String personaId, double monto,
                 Timestamp fecha, String nota) {
        this.id = id;
        this.compraId = compraId;
        this.personaId = personaId;
        this.monto = monto;
        this.fecha = fecha;
        this.nota = nota;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompraId() { return compraId; }
    public void setCompraId(String compraId) { this.compraId = compraId; }
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}