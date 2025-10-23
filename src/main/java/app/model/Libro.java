package app.model;

public class Libro {

    private Integer id;         // null al insertar (autogenerado)
    private String titulo;      // corresponde a la columna titulo en BD
    private int anio;
    private int idAutor;
    private int idCategoria;
    private int estado;         // 1 = Activo, 0 = Inactivo
    private String isbn;        // obligatorio

    // Constructor vacío
    public Libro() { }

    // Constructor completo con id
    public Libro(Integer id, String titulo, int anio, int idAutor, int idCategoria, int estado, String isbn) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.idAutor = idAutor;
        this.idCategoria = idCategoria;
        this.estado = estado;
        this.isbn = isbn;
    }

    // Constructor para insertar (sin id)
    public Libro(String titulo, int anio, int idAutor, int idCategoria, int estado, String isbn) {
        this(null, titulo, anio, idAutor, idCategoria, estado, isbn);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getIdAutor() { return idAutor; }
    public void setIdAutor(int idAutor) { this.idAutor = idAutor; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEstadoDescripcion() { return (estado == 1) ? "Activo" : "Inactivo"; }

    @Override
    public String toString() {
        return titulo + " (" + anio + ") - " + getEstadoDescripcion();
    }
}
