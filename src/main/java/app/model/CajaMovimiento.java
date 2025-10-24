package app.model;

import java.math.BigDecimal;
import java.util.Date;

public class CajaMovimiento {

    // 🧾 Atributos
    private Integer id;          // null al insertar (autogenerado)
    private Date fecha;           // Fecha del movimiento
    private Date hora;            // Hora del movimiento
    private int tipo;             // 1 = Ingreso, 2 = Egreso
    private String subtipo;       // Multa, Reembolso, Pago
    private BigDecimal monto;     // Monto del movimiento
    private int idUsuario;        // FK → Usuario.id
    private String descripcion;   // Descripción del movimiento

    // 🏗️ Constructor vacío
    public CajaMovimiento() {
    }

    // 🏗️ Constructor completo (con id)
    public CajaMovimiento(Integer id, Date fecha, Date hora, int tipo, String subtipo, BigDecimal monto, int idUsuario, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.subtipo = subtipo;
        this.monto = monto;
        this.idUsuario = idUsuario;
        this.descripcion = descripcion;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public CajaMovimiento(Date fecha, Date hora, int tipo, String subtipo, BigDecimal monto, int idUsuario, String descripcion) {
        this(null, fecha, hora, tipo, subtipo, monto, idUsuario, descripcion);
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

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getSubtipo() {
        return subtipo;
    }

    public void setSubtipo(String subtipo) {
        this.subtipo = subtipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        String tipoStr = (tipo == 1) ? "Ingreso" : "Egreso";
        return "CajaMovimiento{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", tipo=" + tipoStr +
                ", subtipo='" + subtipo + '\'' +
                ", monto=" + monto +
                ", idUsuario=" + idUsuario +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}