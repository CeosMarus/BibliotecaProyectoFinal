package app.dao;

import app.db.Conexion;
import app.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    /* Insertar un nuevo cliente */
    public int insertar(Cliente c) {
        if (c == null || c.getNombre().isEmpty() || c.getNit().isEmpty() || c.getTelefono().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos del cliente son obligatorios.");
        }

        String sql = "INSERT INTO Cliente (nombre, nit, telefono, correo, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNit());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setInt(5, 1); //Estado Activo

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
        }
        return -1;
    }

    /* Actualizar cliente existente */
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE Cliente SET nombre=?, nit=?, telefono=?, correo=?, estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNit());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setInt(5, c.getEstado());
            ps.setInt(6, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    /** Eliminacion logica (estado = 0) */
    public boolean eliminar(int id) {
        String sql = "UPDATE Cliente SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    /* ACTIVAR cliente (estado = 1) */
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE Cliente SET estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del cliente: " + e.getMessage());
            return false;
        }
    }

    /* Lisatar todos los clientes (activos y desactivados) */
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, nit, telefono, correo, estado FROM Cliente ORDER BY id ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapCliente(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    /* Buscar cliente por nombre (solo activos) */
    public List<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente WHERE nombre LIKE ? AND estado = 1 ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        return lista;
    }

    /* Validar duplicado de NIT */
    public boolean existeNit(String nit, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE nit = ? AND id != ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nit);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("nit"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getInt("estado")
        );
    }
}