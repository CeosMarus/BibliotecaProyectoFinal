package app.model;

public class Ejemplar {

    private int id;
    private String codigo;
    private int idLibro;
    private int estado; // 1 = Activo, 0 = Desactivado
    private String libroNombre; // opcional (para joins)

    // 🏗️ Constructores
    public Ejemplar() {
    }

    public Ejemplar(int id, String codigo, int idLibro, int estado) {
        this.id = id;
        this.codigo = codigo;
        this.idLibro = idLibro;
        this.estado = estado;
    }

    // 🧭 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
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

    // 🔹 Estado textual
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }

    // 🔹 toString
    @Override
    public String toString() {
        return codigo + " - " + (libroNombre != null ? libroNombre : "Sin título") + " (" + getEstadoDescripcion() + ")";
    }
}
