package app.dao;

import app.model.CompraLibro;
import app.db.Conexion; // ✅ Usar la clase real de conexión
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraLibroDAO extends BaseDAO {

    private Connection conn;

    public CompraLibroDAO() {
        try {
            conn = Conexion.getConnection();
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener la conexión: " + e.getMessage());
        }
    }

    // Insertar nuevo registro
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
            if (rows > 0) {
                //Registar la accion en Auditoria
                auditar("ComprasLibro", "NuevaCompra", "Se registro la compra de la solicitud: " + compra.getIdSolicitud());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al insertar CompraLibro: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Actualizar registro
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
            if (rows > 0)
            {
                //Registar la accion en Auditoria
                auditar("ComprasLibro", "ActualizarRegistro", "Se actualizo la compra con ID: " + compra.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar CompraLibro: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Listar registros activos
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
        //Registar la accion en Auditoria
        auditar("ComprasLibro", "ListarCompras", "Se listaron todas las compras activas");
        return lista;
    }

    // Obtener un registro por ID
    public CompraLibro obtenerPorId(int id) {
        String sql = "SELECT * FROM CompraLibro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("ComprasLibro", "ListarCompras", "Se listo la compra con ID: " + id);
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
    // CAMBIAR ESTADO (activar/inactivar)
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE CompraLibro SET estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, id);
            int filas =  ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("ComprasLibro", "CambioEstadoCompra", "La compra ID: " + id + " cambio a estado: " + nuevoEstado);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del cliente: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Eliminación lógica
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE CompraLibro SET estado = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                //Registar la accion en Auditoria
                auditar("ComprasLibro", "EliminarCompra", "Se elimino compra con ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar CompraLibro: " + e.getMessage());
            return false;
        }
        return false;
    }
}
