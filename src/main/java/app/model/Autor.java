package app.model;

public class Autor {

    // 🧾 Atributos
    private int id;
    private String nombre;
    private String nacionalidad;
    private int estado; // 1 = Activo, 0 = Desactivado

    // 🏗️ Constructor vacío
    public Autor() {
    }

    // 🏗️ Constructor con parámetros
    public Autor(int id, String nombre, String nacionalidad, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
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

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧾 Método auxiliar opcional para mostrar estado en texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }
}
