package app.model;

public class Libro {

    // 🧾 Atributos
    private Integer id;        // null al insertar (autogenerado)
    private String nombre;
    private int anio;
    private int idAutor;       // FK → Autor.id
    private int idCategoria;   // FK → Categoria.id
    private int estado;        // 1 = Activo, 0 = Desactivado

    // 🏗️ Constructor vacío
    public Libro() {
    }

    // 🏗️ Constructor completo (con id)
    public Libro(Integer id, String nombre, int anio, int idAutor, int idCategoria, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.anio = anio;
        this.idAutor = idAutor;
        this.idCategoria = idCategoria;
        this.estado = estado;
    }

    // 🏗️ Constructor sin id (para inserciones nuevas)
    public Libro(String nombre, int anio, int idAutor, int idCategoria, int estado) {
        this(null, nombre, anio, idAutor, idCategoria, estado);
    }

    // 🧭 Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
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

    // 🧩 Representación útil (para depuración o vistas)
    @Override
    public String toString() {
        return nombre + " (" + anio + ") - " + getEstadoDescripcion();
    }
}
