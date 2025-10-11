package app.model;

import java.math.BigDecimal;
import java.util.Date;

public class AperturaCaja {

    // 🧾 Atributos
    private Integer id;           // null al insertar (autogenerado)
    private int idUsuario;        // FK → Usuario.id
    private Date fecha;
    private Date hora;
    private BigDecimal saldoInicial;
    private int estado;           // 1 = Activa, 0 = Cerrada

    // 🏗️ Constructor vacío
    public AperturaCaja() {
    }

    // 🏗️ Constructor completo (con id)
    public AperturaCaja(Integer id, int idUsuario, Date fecha, Date hora, BigDecimal saldoInicial, int estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
        this.hora = hora;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public AperturaCaja(int idUsuario, Date fecha, Date hora, BigDecimal saldoInicial, int estado) {
        this(null, idUsuario, fecha, hora, saldoInicial, estado);
    }

    // 🧭 Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧾 Método auxiliar para mostrar estado como texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activa" : "Cerrada";
    }

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        return "AperturaCaja{" +
                "id=" + id +
                ", idUsuario=" + idUsuario +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", saldoInicial=" + saldoInicial +
                ", estado=" + getEstadoDescripcion() +
                '}';
    }
}
