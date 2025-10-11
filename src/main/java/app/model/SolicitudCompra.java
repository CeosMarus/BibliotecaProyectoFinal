package app.model;

import java.util.Date;

public class SolicitudCompra {

    // 🧾 Atributos
    private Integer id;          // null al insertar (autogenerado)
    private Date fecha;
    private int idUsuario;       // FK → Usuario.id
    private int idLibro;         // FK → Libro.id
    private int cantidad;
    private double costoUnitario;
    private int estado;          // 1: Pendiente/Aprobada/Rechazada, 0: Eliminado (lógica)

    // 🏗️ Constructor vacío
    public SolicitudCompra() {
    }

    // 🏗️ Constructor completo (con id)
    public SolicitudCompra(Integer id, Date fecha, int idUsuario, int idLibro, int cantidad, double costoUnitario, int estado) {
        this.id = id;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idLibro = idLibro;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.estado = estado;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public SolicitudCompra(Date fecha, int idUsuario, int idLibro, int cantidad, double costoUnitario, int estado) {
        this(null, fecha, idUsuario, idLibro, cantidad, costoUnitario, estado);
    }

    // 🧭 Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        return "SolicitudCompra{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", idUsuario=" + idUsuario +
                ", idLibro=" + idLibro +
                ", cantidad=" + cantidad +
                ", costoUnitario=" + costoUnitario +
                ", estado=" + estado +
                '}';
    }
}
