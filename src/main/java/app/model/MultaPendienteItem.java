package app.model;

import java.math.BigDecimal;

public class MultaPendienteItem {
    private final int idMulta;
    private final String nombreCliente;
    private final int idPrestamo;
    private final BigDecimal monto;
    private final int diasAtraso;

    public MultaPendienteItem(int idMulta, String nombreCliente, int idPrestamo, BigDecimal monto, int diasAtraso) {
        this.idMulta = idMulta;
        this.nombreCliente = nombreCliente;
        this.idPrestamo = idPrestamo;
        this.monto = monto;
        this.diasAtraso = diasAtraso;
    }

    // Getters necesarios
    public int getIdMulta() { return idMulta; }
    public String getNombreCliente() { return nombreCliente; }
    public int getIdPrestamo() { return idPrestamo; }
    public BigDecimal getMonto() { return monto; }
    public int getDiasAtraso() { return diasAtraso; }
}