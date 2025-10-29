package app.dao;

import app.db.Conexion;
import app.model.InventarioFisico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioFisicoDAO extends BaseDAO {
    private final Conexion conexion;

    public InventarioFisicoDAO() {
        conexion = new Conexion();
    }

    // ✅ Insertar nuevo registro
    public boolean insertar(InventarioFisico inventario) {
        String sql = "INSERT INTO InventarioFisico (idLibro, cantidadTotal, cantidadDisponible, ubicacion, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, inventario.getIdLibro());
            stmt.setInt(2, inventario.getCantidadTotal());
            stmt.setInt(3, inventario.getCantidadDisponible());
            stmt.setString(4, inventario.getUbicacion());
            stmt.setInt(5, inventario.getEstado());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "NuevoInventarioFisico", "Se creo un nuevo inventario fisico para el libro ID: " + inventario.getIdLibro());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar InventarioFisico: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Listar todos
    public List<InventarioFisico> listarTodos() {
        List<InventarioFisico> lista = new ArrayList<>();
        String sql = "SELECT * FROM InventarioFisico ORDER BY id DESC";

        try (Connection conn = conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InventarioFisico inv = new InventarioFisico(
                        rs.getInt("id"),
                        rs.getInt("idLibro"),
                        rs.getInt("cantidadTotal"),
                        rs.getInt("cantidadDisponible"),
                        rs.getString("ubicacion"),
                        rs.getInt("estado")
                );
                lista.add(inv);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar InventarioFisico: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Inventarios", "ListarInventarioFisico", "Se listaron todos los inventarios fisicos");
        return lista;
    }

    // ✅ Buscar por ID
    public InventarioFisico buscarPorId(int id) {
        String sql = "SELECT * FROM InventarioFisico WHERE id = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("Inventarios", "BuscarInventarioFisico", "Se busco el inventario con ID: " + id);
                    return new InventarioFisico(
                            rs.getInt("id"),
                            rs.getInt("idLibro"),
                            rs.getInt("cantidadTotal"),
                            rs.getInt("cantidadDisponible"),
                            rs.getString("ubicacion"),
                            rs.getInt("estado")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar InventarioFisico: " + e.getMessage());
        }
        return null;
    }

    // ✅ Actualizar registro
    public boolean actualizar(InventarioFisico inventario) {
        String sql = "UPDATE InventarioFisico SET idLibro=?, cantidadTotal=?, cantidadDisponible=?, ubicacion=?, estado=? WHERE id=?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, inventario.getIdLibro());
            stmt.setInt(2, inventario.getCantidadTotal());
            stmt.setInt(3, inventario.getCantidadDisponible());
            stmt.setString(4, inventario.getUbicacion());
            stmt.setInt(5, inventario.getEstado());
            stmt.setInt(6, inventario.getId());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "ActalizarInventarioFisico", "Se actualizo el inventario fisico para el libro ID: " + inventario.getIdLibro());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar InventarioFisico: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Eliminar (físico o lógico)
    public boolean eliminar(int id) {
        String sql = "DELETE FROM InventarioFisico WHERE id = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int  filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "EliminarInventarioFisico", "Se elimino el inventario fisico ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar InventarioFisico: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ✅ Cambiar estado (activar/inactivar)
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE InventarioFisico SET estado = ? WHERE id = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoEstado);
            stmt.setInt(2, id);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Inventarios", "CambioEstadoInventarioFisico", "El inventario ID: " + id + "cambio a estado: " + nuevoEstado);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado en InventarioFisico: " + e.getMessage());
            return false;
        }
        return false;
    }
}
