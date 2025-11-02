package app.model;

import java.time.LocalDate;

/**
 * Modelo para InventarioFisico
 *
 * PROPÓSITO:
 * - Representa un CONTEO FÍSICO de la biblioteca
 * - Se crea cada vez que haces una auditoría/inventario
 * - Permite rastrear QUIÉN y CUÁNDO se hizo el conteo
 *
 * EJEMPLO DE USO:
 * - "Inventario mensual de Enero 2024"
 * - "Auditoría anual - Sala de Referencias"
 * - "Conteo extraordinario por faltantes"
 */
public class InventarioFisico {

    // ===== ATRIBUTOS =====

    /**
     * ID único del inventario (generado automáticamente por BD)
     * - NULL al crear un nuevo inventario
     * - Se asigna automáticamente al insertar
     */
    private Integer id;

    /**
     * Fecha en que se realizó el inventario
     * Ejemplo: 2024-01-15
     */
    private LocalDate fecha;

    /**
     * Nombre de la persona responsable del conteo
     * Puede ser diferente al usuario del sistema
     * Ejemplo: "Juan Pérez - Bibliotecario"
     */
    private String responsable;

    /**
     * ID del usuario del sistema que registró el inventario
     * Referencia a Usuario.id
     */
    private Integer idUsuario;

    /**
     * Observaciones generales del inventario
     * Ejemplo: "Se encontraron 3 libros dañados en Sala A"
     */
    private String observaciones;

    /**
     * Estado del inventario
     * - 1 = Activo/Vigente
     * - 0 = Anulado/Cancelado
     */
    private int estado;

    // Campos adicionales para mostrar en la UI (JOINs)
    private String nombreUsuario; // Nombre del usuario que lo creó

    // ===== CONSTRUCTORES =====

    /**
     * Constructor vacío
     * Usado por frameworks y para crear objetos nuevos
     */
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