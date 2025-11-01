package app.model;

import java.time.LocalDateTime;

/**
 * Modelo para BitacoraInventario
 *
 * PROPÓSITO:
 * - Registra CADA diferencia encontrada durante un inventario
 * - Mantiene historial completo de cambios en ejemplares
 * - Permite auditoría y trazabilidad
 *
 * CUÁNDO SE USA:
 * 1. Durante un inventario físico (diferencias encontradas)
 * 2. Al cambiar ubicación de un ejemplar
 * 3. Al cambiar estado de un ejemplar (Disponible → Dañado)
 * 4. Al dar de baja un ejemplar
 *
 * EJEMPLO:
 * - "Ejemplar LIB-2024-0045 encontrado dañado, actualizado de 'Disponible' a 'Dañado'"
 * - "Ejemplar LIB-2024-0046 no encontrado en estante, marcado como 'Perdido'"
 * - "Ejemplar LIB-2024-0047 movido de Sala A a Sala B"
 */
public class BitacoraInventario {

    // ===== ATRIBUTOS =====

    /**
     * ID único del registro (generado por BD)
     */
    private Integer id;

    /**
     * ID del inventario al que pertenece este registro
     * Referencia a InventarioFisico.id
     * - Puede ser NULL si es un cambio fuera de inventario formal
     */
    private Integer idInventario;

    /**
     * ID del ejemplar afectado
     * Referencia a Ejemplar.id
     */
    private Integer idEjemplar;

    /**
     * Descripción de la diferencia encontrada
     * Ejemplos:
     * - "Sistema: Disponible | Real: Dañado"
     * - "No encontrado en ubicación registrada"
     * - "Código de inventario duplicado"
     * - "Ubicación incorrecta: estaba en Sala B, no en Sala A"
     */
    private String diferencia;

    /**
     * Acción correctiva tomada
     * Ejemplos:
     * - "Estado actualizado a 'Dañado'"
     * - "Marcado como 'Perdido'"
     * - "Ubicación actualizada a Sala B, Estante 5"
     * - "Ejemplar dado de baja"
     */
    private String accionCorrectiva;

    /**
     * Fecha y hora exacta del registro
     * Para trazabilidad completa
     */
    private LocalDateTime fechaRegistro;

    // Campos adicionales para mostrar en la UI (JOINs)
    private String codigoInventario;  // Del ejemplar
    private String tituloLibro;       // Del libro
    private String responsableInventario; // Del inventario físico

    // ===== CONSTRUCTORES =====

    /**
     * Constructor vacío
     */
    public BitacoraInventario() {
        this.fechaRegistro = LocalDateTime.now(); // Fecha actual por defecto
    }

    /**
     * Constructor completo (con ID)
     * Usado al recuperar de BD
     */
    public BitacoraInventario(Integer id, Integer idInventario, Integer idEjemplar,
                              String diferencia, String accionCorrectiva,
                              LocalDateTime fechaRegistro) {
        this.id = id;
        this.idInventario = idInventario;
        this.idEjemplar = idEjemplar;
        this.diferencia = diferencia;
        this.accionCorrectiva = accionCorrectiva;
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Constructor sin ID (para INSERT)
     * Usado al crear nuevo registro
     */
    public BitacoraInventario(Integer idInventario, Integer idEjemplar,
                              String diferencia, String accionCorrectiva) {
        this(null, idInventario, idEjemplar, diferencia, accionCorrectiva, LocalDateTime.now());
    }

    /**
     * Constructor para cambios fuera de inventario formal
     * (idInventario = NULL)
     */
    public BitacoraInventario(Integer idEjemplar, String diferencia, String accionCorrectiva) {
        this(null, idEjemplar, diferencia, accionCorrectiva);
    }

    // ===== GETTERS Y SETTERS =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }

    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public String getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(String diferencia) {
        this.diferencia = diferencia;
    }

    public String getAccionCorrectiva() {
        return accionCorrectiva;
    }

    public void setAccionCorrectiva(String accionCorrectiva) {
        this.accionCorrectiva = accionCorrectiva;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getResponsableInventario() {
        return responsableInventario;
    }

    public void setResponsableInventario(String responsableInventario) {
        this.responsableInventario = responsableInventario;
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Verifica si este registro pertenece a un inventario formal
     * @return true si pertenece a un inventario
     */
    public boolean tieneInventarioAsociado() {
        return idInventario != null && idInventario > 0;
    }

    /**
     * Tipo de registro según si tiene inventario asociado
     * @return "Inventario Formal" o "Cambio Manual"
     */
    public String getTipoRegistro() {
        return tieneInventarioAsociado() ? "Inventario Formal" : "Cambio Manual";
    }

    /**
     * Para debugging y logs
     */
    @Override
    public String toString() {
        return String.format("BitacoraInventario{id=%d, ejemplar=%d, diferencia='%s', fecha=%s}",
                id, idEjemplar, diferencia, fechaRegistro);
    }
}