// src/main/java/app/model/Cliente.java
package app.model;

public class Cliente {

    private Integer id;       // null al insertar
    private String nombre;
    private String nit;
    private String telefono;
    private String correo;    // Nuevo campo agregado
    private int estado;       // 1 = Activo, 0 = Desactivado

    // 🏗 Constructor vacío
    public Cliente() {}

    // 🏗 Constructor con todos los campos
    public Cliente(Integer id, String nombre, String nit, String telefono, String correo, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.nit = nit;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    // 🏗 Constructor sin ID (para inserciones)
    public Cliente(String nombre, String nit, String telefono, String correo, int estado) {
        this(null, nombre, nit, telefono, correo, estado);
    }

    // 🧭 Getters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public int getEstado() { return estado; }

    // 🧭 Setters
    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setNit(String nit) { this.nit = nit; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setEstado(int estado) { this.estado = estado; }

    // 📝 Método auxiliar para mostrar estado en texto
    public String getEstadoDescripcion() {
        return (estado == 1) ? "Activo" : "Desactivado";
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nombre='" + nombre + "', nit='" + nit + "', telefono='" + telefono + "', correo='" + correo + "', estado=" + getEstadoDescripcion() + "}";
    }
}
