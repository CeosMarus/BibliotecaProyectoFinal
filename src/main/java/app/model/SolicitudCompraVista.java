package app.model;

public class SolicitudCompraVista {
    private int id;
    private String descripcion;

    public SolicitudCompraVista(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}