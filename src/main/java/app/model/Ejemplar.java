package app.model;

import java.sql.Date;

public class Ejemplar {

    // 🧾 Atributos
    private int id;
    private int idLibro;           // FK → Libro.id
    private String codigoInventario;
    private String sala;
    private String estante;
    private String nivel;
    private String estadoCopia;
    private Date fechaAlta;
    private Date fechaBaja;
    private int estado;            // 1 = Activo, 0 = Inactivo

    // 🔹 Atributo adicional para mostrar el nombre/titulo del libro en tablas
    private String libroNombre;

    // 🏗️ Constructor vacío
    public Ejemplar() {
    }

    // 🏗️ Constructor completo
    public Ejemplar(int id, int idLibro, String codigoInventario, String sala,
                    String estante, String nivel, String estadoCopia,
                    Date fechaAlta, Date fechaBaja, int estado) {
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getLibroNombre() {
        return libroNombre;
    }

    public void setLibroNombre(String libroNombre) {
        this.libroNombre = libroNombre;
    }

    // 🔹 Método auxiliar para mostrar estado como texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Inactivo";
    }

    // 🔹 toString útil para depuración o logs
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", codigoInventario, libroNombre, getEstadoDescripcion());
    }
}
