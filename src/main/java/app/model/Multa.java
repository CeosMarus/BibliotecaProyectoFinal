package app.model;

import java.math.BigDecimal;
import java.util.Date;

public class Multa {

    // 🧾 Atributos
    private Integer id;           // null al insertar (autogenerado)
    private int idPrestamo;       // FK → Prestamo.id
    private int idCliente;        // FK → Cliente.id
    private BigDecimal monto;
    private int diasAtraso;
    private int estadoPago;       // 0 = Pendiente, 1 = Pagado
    private Date fechaPago;
    private String observaciones;
    private int estado;           // 1 = Activo, 0 = Desactivado

    // 🏗️ Constructor vacío
    public Multa() {
    }

    // 🏗️ Constructor completo (con id)
    public Multa(Integer id, int idPrestamo, int idCliente, BigDecimal monto, int diasAtraso,
                 int estadoPago, Date fechaPago, String observaciones, int estado) {
        this.id = id;
        this.idPrestamo = idPrestamo;
        this.idCliente = idCliente;
        this.monto = monto;
        this.diasAtraso = diasAtraso;
        this.estadoPago = estadoPago;
        this.fechaPago = fechaPago;
        this.observaciones = observaciones;
        this.estado = estado;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public Multa(int idPrestamo, int idCliente, BigDecimal monto, int diasAtraso,
                 int estadoPago, Date fechaPago, String observaciones, int estado) {
        this(null, idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado);
    }

    // 🧭 Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public int getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(int estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧾 Método auxiliar para mostrar estado como texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }

    // 🧾 Método auxiliar para mostrar estado de pago como texto
    public String getEstadoPagoDescripcion() {
        return (estadoPago == 1) ? "Pagado" : "Pendiente";
    }

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        return "Multa{" +
                "id=" + id +
                ", idPrestamo=" + idPrestamo +
                ", idCliente=" + idCliente +
                ", monto=" + monto +
                ", diasAtraso=" + diasAtraso +
                ", estadoPago=" + getEstadoPagoDescripcion() +
                ", fechaPago=" + fechaPago +
                ", observaciones='" + observaciones + '\'' +
                ", estado=" + getEstadoDescripcion() +
                '}';
    }
}
