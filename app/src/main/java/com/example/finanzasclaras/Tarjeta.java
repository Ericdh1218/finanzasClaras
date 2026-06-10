package com.example.finanzasclaras;

public class Tarjeta {

    private String id;
    private String nombre;
    private String banco;
    private double limite;
    private int fechaCorte;
    private int fechaPago;
    private Boolean esQuincenal;


    public Tarjeta() {}

    public Tarjeta(String id, String nombre, String banco, double limite, int fechaCorte, int fechaPago, Boolean esQuincenal) {
        this.id = id;
        this.nombre = nombre;
        this.banco = banco;
        this.limite = limite;
        this.fechaCorte = fechaCorte;
        this.fechaPago = fechaPago;
        this.esQuincenal = esQuincenal;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public double getLimite() { return limite; }
    public void setLimite(double limite) { this.limite = limite; }
    public int getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(int fechaCorte) { this.fechaCorte = fechaCorte; }
    public int getFechaPago() { return fechaPago; }
    public void setFechaPago(int fechaPago) { this.fechaPago = fechaPago; }
    public boolean isQuincenal() {
        // Si el valor es nulo (tarjeta vieja), asumimos que es falso (corte mensual)
        if (esQuincenal == null) {
            return false;
        }
        return esQuincenal;
    }

    public void setEsQuincenal(Boolean esQuincenal) {
        this.esQuincenal = esQuincenal;
    }
}