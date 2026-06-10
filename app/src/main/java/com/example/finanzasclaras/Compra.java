package com.example.finanzasclaras;

import com.google.firebase.Timestamp;

public class Compra {
    private String id;
    private String descripcion;
    private String tarjetaId;
    private String personaId;
    private double monto;
    private double saldoPendiente;

    // Variables de tu motor de cuotas
    private String tipoPago;
    private int totalCuotas;
    private int cuotasPagadas;
    private double montoPorCuota;
    private Timestamp fecha;

    // Variable para el historial
    private boolean liquidada;
    private String nombrePersonaTemporal;
    private Timestamp fechaUltimoPago;

    // Constructor vacío (Requerido por Firebase)
    public Compra() {}

    // Constructor completo
    public Compra(String id, String descripcion, String tarjetaId, String personaId,
                  double monto, double saldoPendiente, String tipoPago,
                  int totalCuotas, int cuotasPagadas, double montoPorCuota,
                  Timestamp fecha, boolean liquidada) {
        this.id = id;
        this.descripcion = descripcion;
        this.tarjetaId = tarjetaId;
        this.personaId = personaId;
        this.monto = monto;
        this.saldoPendiente = saldoPendiente;
        this.tipoPago = tipoPago;
        this.totalCuotas = totalCuotas;
        this.cuotasPagadas = cuotasPagadas;
        this.montoPorCuota = montoPorCuota;
        this.fecha = fecha;
        this.liquidada = liquidada;
    }

    // --- GETTERS Y SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTarjetaId() { return tarjetaId; }
    public void setTarjetaId(String tarjetaId) { this.tarjetaId = tarjetaId; }

    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public double getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(double saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public int getTotalCuotas() { return totalCuotas; }
    public void setTotalCuotas(int totalCuotas) { this.totalCuotas = totalCuotas; }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) { this.cuotasPagadas = cuotasPagadas; }

    public double getMontoPorCuota() { return montoPorCuota; }
    public void setMontoPorCuota(double montoPorCuota) { this.montoPorCuota = montoPorCuota; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public boolean isLiquidada() { return liquidada; }
    public void setLiquidada(boolean liquidada) { this.liquidada = liquidada; }
    public String getNombrePersonaTemporal() { return nombrePersonaTemporal; }
    public void setNombrePersonaTemporal(String nombre) { this.nombrePersonaTemporal = nombre; }
    public Timestamp getFechaUltimoPago() { return fechaUltimoPago; }
    public void setFechaUltimoPago(Timestamp fechaUltimoPago) { this.fechaUltimoPago = fechaUltimoPago; }
}