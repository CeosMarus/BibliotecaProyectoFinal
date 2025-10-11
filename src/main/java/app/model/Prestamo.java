package app.model;

import java.util.Date;

public class Prestamo {

    private Integer id;              // Autogenerado
    private int idCliente;           // FK → Cliente.id
    private int idEjemplar;          // FK → Ejemplar.id
    private Date fechaPrestamo;      // Fecha de préstamo
    private Date fechaVencimiento;   // Fecha de devolución prevista
    private Date fechaDevolucion;    // Fecha de devolución real
    private int estado;              // 1 = Activo, 0 = Devuelto / Inactivo

    // Constructor vacío
    public Prestamo() {
    }

    // Constructor completo
    public Prestamo(Integer id, int idCliente, int idEjemplar, Date fechaPrestamo, Date fechaVencimiento, Date fechaDevolucion, int estado) {
        this.id = id;
        this.idCliente = idCliente;
        this.idEjemplar = idEjemplar;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    // Constructor sin id (para inserción)
    public Prestamo(int idCliente, int idEjemplar, Date fechaPrestamo, Date fechaVencimiento, int estado) {
        this(null, idCliente, idEjemplar, fechaPrestamo, fechaVencimiento, null, estado);
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(int idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(Date fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // Método auxiliar
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Devuelto";
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", idCliente=" + idCliente +
                ", idEjemplar=" + idEjemplar +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaVencimiento=" + fechaVencimiento +
                ", fechaDevolucion=" + fechaDevolucion +
                ", estado=" + getEstadoDescripcion() +
                '}';
    }
}
