package app.model;

public class Autor {

    // 🧾 Atributos
    private int id;
    private String nombre;
    private String biografia;
    private int estado; // 1 = Activo, 0 = Desactivado

    // 🏗️ Constructor vacío
    public Autor() {
    }

    // 🏗️ Constructor con parámetros
    public Autor(int id, String nombre, String biografia, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.biografia = biografia;
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

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // 🧾 Método auxiliar para mostrar estado en texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }
}
