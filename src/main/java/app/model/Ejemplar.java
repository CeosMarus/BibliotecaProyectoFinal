package app.model;

import java.sql.Date;

public class Ejemplar {

    private int id;
    private int idLibro;
    private String codigoInventario;
    private String sala;
    private String estante;
    private String nivel;
    private String estadoCopia; // Ejemplo: "Disponible", "Prestado", "Dañado", etc.
    private Date fechaAlta;
    private Date fechaBaja;
    private String estado; // "Activo" o "Desactivado"

    // Campo auxiliar (para mostrar el nombre del libro al listar)
    private String libroNombre;

    // 🏗️ Constructores
    public Ejemplar() {
    }

    // Constructor completo
    public Ejemplar(int id, int idLibro, String codigoInventario, String sala, String estante, String nivel,
                    String estadoCopia, Date fechaAlta, Date fechaBaja, String estado) {
        this.id = id;
        this.idLibro = idLibro;
        this.codigoInventario = codigoInventario;
        this.sala = sala;
        this.estante = estante;
        this.nivel = nivel;
        this.estadoCopia = estadoCopia;
        this.fechaAlta = fechaAlta;
        this.fechaBaja = fechaBaja;
        this.estado = estado;
    }

    // Constructor parcial (para inserts sin fechaBaja)
    public Ejemplar(int idLibro, String codigoInventario, String sala, String estante, String nivel,
                    String estadoCopia, Date fechaAlta, String estado) {
        this.idLibro = idLibro;
        this.codigoInventario = codigoInventario;
        this.sala = sala;
        this.estante = estante;
        this.nivel = nivel;
        this.estadoCopia = estadoCopia;
        this.fechaAlta = fechaAlta;
        this.estado = estado;
    }

    // 🧭 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getEstante() {
        return estante;
    }

    public void setEstante(String estante) {
        this.estante = estante;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getEstadoCopia() {
        return estadoCopia;
    }

    public void setEstadoCopia(String estadoCopia) {
        this.estadoCopia = estadoCopia;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getLibroNombre() {
        return libroNombre;
    }

    public void setLibroNombre(String libroNombre) {
        this.libroNombre = libroNombre;
    }

    // 🔹 Estado textual
    public String getEstadoDescripcion() {
        return estado != null ? estado : "Desconocido";
    }

    // 🔹 toString (para ComboBox o debug)
    @Override
    public String toString() {
        return codigoInventario + " - " +
                (libroNombre != null ? libroNombre : "Sin título") +
                " (" + getEstadoDescripcion() + ")";
    }
}
