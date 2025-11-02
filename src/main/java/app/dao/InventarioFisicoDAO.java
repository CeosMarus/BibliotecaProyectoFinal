package app.dao;

import app.db.Conexion;
import app.model.InventarioFisico;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de Inventarios Físicos
 *
 * RESPONSABILIDADES:
 * - Crear registros de inventarios/auditorías
 * - Listar inventarios con información del usuario
 * - Buscar inventarios por fecha o responsable
 * - Anular inventarios si es necesario
 */
public class InventarioFisicoDAO {

    /**
     * INSERTAR un nuevo inventario físico
     *
     * CUÁNDO SE USA:
     * - Al iniciar un nuevo conteo físico
     * - Al crear una auditoría programada
     *
     * @param inventario Objeto con los datos del inventario
     * @return ID generado del inventario, -1 si falla
     */
    public int insertar(InventarioFisico inventario) {
        // Validación básica
        if (inventario == null || inventario.getResponsable().isEmpty()) {
            throw new IllegalArgumentException("El responsable es obligatorio");
        }

        String sql = "INSERT INTO InventarioFisico (fecha, responsable, idUsuario, observaciones, estado) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Convertir LocalDate a java.sql.Date
            ps.setDate(1, Date.valueOf(inventario.getFecha()));
            ps.setString(2, inventario.getResponsable());
            ps.setInt(3, inventario.getIdUsuario());
            ps.setString(4, inventario.getObservaciones());
            ps.setInt(5, inventario.getEstado());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                // Obtener el ID generado
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        inventario.setId(id);
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar inventario: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * ACTUALIZAR un inventario existente
     *
     * CUÁNDO SE USA:
     * - Al modificar observaciones de un inventario
     * - Al cambiar el responsable
     *
     * @param inventario Objeto con los datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(InventarioFisico inventario) {
        if (inventario == null || inventario.getId() == null) {
            throw new IllegalArgumentException("ID del inventario es obligatorio");
        }

        String sql = "UPDATE InventarioFisico " +
                "SET fecha=?, responsable=?, idUsuario=?, observaciones=?, estado=? " +
                "WHERE id=?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(inventario.getFecha()));
            ps.setString(2, inventario.getResponsable());
            ps.setInt(3, inventario.getIdUsuario());
            ps.setString(4, inventario.getObservaciones());
            ps.setInt(5, inventario.getEstado());
            ps.setInt(6, inventario.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar inventario: " + e.getMessage());
            return false;
        }
    }

    /**
     * ANULAR un inventario (cambiar estado a 0)
     *
     * CUÁNDO SE USA:
     * - Cuando un inventario fue registrado por error
     * - Cuando se cancela un conteo en progreso
     *
     * @param id ID del inventario a anular
     * @return true si se anuló correctamente
     */
    public boolean anular(int id) {
        String sql = "UPDATE InventarioFisico SET estado = 0 WHERE id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al anular inventario: " + e.getMessage());
            return false;
        }
    }

    /**
     * BUSCAR inventario por ID
     *
     * @param id ID del inventario
     * @return Objeto InventarioFisico o null si no existe
     */
    public InventarioFisico buscarPorId(int id) {
        String sql = "SELECT i.*, u.nombre AS nombreUsuario " +
                "FROM InventarioFisico i " +
                "INNER JOIN Usuario u ON i.idUsuario = u.id " +
                "WHERE i.id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearInventario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar inventario: " + e.getMessage());
        }
        return null;
    }

    /**
     * LISTAR todos los inventarios con JOIN a Usuario
     *
     * INCLUYE:
     * - Datos del inventario
     * - Nombre del usuario que lo creó
     *
     * @return Lista de inventarios ordenados por fecha descendente
     */
    public List<InventarioFisico> listar() {
        String sql = "SELECT i.*, u.nombre AS nombreUsuario " +
                "FROM InventarioFisico i " +
                "INNER JOIN Usuario u ON i.idUsuario = u.id " +
                "ORDER BY i.fecha DESC";

        List<InventarioFisico> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearInventario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar inventarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * LISTAR inventarios activos únicamente
     *
     * @return Lista de inventarios con estado = 1
     */
    public List<InventarioFisico> listarActivos() {
        String sql = "SELECT i.*, u.nombre AS nombreUsuario " +
                "FROM InventarioFisico i " +
                "INNER JOIN Usuario u ON i.idUsuario = u.id " +
                "WHERE i.estado = 1 " +
                "ORDER BY i.fecha DESC";

        List<InventarioFisico> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearInventario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar inventarios activos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * BUSCAR inventarios por rango de fechas
     *
     * ÚTIL PARA:
     * - Reportes mensuales
     * - Auditorías de un período específico
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista de inventarios en ese rango
     */
    public List<InventarioFisico> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT i.*, u.nombre AS nombreUsuario " +
                "FROM InventarioFisico i " +
                "INNER JOIN Usuario u ON i.idUsuario = u.id " +
                "WHERE i.fecha BETWEEN ? AND ? " +
                "ORDER BY i.fecha DESC";

        List<InventarioFisico> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearInventario(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por rango de fechas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * BUSCAR inventarios por responsable (búsqueda parcial)
     *
     * @param nombre Nombre o parte del nombre del responsable
     * @return Lista de inventarios donde el responsable coincide
     */
    public List<InventarioFisico> buscarPorResponsable(String nombre) {
        String sql = "SELECT i.*, u.nombre AS nombreUsuario " +
                "FROM InventarioFisico i " +
                "INNER JOIN Usuario u ON i.idUsuario = u.id " +
                "WHERE i.responsable LIKE ? " +
                "ORDER BY i.fecha DESC";

        List<InventarioFisico> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearInventario(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por responsable: " + e.getMessage());
        }
        return lista;
    }

    /**
     * CONTAR total de inventarios realizados
     *
     * ÚTIL PARA:
     * - Estadísticas
     * - Reportes gerenciales
     *
     * @return Número total de inventarios
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM InventarioFisico WHERE estado = 1";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al contar inventarios: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper: Mapea ResultSet a objeto InventarioFisico
     *
     * CONVIERTE:
     * Fila de BD → Objeto Java
     *
     * @param rs ResultSet posicionado en una fila
     * @return Objeto InventarioFisico con todos los datos
     * @throws SQLException si hay error al leer datos
     */
    private InventarioFisico mapearInventario(ResultSet rs) throws SQLException {
        InventarioFisico inv = new InventarioFisico();
        inv.setId(rs.getInt("id"));

        // Convertir java.sql.Date a LocalDate
        Date fecha = rs.getDate("fecha");
        if (fecha != null) {
            inv.setFecha(fecha.toLocalDate());
        }

        inv.setResponsable(rs.getString("responsable"));
        inv.setIdUsuario(rs.getInt("idUsuario"));
        inv.setObservaciones(rs.getString("observaciones"));
        inv.setEstado(rs.getInt("estado"));

        // Campo del JOIN
        inv.setNombreUsuario(rs.getString("nombreUsuario"));

        return inv;
    }
}