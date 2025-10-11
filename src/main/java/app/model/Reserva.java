package app.model;

import java.util.Date;

public class Reserva {
    private Integer id;        // ID de la reserva, null al insertar
    private int idCliente;     // FK a Cliente.id
    private int idLibro;       // FK a Libro.id
    private Date fechaReserva; // Fecha en que se realiza la reserva
    private int estadoReserva; // 1 = Activa, 2 = Vencida, 3 = Retirada
    private int posicionCola;  // Posición en la cola de reserva

    // Constructor vacío
    public Reserva() {}

    // Constructor completo
    public Reserva(Integer id, int idCliente, int idLibro, Date fechaReserva, int estadoReserva, int posicionCola) {
        this.id = id;
        this.idCliente = idCliente;
        this.idLibro = idLibro;
        this.fechaReserva = fechaReserva;
        this.estadoReserva = estadoReserva;
        this.posicionCola = posicionCola;
    }

    // Constructor para insertar
    public Reserva(int idCliente, int idLibro, Date fechaReserva, int estadoReserva, int posicionCola) {
        this(null, idCliente, idLibro, fechaReserva, estadoReserva, posicionCola);
    }

    // Getters
    public Integer getId() { return id; }
    public int getIdCliente() { return idCliente; }
    public int getIdLibro() { return idLibro; }
    public Date getFechaReserva() { return fechaReserva; }
    public int getEstadoReserva() { return estadoReserva; }
    public int getPosicionCola() { return posicionCola; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public void setFechaReserva(Date fechaReserva) { this.fechaReserva = fechaReserva; }
    public void setEstadoReserva(int estadoReserva) { this.estadoReserva = estadoReserva; }
    public void setPosicionCola(int posicionCola) { this.posicionCola = posicionCola; }

    @Override
    public String toString() {
        return "Reserva{id=" + id +
                ", idCliente=" + idCliente +
                ", idLibro=" + idLibro +
                ", fechaReserva=" + fechaReserva +
                ", estadoReserva=" + estadoReserva +
                ", posicionCola=" + posicionCola +
                '}';
    }
}
