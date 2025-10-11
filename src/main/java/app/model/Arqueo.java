package app.model;

import java.util.Date;

public class Arqueo {

    // 🧾 Atributos
    private Integer id;               // null al insertar (autogenerado)
    private Date fecha;
    private Date hora;
    private int idApertura;           // FK → AperturaCaja.id
    private float esperadoEfectivo;
    private float contadoEfectivo;
    private float diferencia;
    private int idUsuario;            // FK → Usuario.id
    private int estado;               // 1 = activo, 0 = desactivado

    // 🏗️ Constructor vacío
    public Arqueo() {
    }

    // 🏗️ Constructor completo (con id)
    public Arqueo(Integer id, Date fecha, Date hora, int idApertura, float esperadoEfectivo, float contadoEfectivo, float diferencia, int idUsuario, int estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.idApertura = idApertura;
        this.esperadoEfectivo = esperadoEfectivo;
        this.contadoEfectivo = contadoEfectivo;
        this.diferencia = diferencia;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public Arqueo(Date fecha, Date hora, int idApertura, float esperadoEfectivo, float contadoEfectivo, float diferencia, int idUsuario) {
        this(null, fecha, hora, idApertura, esperadoEfectivo, contadoEfectivo, diferencia, idUsuario, 1);
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

    public int getIdApertura() {
        return idApertura;
    }

    public void setIdApertura(int idApertura) {
        this.idApertura = idApertura;
    }

    public float getEsperadoEfectivo() {
        return esperadoEfectivo;
    }

    public void setEsperadoEfectivo(float esperadoEfectivo) {
        this.esperadoEfectivo = esperadoEfectivo;
    }

    public float getContadoEfectivo() {
        return contadoEfectivo;
    }

    public void setContadoEfectivo(float contadoEfectivo) {
        this.contadoEfectivo = contadoEfectivo;
    }

    public float getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(float diferencia) {
        this.diferencia = diferencia;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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
        return "Arqueo{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", idApertura=" + idApertura +
                ", esperadoEfectivo=" + esperadoEfectivo +
                ", contadoEfectivo=" + contadoEfectivo +
                ", diferencia=" + diferencia +
                ", idUsuario=" + idUsuario +
                ", estado=" + estado +
                '}';
    }
}
