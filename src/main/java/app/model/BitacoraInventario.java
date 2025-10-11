package app.model;

import java.time.LocalDateTime;

public class BitacoraInventario {
    private Integer id;                     // null al insertar
    private int idInventario;               // FK a InventarioFisico.id
    private String tipoMovimiento;          // Entrada / Salida / Ajuste
    private int cantidad;
    private LocalDateTime fechaMovimiento;
    private String observacion;
    private String usuarioResponsable;
    private int estado;                     // 1 = Activo, 0 = Inactivo

    public BitacoraInventario() {}

    public BitacoraInventario(Integer id, int idInventario, String tipoMovimiento, int cantidad,
                              LocalDateTime fechaMovimiento, String observacion, String usuarioResponsable, int estado) {
        this.id = id;
        this.idInventario = idInventario;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fechaMovimiento = fechaMovimiento;
        this.observacion = observacion;
        this.usuarioResponsable = usuarioResponsable;
        this.estado = estado;
    }

    public BitacoraInventario(int idInventario, String tipoMovimiento, int cantidad,
                              LocalDateTime fechaMovimiento, String observacion, String usuarioResponsable, int estado) {
        this(null, idInventario, tipoMovimiento, cantidad, fechaMovimiento, observacion, usuarioResponsable, estado);
    }

    // Getters
    public Integer getId() { return id; }
    public int getIdInventario() { return idInventario; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public String getObservacion() { return observacion; }
    public String getUsuarioResponsable() { return usuarioResponsable; }
    public int getEstado() { return estado; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setIdInventario(int idInventario) { this.idInventario = idInventario; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }
    public void setEstado(int estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "BitacoraInventario{id=" + id +
                ", idInventario=" + idInventario +
                ", tipoMovimiento='" + tipoMovimiento + '\'' +
                ", cantidad=" + cantidad +
                ", fechaMovimiento=" + fechaMovimiento +
                ", observacion='" + observacion + '\'' +
                ", usuario='" + usuarioResponsable + '\'' +
                ", estado=" + estado + '}';
    }
}
