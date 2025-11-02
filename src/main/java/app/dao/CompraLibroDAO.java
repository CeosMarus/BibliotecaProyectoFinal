package app.dao;

import app.db.Conexion;
import app.model.CompraLibro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraLibroDAO extends BaseDAO {

    private Connection conn;

    public CompraLibroDAO() {
        try {
            conn = Conexion.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // 🔹 INSERTAR COMPRA (solo si solicitud está aprobada)
    // ============================================================
    public boolean insertar(CompraLibro compra) {
        try {
            conn.setAutoCommit(false);

            // 1. Validar que la solicitud esté aprobada (estado = 2)
            String validarSql = "SELECT estado FROM SolicitudCompra WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(validarSql)) {
                ps.setInt(1, compra.getIdSolicitud());
                ResultSet rs = ps.executeQuery();

                if (!rs.next() || rs.getInt("estado") != 2) {
                    throw new SQLException("❌ La solicitud no está aprobada para compra.");
                }
            }

            // 2. Insertar compra
            String sqlCompra = "INSERT INTO CompraLibro (idSolicitud, proveedor, costoTotal, fechaRecepcion, estado) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sqlCompra)) {
                ps.setInt(1, compra.getIdSolicitud());
                ps.setString(2, compra.getProveedor());
                ps.setDouble(3, compra.getCostoTotal());
                ps.setDate(4, compra.getFechaRecepcion() != null ? new java.sql.Date(compra.getFechaRecepcion().getTime()) : null);
                ps.setInt(5, compra.getEstado());

                ps.executeUpdate();
            }

            // 3. Actualizar solicitud a "4 finalizada (comprada)"
            String sqlUpdateSolicitud = "UPDATE SolicitudCompra SET estado = 4 WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSolicitud)) {
                ps.setInt(1, compra.getIdSolicitud());
                ps.executeUpdate();
            }

            conn.commit();

            auditar("CompraLibro", "RegistrarCompra", "Compra registrada para Solicitud ID: " + compra.getIdSolicitud());
            return true;

        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            ex.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ============================================================
    // 🔹 ACTUALIZAR COMPRA
    // ============================================================
    public boolean actualizar(CompraLibro compra) {
        String sql = "UPDATE CompraLibro SET proveedor=?, costoTotal=?, fechaRecepcion=?, estado=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, compra.getProveedor());
            ps.setDouble(2, compra.getCostoTotal());
            ps.setDate(3, compra.getFechaRecepcion() != null ? new java.sql.Date(compra.getFechaRecepcion().getTime()) : null);
            ps.setInt(4, compra.getEstado());
            ps.setInt(5, compra.getId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                auditar("CompraLibro", "ActualizarCompra", "Compra ID: " + compra.getId());
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // ============================================================
    // 🔹 LISTAR COMPRAS ACTIVAS
    // ============================================================
    public List<CompraLibro> listarActivos() {
        List<CompraLibro> lista = new ArrayList<>();
        String sql = "SELECT * FROM CompraLibro WHERE estado = 1 ORDER BY id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultado(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        auditar("CompraLibro", "ListarCompras", "Listado de compras activas");
        return lista;
    }

    // ============================================================
    // 🔹 LISTAR SOLICITUDES APROBADAS (para mostrar en combo)
    // ============================================================
    public List<Integer> listarSolicitudesAprobadas() {
        List<Integer> lista = new ArrayList<>();
        String sql = "SELECT id FROM SolicitudCompra WHERE estado = 2 ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getInt("id"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    // 🔹 OBTENER COMPRA POR ID
    // ============================================================
    public CompraLibro obtenerPorId(int id) {
        String sql = "SELECT * FROM CompraLibro WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    auditar("CompraLibro", "BuscarCompra", "Compra ID: " + id);
                    return mapResultado(rs);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    // ============================================================
    // 🔹 ELIMINACIÓN LÓGICA
    // ============================================================
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE CompraLibro SET estado = 0 WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                auditar("CompraLibro", "EliminarCompra", "Compra ID: " + id);
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // 🔹 MAP RESULTSET → OBJETO
    // ============================================================
    private CompraLibro mapResultado(ResultSet rs) throws SQLException {
        return new CompraLibro(
                rs.getInt("id"),
                rs.getInt("idSolicitud"),
                rs.getString("proveedor"),
                rs.getDouble("costoTotal"),
                rs.getDate("fechaRecepcion"),
                rs.getInt("estado")
        );
    }
}
