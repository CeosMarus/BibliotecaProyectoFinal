package app.model;

public class InventarioFisico {
    private Integer id;            // null al insertar
    private int idLibro;           // FK a Libro.id
    private int cantidadTotal;
    private int cantidadDisponible;
    private String ubicacion;
    private int estado;            // 1 = Activo, 0 = Inactivo

    public InventarioFisico() {}

    public InventarioFisico(Integer id, int idLibro, int cantidadTotal, int cantidadDisponible, String ubicacion, int estado) {
        this.id = id;
        this.idLibro = idLibro;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadDisponible;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public InventarioFisico(int idLibro, int cantidadTotal, int cantidadDisponible, String ubicacion, int estado) {
        this(null, idLibro, cantidadTotal, cantidadDisponible, ubicacion, estado);
    }

    public Integer getId() { return id; }
    public int getIdLibro() { return idLibro; }
    public int getCantidadTotal() { return cantidadTotal; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public String getUbicacion() { return ubicacion; }
    public int getEstado() { return estado; }

    public void setId(Integer id) { this.id = id; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public void setCantidadTotal(int cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setEstado(int estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "InventarioFisico{id=" + id + ", idLibro=" + idLibro +
                ", total=" + cantidadTotal + ", disponible=" + cantidadDisponible +
                ", ubicacion='" + ubicacion + "'}";
    }
}
