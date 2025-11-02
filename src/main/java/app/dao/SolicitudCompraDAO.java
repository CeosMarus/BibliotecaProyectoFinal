package app.dao;

import app.db.Conexion;
import app.model.SolicitudCompra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCompraDAO extends BaseDAO {

    private Connection conn;

    public SolicitudCompraDAO() {
        try {
            conn = Conexion.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // 🔹 INSERTAR NUEVA SOLICITUD (estado = 1 pendiente)
    // ============================================================
    public boolean insertar(SolicitudCompra s) {
        String sql = "INSERT INTO SolicitudCompra (fecha, idUsuario, idLibro, cantidad, costoUnitario, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(s.getFecha().getTime()));
            ps.setInt(2, s.getIdUsuario());
            ps.setInt(3, s.getIdLibro());
            ps.setInt(4, s.getCantidad());
            ps.setBigDecimal(5, new java.math.BigDecimal(s.getCostoUnitario()));
            ps.setInt(6, s.getEstado());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                auditar("SolicitudCompra", "CrearSolicitud",
                        "Nueva solicitud de compra. Libro ID: " + s.getIdLibro());
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // 🔹 LISTAR TODAS LAS SOLICITUDES
    // ============================================================
    public List<SolicitudCompra> listar() {
        List<SolicitudCompra> lista = new ArrayList<>();

        String sql = "SELECT * FROM SolicitudCompra WHERE estado <> 0 ORDER BY id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultado(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        auditar("SolicitudCompra", "ListarSolicitudes", "Se listaron solicitudes activas");
        return lista;
    }

    // ============================================================
    // 🔹 LISTAR SÓLO SOLICITUDES PENDIENTES (estado = 1)
    // ============================================================
    public List<SolicitudCompra> listarPendientes() {
        List<SolicitudCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM SolicitudCompra WHERE estado = 1 ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultado(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        auditar("SolicitudCompra", "ListarPendientes", "Se listaron solicitudes pendientes");
        return lista;
    }
    // ============================================================
// 🔹 LISTAR SÓLO SOLICITUDES APROBADAS (estado = 2)
// ============================================================
    public List<SolicitudCompra> listarAprobadas() {
        List<SolicitudCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM SolicitudCompra WHERE estado = 2 ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultado(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        auditar("SolicitudCompra", "ListarAprobadas", "Se listaron solicitudes aprobadas");
        return lista;
    }

    // ============================================================
// 🔹 ACTUALIZAR ESTADO (alias para cambiarEstado)
// ============================================================
    public boolean actualizarEstado(int id, int nuevoEstado) {
        return cambiarEstado(id, nuevoEstado);
    }

    // ============================================================
    // 🔹 OBTENER POR ID
    // ============================================================
    public SolicitudCompra obtenerPorId(int id) {
        String sql = "SELECT * FROM SolicitudCompra WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    auditar("SolicitudCompra", "BuscarPorID", "Solicitud ID: " + id);
                    return mapResultado(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    // ============================================================
    // 🔹 ACTUALIZAR SOLICITUD
    // ============================================================
    public boolean actualizar(SolicitudCompra s) {
        String sql = "UPDATE SolicitudCompra SET fecha=?, idUsuario=?, idLibro=?, cantidad=?, costoUnitario=?, estado=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(s.getFecha().getTime()));
            ps.setInt(2, s.getIdUsuario());
            ps.setInt(3, s.getIdLibro());
            ps.setInt(4, s.getCantidad());
            ps.setBigDecimal(5, new java.math.BigDecimal(s.getCostoUnitario()));
            ps.setInt(6, s.getEstado());
            ps.setInt(7, s.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                auditar("SolicitudCompra", "ActualizarSolicitud", "Solicitud ID: " + s.getId());
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // 🔹 CAMBIAR ESTADO (1 pendiente, 2 aprobada, 3 rechazada, 0 eliminado)
    // ============================================================
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE SolicitudCompra SET estado=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                auditar("SolicitudCompra", "CambiarEstado",
                        "Solicitud ID: " + id + " -> Estado: " + nuevoEstado);
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // 🔹 ELIMINACIÓN LÓGICA
    // ============================================================
    public boolean eliminar(int id) {
        return cambiarEstado(id, 0);
    }

    // ============================================================
    // 🔹 MAPEAR RESULTADO A OBJETO
    // ============================================================
    private SolicitudCompra mapResultado(ResultSet rs) throws SQLException {
        return new SolicitudCompra(
                rs.getInt("id"),
                rs.getDate("fecha"),
                rs.getInt("idUsuario"),
                rs.getInt("idLibro"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("costoUnitario").doubleValue(),
                rs.getInt("estado")
        );
    }
}
