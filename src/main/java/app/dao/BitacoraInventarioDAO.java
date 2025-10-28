package app.dao;

import app.db.Conexion;
import app.model.BitacoraInventario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraInventarioDAO extends BaseDAO {

    private final Conexion conexion;

    public BitacoraInventarioDAO() {
        conexion = new Conexion();
    }

    // ✅ Insertar nuevo movimiento
    public boolean insertar(BitacoraInventario bitacora) {
        String sql = "INSERT INTO BitacoraInventario (idInventario, tipoMovimiento, cantidad, fechaMovimiento, observacion, usuarioResponsable, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bitacora.getIdInventario());
            stmt.setString(2, bitacora.getTipoMovimiento());
            stmt.setInt(3, bitacora.getCantidad());
            stmt.setTimestamp(4, Timestamp.valueOf(bitacora.getFechaMovimiento()));
            stmt.setString(5, bitacora.getObservacion());
            stmt.setString(6, bitacora.getUsuarioResponsable());
            stmt.setInt(7, bitacora.getEstado());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "NuevaBitacoraInventario", "Se creo una nueva bitacora de inventario con id: " + bitacora.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar BitacoraInventario: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Listar todos los movimientos
    public List<BitacoraInventario> listarTodos() {
        List<BitacoraInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM BitacoraInventario ORDER BY fechaMovimiento DESC";

        try (Connection conn = conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BitacoraInventario b = new BitacoraInventario(
                        rs.getInt("id"),
                        rs.getInt("idInventario"),
                        rs.getString("tipoMovimiento"),
                        rs.getInt("cantidad"),
                        rs.getTimestamp("fechaMovimiento").toLocalDateTime(),
                        rs.getString("observacion"),
                        rs.getString("usuarioResponsable"),
                        rs.getInt("estado")
                );
                lista.add(b);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar BitacoraInventario: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Inventarios", "ListarBitacoraInventario", "Se listo todas las bitacoras de inventario");
        return lista;
    }

    // ✅ Buscar por ID
    public BitacoraInventario buscarPorId(int id) {
        String sql = "SELECT * FROM BitacoraInventario WHERE id = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("Inventarios", "ListarBitacoraInventario", "Se listo todas la bitacora con ID: " + id);
                    return new BitacoraInventario(
                            rs.getInt("id"),
                            rs.getInt("idInventario"),
                            rs.getString("tipoMovimiento"),
                            rs.getInt("cantidad"),
                            rs.getTimestamp("fechaMovimiento").toLocalDateTime(),
                            rs.getString("observacion"),
                            rs.getString("usuarioResponsable"),
                            rs.getInt("estado")
                    );

                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar BitacoraInventario: " + e.getMessage());
        }
        return null;
    }

    // ✅ Actualizar movimiento
    public boolean actualizar(BitacoraInventario bitacora) {
        String sql = "UPDATE BitacoraInventario SET idInventario=?, tipoMovimiento=?, cantidad=?, fechaMovimiento=?, observacion=?, usuarioResponsable=?, estado=? WHERE id=?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bitacora.getIdInventario());
            stmt.setString(2, bitacora.getTipoMovimiento());
            stmt.setInt(3, bitacora.getCantidad());
            stmt.setTimestamp(4, Timestamp.valueOf(bitacora.getFechaMovimiento()));
            stmt.setString(5, bitacora.getObservacion());
            stmt.setString(6, bitacora.getUsuarioResponsable());
            stmt.setInt(7, bitacora.getEstado());
            stmt.setInt(8, bitacora.getId());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "ActualizarBitacoraInventario", "Se actualizo la bitacora con id: " + bitacora.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar BitacoraInventario: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Eliminar (registro físico)
    public boolean eliminar(int id) {
        String sql = "DELETE FROM BitacoraInventario WHERE id = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "EliminarBitacoraInventario", "Se elimino la bitarcora ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar BitacoraInventario: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Cambiar estado (activar/inactivar)
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE BitacoraInventario SET estado = ? WHERE id = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoEstado);
            stmt.setInt(2, id);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "CabioEstadoBitacora", "Se cambio el estado de bitacora: " + id + "a estado: " + nuevoEstado);
                return true;

            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado en BitacoraInventario: " + e.getMessage());
            return false;
        }
        return false;
    }
}
