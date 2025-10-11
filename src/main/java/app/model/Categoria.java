package app.model;

public class Categoria {

    // 🧾 Atributos
    private int id;
    private String nombre;
    private int estado; // 1 = Activo, 0 = Desactivado

    // 🏗️ Constructor vacío
    public Categoria() {
    }

    // 🏗️ Constructor con parámetros
    public Categoria(int id, String nombre, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
    }

    // 🧭 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧾 Método auxiliar para mostrar el estado en texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }

    // 🧩 Método opcional para depuración (toString)
    @Override
    public String toString() {
        return nombre + " (" + getEstadoDescripcion() + ")";
    }
}
