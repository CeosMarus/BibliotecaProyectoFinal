package app.model;

import java.time.LocalDateTime;


public class BitacoraInventario {

    // ===== ATRIBUTOS =====

    private Integer id;


    private Integer idInventario;


    private Integer idEjemplar;


    private String diferencia;


    private String accionCorrectiva;


    private LocalDateTime fechaRegistro;

    // Campos adicionales para mostrar en la UI (JOINs)
    private String codigoInventario;  // Del ejemplar
    private String tituloLibro;       // Del libro
    private String responsableInventario; // Del inventario físico

    // ===== CONSTRUCTORES =====


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