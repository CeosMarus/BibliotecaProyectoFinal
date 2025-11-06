package app.model;

import java.time.LocalDate;

public class InventarioFisico {

    // ===== ATRIBUTOS =====

    private Integer id;
    private LocalDate fecha;
    private String responsable;
    private Integer idUsuario;

    /**
     * Observaciones generales del inventario
     * Ejemplo: "Se encontraron 3 libros dañados en Sala A"
     */
    private String observaciones;


    private int estado;

    // Campos adicionales para mostrar en la UI (JOINs)
    private String nombreUsuario; // Nombre del usuario que lo creó

    // ===== CONSTRUCTORES =====

    public InventarioFisico() {
        this.estado = 1; // Por defecto activo
    }

    /**
     * Constructor completo (con ID)
     * Usado al recuperar datos de la BD
     */
    public InventarioFisico(Integer id, LocalDate fecha, String responsable,
                            Integer idUsuario, String observaciones, int estado) {
        this.id = id;
        this.fecha = fecha;
        this.responsable = responsable;
        this.idUsuario = idUsuario;
        this.observaciones = observaciones;
        this.estado = estado;
    }

    /**
     * Constructor sin ID (para INSERT)
     * Usado al crear un nuevo inventario
     */
    public InventarioFisico(LocalDate fecha, String responsable,
                            Integer idUsuario, String observaciones) {
        this(null, fecha, responsable, idUsuario, observaciones, 1);
    }

    // ===== GETTERS Y SETTERS =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Retorna el estado en texto legible
     * @return "Activo" o "Anulado"
     */
    public String getEstadoDescripcion() {
        return estado == 1 ? "Activo" : "Anulado";
    }

    /**
     * Verifica si el inventario está activo
     * @return true si está activo
     */
    public boolean isActivo() {
        return estado == 1;
    }

    /**
     * Para debugging y logs
     */
    @Override
    public String toString() {
        return String.format("InventarioFisico{id=%d, fecha=%s, responsable='%s', estado=%s}",
                id, fecha, responsable, getEstadoDescripcion());
    }
}