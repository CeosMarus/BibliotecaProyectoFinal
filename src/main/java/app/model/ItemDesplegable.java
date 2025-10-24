package app.model;

/**
 * Clase auxiliar para almacenar el ID (Integer) y la descripción (String)
 * en un JComboBox. Al sobrescribir toString(), permite que el JComboBox
 * muestre la descripción mientras almacena el ID internamente.
 */
public class ItemDesplegable {
    private final int id;
    private final String descripcion;

    /**
     * Constructor.
     * @param id El ID numérico (ej: idCliente, idPrestamo).
     * @param descripcion La cadena de texto visible en el JComboBox.
     */
    public ItemDesplegable(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el ID numérico asociado al ítem seleccionado.
     * @return El ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Método crucial: define qué texto se mostrará en el JComboBox.
     * @return La descripción amigable.
     */
    @Override
    public String toString() {
        return descripcion;
    }
}