package app.model;

import java.util.Date;

public class CompraLibro {

    private int id;
    private int idSolicitud;
    private String proveedor;
    private double costoTotal;
    private Date fechaRecepcion;
    private int estado; // 1 = activo, 0 = desactivado

    // 🟢 Constructor vacío
    public CompraLibro() {}

    // 🟢 Constructor completo
    public CompraLibro(int id, int idSolicitud, String proveedor, double costoTotal, Date fechaRecepcion, int estado) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
        this.fechaRecepcion = fechaRecepcion;
        this.estado = estado;
    }

    // 🟢 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🟢 Método para mostrar información básica (opcional)
    @Override
    public String toString() {
        return "CompraLibro{" +
                "id=" + id +
                ", idSolicitud=" + idSolicitud +
                ", proveedor='" + proveedor + '\'' +
                ", costoTotal=" + costoTotal +
                ", fechaRecepcion=" + fechaRecepcion +
                ", estado=" + estado +
                '}';
    }
}
