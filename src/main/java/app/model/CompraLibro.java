package app.model;

import java.util.Date;

public class CompraLibro {

    private Integer id;             // PK
    private Integer idSolicitud;    // FK de SolicitudCompra
    private String proveedor;       // Nombre del proveedor
    private Double costoTotal;      // Costo total de la compra
    private Date fechaRecepcion;    // Fecha de recepción (puede ser null)
    private Integer estado;         // 1 = Activo, 0 = Inactivo

    // Constructor completo (para carga desde BD)
    public CompraLibro(Integer id, Integer idSolicitud, String proveedor, Double costoTotal, Date fechaRecepcion, Integer estado) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
        this.fechaRecepcion = fechaRecepcion;
        this.estado = estado;
    }

    // Constructor para insertar nueva compra
    public CompraLibro(Integer idSolicitud, String proveedor, Double costoTotal, Date fechaRecepcion, Integer estado) {
        this.idSolicitud = idSolicitud;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
        this.fechaRecepcion = fechaRecepcion;
        this.estado = estado;
    }

    // Constructor vacío por si se necesita
    public CompraLibro() {}

    // ==========================
    // GETTERS & SETTERS
    // ==========================
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Compra #" + id +
                " | Solicitud " + idSolicitud +
                " | Proveedor: " + proveedor +
                " | Q" + costoTotal;
    }
}
