package app.model;

import java.util.Date;

public class RostroUsuario {
    private Integer id;           // ID del rostro, null al insertar
    private int idUsuario;        // FK a Usuario.id
    private byte[] plantilla;     // Plantilla facial en binario
    private Date fechaRegistro;   // Fecha del registro del rostro
    private int estado;           // 1 = Activo, 0 = Inactivo

    // Constructor vacío
    public RostroUsuario() {}

    // Constructor completo
    public RostroUsuario(Integer id, int idUsuario, byte[] plantilla, Date fechaRegistro, int estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.plantilla = plantilla;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    // Constructor para insertar
    public RostroUsuario(int idUsuario, byte[] plantilla, Date fechaRegistro, int estado) {
        this(null, idUsuario, plantilla, fechaRegistro, estado);
    }

    // Getters
    public Integer getId() { return id; }
    public int getIdUsuario() { return idUsuario; }
    public byte[] getPlantilla() { return plantilla; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public int getEstado() { return estado; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setPlantilla(byte[] plantilla) { this.plantilla = plantilla; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setEstado(int estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "RostroUsuario{id=" + id +
                ", idUsuario=" + idUsuario +
                ", fechaRegistro=" + fechaRegistro +
                ", estado=" + estado +
                '}';
    }
}
