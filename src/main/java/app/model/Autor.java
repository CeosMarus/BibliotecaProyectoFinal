package app.model;

public class Autor {
    private Integer id;
    private String nombre;
    private String biografia;
    private int estado; // 1 = Activo, 0 = Inactivo

    // Constructor vacío
    public Autor() {}

    // Constructor completo
    public Autor(Integer id, String nombre, String biografia, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.biografia = biografia;
        this.estado = estado;
    }

    // Getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    // 🔹 Mostrar nombre en JComboBox
    @Override
    public String toString() {
        return nombre;
    }
}
