package app.model;

public class LibroConAutor {

    private final int id;
    private final String titulo;         // columna "titulo"
    private final int anio;
    private final String autorNombre;
    private final String categoriaNombre;
    private final int estado;
    private final String isbn;

    // Constructor completo
    public LibroConAutor(int id, String titulo, int anio, String autorNombre, String categoriaNombre, int estado, String isbn) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.autorNombre = autorNombre;
        this.categoriaNombre = categoriaNombre;
        this.estado = estado;
        this.isbn = isbn;
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public int getAnio() { return anio; }
    public String getAutorNombre() { return autorNombre; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public int getEstado() { return estado; }
    public String getIsbn() { return isbn; }

    public String getEstadoDescripcion() { return (estado == 1) ? "Activo" : "Inactivo"; }

    @Override
    public String toString() {
        return String.format("%s (%d) | Autor: %s | Categoría: %s | Estado: %s | ISBN: %s",
                titulo, anio, autorNombre, categoriaNombre, getEstadoDescripcion(), isbn);
    }
}
