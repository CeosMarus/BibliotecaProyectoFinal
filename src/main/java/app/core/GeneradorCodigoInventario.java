package app.core;

import app.db.Conexion;

import java.sql.*;
import java.time.LocalDate;

/**
    Utilidad para generar códigos de inventario únicos
    FORMATO: LIB-YYYY-NNNN
    EJEMPLOS:
 */
public class GeneradorCodigoInventario {

    /*
      1. Obtiene el año actual
      2. Busca el último código generado este año
      3. Incrementa la secuencia en 1
      4. Si no hay códigos del año actual, empieza en 0001
     */
    public static String generarCodigo() throws SQLException {
        // Obtener año actual
        int anioActual = LocalDate.now().getYear();

        // Prefijo del código (puedes cambiarlo)
        String prefijo = "LIB-" + anioActual + "-";

        // Consulta para obtener el último código del año actual
        String sql = "SELECT TOP 1 codigoInventario " +
                "FROM Ejemplar " +
                "WHERE codigoInventario LIKE ? " +
                "ORDER BY codigoInventario DESC";

        int siguienteSecuencia = 1; // Por defecto, empieza en 1

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Buscar códigos que empiecen con "LIB-2024-"
            ps.setString(1, prefijo + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Encontró un código previo
                    String ultimoCodigo = rs.getString("codigoInventario");

                    // Extraer la parte numérica
                    String parteNumerica = ultimoCodigo.substring(ultimoCodigo.lastIndexOf("-") + 1);
                    int ultimaSecuencia = Integer.parseInt(parteNumerica);

                    // Incrementar
                    siguienteSecuencia = ultimaSecuencia + 1;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al generar código: " + e.getMessage());
            // En caso de error, usar timestamp como fallback
            siguienteSecuencia = (int)(System.currentTimeMillis() % 10000);
        }

        // Formatear con ceros a la izquierda (4 dígitos)
        String secuenciaFormateada = String.format("%04d", siguienteSecuencia);

        // Retornar código completo
        return prefijo + secuenciaFormateada;
    }

    public static boolean esCodigoValido(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return false;
        }

        // Debe tener formato: LIB-YYYY-NNNN
        return codigo.matches("^LIB-\\d{4}-\\d{4}$");
    }

    public static int extraerAnio(String codigo) {
        if (!esCodigoValido(codigo)) {
            return 0;
        }

        String[] partes = codigo.split("-");
        return Integer.parseInt(partes[1]);
    }


    public static int extraerSecuencia(String codigo) {
        if (!esCodigoValido(codigo)) {
            return 0;
        }

        String[] partes = codigo.split("-");
        return Integer.parseInt(partes[2]);
    }

    /* Main para realizar pruebas */
    public static void main(String[] args) {
        try {
            System.out.println("=== GENERADOR DE CÓDIGOS ===\n");

            // Generar 5 códigos de prueba
            for (int i = 1; i <= 5; i++) {
                String codigo = generarCodigo();
                System.out.println("Código " + i + ": " + codigo);
            }

            // Validar códigos
            System.out.println("\n=== VALIDACIONES ===");
            System.out.println("LIB-2024-0001 válido: " + esCodigoValido("LIB-2024-0001"));
            System.out.println("LIB-24-001 válido: " + esCodigoValido("LIB-24-001"));
            System.out.println("INVALID válido: " + esCodigoValido("INVALID"));

            // Extraer partes
            System.out.println("\n=== EXTRACCIÓN ===");
            String ejemplo = "LIB-2024-0045";
            System.out.println("Código: " + ejemplo);
            System.out.println("Año: " + extraerAnio(ejemplo));
            System.out.println("Secuencia: " + extraerSecuencia(ejemplo));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}