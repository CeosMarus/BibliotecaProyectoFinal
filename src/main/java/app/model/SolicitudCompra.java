package app.model;

import java.util.Date;

public class SolicitudCompra {

    private Integer id;
    private Date fecha;
    private Integer idUsuario;
    private Integer idLibro;
    private Integer cantidad;
    private Double costoUnitario;
    private Integer estado; // 1=Pendiente, 2=Aprobada, 3=Rechazada, 0=Eliminada

    /** ✅ Constructor para insertar nuevas solicitudes */
    public SolicitudCompra(Date fecha, Integer idUsuario, Integer idLibro,
                           Integer cantidad, Double costoUnitario, Integer estado) {
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idLibro = idLibro;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.estado = estado;
    }

    /** ✅ Constructor para obtener desde BD */
    public SolicitudCompra(Integer id, Date fecha, Integer idUsuario, Integer idLibro,
                           Integer cantidad, Double costoUnitario, Integer estado) {
        this.id = id;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idLibro = idLibro;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.estado = estado;
    }

    // ===== ✅ Getters =====
    public Integer getId() { return id; }
    public Date getFecha() { return fecha; }
    public Integer getIdUsuario() { return idUsuario; }
    public Integer getIdLibro() { return idLibro; }
    public Integer getCantidad() { return cantidad; }
    public Double getCostoUnitario() { return costoUnitario; }
    public Integer getEstado() { return estado; }

    // ===== ✅ Estados legibles =====
    public String getEstadoTexto() {
        return switch (estado) {
            case 1 -> "Pendiente";
            case 2 -> "Aprobada";
            case 3 -> "Rechazada";
            default -> "Eliminada";
        };
    }

    // ✅ Para combos si deseas mostrar en UI
    @Override
    public String toString() {
        return "Solicitud #" + id + " - " + getEstadoTexto();
    }
}
