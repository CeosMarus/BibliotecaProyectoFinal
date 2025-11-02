package app.model;

import java.util.Date;

public class Devolucion {

    private Integer id;
    private int idPrestamo;
    private Date fechaDevolucion;
    private String estadoCopia;    // Bueno / Dañado / Perdido
    private String observaciones;
    private int idUsuario;
    private int estado; // 1 activo, 0 anulado

    public Devolucion() {}

    public Devolucion(Integer id, int idPrestamo, Date fechaDevolucion, String estadoCopia,
                      String observaciones, int idUsuario, int estado) {
        this.id = id;
        this.idPrestamo = idPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estadoCopia = estadoCopia;
        this.observaciones = observaciones;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    public Devolucion(int idPrestamo, Date fechaDevolucion, String estadoCopia,
                      String observaciones, int idUsuario) {
        this(null, idPrestamo, fechaDevolucion, estadoCopia, observaciones, idUsuario, 1);
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }

    public Date getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Date fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstadoCopia() { return estadoCopia; }
    public void setEstadoCopia(String estadoCopia) { this.estadoCopia = estadoCopia; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Devolucion{" +
                "id=" + id +
                ", idPrestamo=" + idPrestamo +
                ", fechaDevolucion=" + fechaDevolucion +
                ", estadoCopia='" + estadoCopia + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", idUsuario=" + idUsuario +
                ", estado=" + estado +
                '}';
    }
}