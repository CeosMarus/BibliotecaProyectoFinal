package app.dao;

import app.db.Conexion;
import app.model.BitacoraInventario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class BitacoraInventarioDAO {

    //INSERTAR un registro en la bitácora

    public int insertar(BitacoraInventario bitacora) {
        if (bitacora == null || bitacora.getIdEjemplar() == null) {
            throw new IllegalArgumentException("El ejemplar es obligatorio");
        }

        String sql = "INSERT INTO BitacoraInventario " +
                "(idInventario, idEjemplar, diferencia, accionCorrectiva, fechaRegistro) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // idInventario puede ser NULL (cambios fuera de inventario formal)
            if (bitacora.getIdInventario() != null) {
                ps.setInt(1, bitacora.getIdInventario());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setInt(2, bitacora.getIdEjemplar());
            ps.setString(3, bitacora.getDiferencia());
            ps.setString(4, bitacora.getAccionCorrectiva());
            ps.setTimestamp(5, Timestamp.valueOf(bitacora.getFechaRegistro()));

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        bitacora.setId(id);
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar en bitácora: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // REGISTRAR cambio simple (sin inventario asociado)

    public int registrarCambio(int idEjemplar, String diferencia, String accion) {
        BitacoraInventario bitacora = new BitacoraInventario(
                null, // Sin inventario asociado
                idEjemplar,
                diferencia,
                accion
        );
        return insertar(bitacora);
    }

    // BUSCAR registro por ID

    public BitacoraInventario buscarPorId(int id) {
        String sql = "SELECT b.*, " +
                "       e.codigoInventario, " +
                "       l.titulo AS tituloLibro, " +
                "       inv.responsable AS responsableInventario " +
                "FROM BitacoraInventario b " +
                "INNER JOIN Ejemplar e ON b.idEjemplar = e.id " +
                "INNER JOIN Libro l ON e.idLibro = l.id " +
                "LEFT JOIN InventarioFisico inv ON b.idInventario = inv.id " +
                "WHERE b.id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearBitacora(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar registro: " + e.getMessage());
        }
        return null;
    }

    //LISTAR todos los registros con información completa
    public List<BitacoraInventario> listar() {
        String sql = "SELECT b.*, " +
                "       e.codigoInventario, " +
                "       l.titulo AS tituloLibro, " +
                "       inv.responsable AS responsableInventario " +
                "FROM BitacoraInventario b " +
                "INNER JOIN Ejemplar e ON b.idEjemplar = e.id " +
                "INNER JOIN Libro l ON e.idLibro = l.id " +
                "LEFT JOIN InventarioFisico inv ON b.idInventario = inv.id " +
                "ORDER BY b.fechaRegistro DESC";

        return ejecutarConsulta(sql);
    }

    public List<BitacoraInventario> listarPorInventario(int idInventario) {
        String sql = "SELECT b.*, " +
                "       e.codigoInventario, " +
                "       l.titulo AS tituloLibro, " +
                "       inv.responsable AS responsableInventario " +
                "FROM BitacoraInventario b " +
                "INNER JOIN Ejemplar e ON b.idEjemplar = e.id " +
                "INNER JOIN Libro l ON e.idLibro = l.id " +
                "LEFT JOIN InventarioFisico inv ON b.idInventario = inv.id " +
                "WHERE b.idInventario = ? " +
                "ORDER BY b.fechaRegistro DESC";

        List<BitacoraInventario> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idInventario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBitacora(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar por inventario: " + e.getMessage());
        }
        return lista;
    }

    /**
     * LISTAR historial de un ejemplar específico
     *
     * ÚTIL PARA:
     * - Ver todos los cambios de un libro específico
     * - Trazabilidad completa de un ejemplar
     *
     * @param idEjemplar ID del ejemplar
     * @return Historial completo del ejemplar
     */
    public List<BitacoraInventario> listarPorEjemplar(int idEjemplar) {
        String sql = "SELECT b.*, " +
                "       e.codigoInventario, " +
                "       l.titulo AS tituloLibro, " +
                "       inv.responsable AS responsableInventario " +
                "FROM BitacoraInventario b " +
                "INNER JOIN Ejemplar e ON b.idEjemplar = e.id " +
                "INNER JOIN Libro l ON e.idLibro = l.id " +
                "LEFT JOIN InventarioFisico inv ON b.idInventario = inv.id " +
                "WHERE b.idEjemplar = ? " +
                "ORDER BY b.fechaRegistro DESC";

        List<BitacoraInventario> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEjemplar);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBitacora(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar por ejemplar: " + e.getMessage());
        }
        return lista;
    }

    /**
     * LISTAR cambios en un rango de fechas
     *
     * @param fechaInicio Fecha y hora inicial
     * @param fechaFin Fecha y hora final
     * @return Lista de cambios en ese período
     */
    public List<BitacoraInventario> listarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT b.*, " +
                "       e.codigoInventario, " +
                "       l.titulo AS tituloLibro, " +
                "       inv.responsable AS responsableInventario " +
                "FROM BitacoraInventario b " +
                "INNER JOIN Ejemplar e ON b.idEjemplar = e.id " +
                "INNER JOIN Libro l ON e.idLibro = l.id " +
                "LEFT JOIN InventarioFisico inv ON b.idInventario = inv.id " +
                "WHERE b.fechaRegistro BETWEEN ? AND ? " +
                "ORDER BY b.fechaRegistro DESC";

        List<BitacoraInventario> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBitacora(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar por rango de fechas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * CONTAR diferencias encontradas en un inventario
     *
     * @param idInventario ID del inventario
     * @return Número de diferencias registradas
     */
    public int contarDiferenciasPorInventario(int idInventario) {
        String sql = "SELECT COUNT(*) FROM BitacoraInventario WHERE idInventario = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idInventario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al contar diferencias: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper: Ejecuta consulta genérica
     */
    private List<BitacoraInventario> ejecutarConsulta(String sql) {
        List<BitacoraInventario> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearBitacora(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error en consulta: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Helper: Mapea ResultSet a objeto BitacoraInventario
     */
    private BitacoraInventario mapearBitacora(ResultSet rs) throws SQLException {
        BitacoraInventario bitacora = new BitacoraInventario();
        bitacora.setId(rs.getInt("id"));

        // idInventario puede ser NULL
        int idInv = rs.getInt("idInventario");
        bitacora.setIdInventario(rs.wasNull() ? null : idInv);

        bitacora.setIdEjemplar(rs.getInt("idEjemplar"));
        bitacora.setDiferencia(rs.getString("diferencia"));
        bitacora.setAccionCorrectiva(rs.getString("accionCorrectiva"));

        Timestamp ts = rs.getTimestamp("fechaRegistro");
        if (ts != null) {
            bitacora.setFechaRegistro(ts.toLocalDateTime());
        }

        // Campos del JOIN
        bitacora.setCodigoInventario(rs.getString("codigoInventario"));
        bitacora.setTituloLibro(rs.getString("tituloLibro"));
        bitacora.setResponsableInventario(rs.getString("responsableInventario"));

        return bitacora;
    }
}