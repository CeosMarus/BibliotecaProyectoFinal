package app.model;

public class LibroConAutor {

    // 🧾 Atributos combinados de libro, autor y categoría
    private final int id;
    private final String nombre;
    private final int anio;
    private final String autorNombre;
    private final String categoriaNombre;
    private final int estado;

    // 🏗️ Constructor completo
    public LibroConAutor(int id, String nombre, int anio, String autorNombre, String categoriaNombre, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.anio = anio;
        this.autorNombre = autorNombre;
        this.categoriaNombre = categoriaNombre;
        this.estado = estado;
    }

    // 🧭 Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAnio() {
        return anio;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public int getEstado() {
        return estado;
    }

    // 🧾 Método auxiliar para mostrar estado como texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }

    // 🧩 toString para visualización en tablas o logs
    @Override
    public String toString() {
        return String.format(
                "%s (%d) | Autor: %s | Categoría: %s | Estado: %s",
                nombre, anio, autorNombre, categoriaNombre, getEstadoDescripcion()
        );
    }
}
