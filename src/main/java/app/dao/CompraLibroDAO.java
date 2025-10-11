package app.dao;

import app.model.CompraLibro;
import app.db.Conexion; // ✅ Usar la clase real de conexión
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraLibroDAO {

    private Connection conn;

    public CompraLibroDAO() {
        try {
            conn = Conexion.getConnection(); // ✅ Cambiar DBConnection por Conexion
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener la conexión: " + e.getMessage());
        }
    }

    // 🟢 Insertar nuevo registro
    public boolean insertar(CompraLibro compra) {
        String sql = "INSERT INTO CompraLibro (idSolicitud, proveedor, costoTotal, fechaRecepcion, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, compra.getIdSolicitud());
            ps.setString(2, compra.getProveedor());
            ps.setDouble(3, compra.getCostoTotal());
            if (compra.getFechaRecepcion() != null) {
                ps.setDate(4, new java.sql.Date(compra.getFechaRecepcion().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, compra.getEstado());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error al insertar CompraLibro: " + e.getMessage());
            return false;
        }
    }

    // 🟡 Actualizar registro
    public boolean actualizar(CompraLibro compra) {
        String sql = "UPDATE CompraLibro SET idSolicitud = ?, proveedor = ?, costoTotal = ?, fechaRecepcion = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, compra.getIdSolicitud());
            ps.setString(2, compra.getProveedor());
            ps.setDouble(3, compra.getCostoTotal());
            if (compra.getFechaRecepcion() != null) {
                ps.setDate(4, new java.sql.Date(compra.getFechaRecepcion().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, compra.getEstado());
            ps.setInt(6, compra.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar CompraLibro: " + e.getMessage());
            return false;
        }
    }

    // 🟢 Listar registros activos
    public List<CompraLibro> listarActivos() {
        List<CompraLibro> lista = new ArrayList<>();
        String sql = "SELECT * FROM CompraLibro WHERE estado = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CompraLibro compra = new CompraLibro(
                        rs.getInt("id"),
                        rs.getInt("idSolicitud"),
                        rs.getString("proveedor"),
                        rs.getDouble("costoTotal"),
                        rs.getDate("fechaRecepcion"),
                        rs.getInt("estado")
                );
                lista.add(compra);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al listar CompraLibro: " + e.getMessage());
        }
        return lista;
    }

    // 🟢 Obtener un registro por ID
    public CompraLibro obtenerPorId(int id) {
        String sql = "SELECT * FROM CompraLibro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener CompraLibro: " + e.getMessage());
        }
        return null;
    }

    // 🟢 Eliminación lógica
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE CompraLibro SET estado = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar CompraLibro: " + e.getMessage());
            return false;
        }
    }
}
