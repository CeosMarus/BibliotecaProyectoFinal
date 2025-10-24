package app.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Modelo auxiliar para cargar datos de la Multa junto con campos descriptivos
 * de tablas relacionadas (Cliente, Prestamo) para la vista (JTable).
 */
public class MultaDetalle extends Multa {

    // 🆕 Campos descriptivos para la vista
    private String nombreCliente;
    // Si necesitas el título del libro o descripción del préstamo:
    // private String descripcionPrestamo;

    // Constructor que acepta todos los campos de Multa
    public MultaDetalle(Integer id, int idPrestamo, int idCliente, BigDecimal monto, int diasAtraso,
                        int estadoPago, Date fechaPago, String observaciones, int estado,
                        String nombreCliente) {

        super(id, idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado);
        this.nombreCliente = nombreCliente;
        // this.descripcionPrestamo = descripcionPrestamo; // Si lo incluyes
    }

    // Constructor vacío para facilitar el mapeo si no usas el constructor completo
    public MultaDetalle() {
    }

    // 🧭 Getter y Setter para el nuevo campo
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    // Si incluyes la descripción del préstamo:
    /*
    public String getDescripcionPrestamo() {
        return descripcionPrestamo;
    }

    public void setDescripcionPrestamo(String descripcionPrestamo) {
        this.descripcionPrestamo = descripcionPrestamo;
    }
    */

    // Sobrescribir toString para incluir el nombre del cliente
    @Override
    public String toString() {
        return "MultaDetalle{" +
                super.toString() +
                ", nombreCliente='" + nombreCliente + '\'' +
                '}';
    }
}