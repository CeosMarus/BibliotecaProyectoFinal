package app.model;

import java.util.Date;

public class Auditoria {

    // 🧾 Atributos
    private Integer id;          // null al insertar (autogenerado)
    private Date fechaHora;
    private int idUsuario;       // FK → Usuario.id
    private String modulo;
    private String accion;
    private String detalle;

    // 🏗️ Constructor vacío
    public Auditoria() {
    }

    // 🏗️ Constructor completo (con id)
    public Auditoria(Integer id, Date fechaHora, int idUsuario, String modulo, String accion, String detalle) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.idUsuario = idUsuario;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public Auditoria(Date fechaHora, int idUsuario, String modulo, String accion, String detalle) {
        this(null, fechaHora, idUsuario, modulo, accion, detalle);
    }

    // 🧭 Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        return "Auditoria{" +
                "id=" + id +
                ", fechaHora=" + fechaHora +
                ", idUsuario=" + idUsuario +
                ", modulo='" + modulo + '\'' +
                ", accion='" + accion + '\'' +
                ", detalle='" + detalle + '\'' +
                '}';
    }
}

