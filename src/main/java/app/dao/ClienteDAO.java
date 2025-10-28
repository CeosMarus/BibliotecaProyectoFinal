// src/main/java/app/dao/ClienteDAO.java
package app.dao;

import app.db.Conexion;
import app.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO extends BaseDAO {

    // INSERTAR un nuevo cliente, devuelve ID generado
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
            ps.setInt(5, c.getEstado());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        c.setId(id);
                        //Registar la accion en Auditoria
                        auditar("Clientes", "NuevoCliente", "Se creo el nuevo cliente: " + c.getNombre());
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
        }
        return -1;
    }

    // ACTUALIZAR cliente existente
    public boolean actualizar(Cliente c) {
        if (c == null || c.getId() == null || c.getNombre().isEmpty() || c.getNit().isEmpty() || c.getTelefono().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos del cliente son obligatorios y debe existir ID.");
        }

        String sql = "UPDATE Cliente SET nombre=?, nit=?, telefono=?, correo=?, estado=? WHERE id=?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNit());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setInt(5, c.getEstado());
            ps.setInt(6, c.getId());

            int  filas = ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Clientes", "ActualizarCliente", "Se actualizo el cliente: " + c.getNombre());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
        return false;
    }

    // ELIMINAR físicamente un cliente
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Cliente WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Clientes", "EliminarCliente", "Se elimino definitivamente el cliente con ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
        return false;
    }

    // CAMBIAR ESTADO (activar/inactivar)
    public boolean cambiarEstado(int id, int nuevoEstado) {
        String sql = "UPDATE Cliente SET estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, id);
            int filas =  ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Clientes", "CambioEstadoCliente", "Cliente ID: " + id + "cambio a estado: " + nuevoEstado);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del cliente: " + e.getMessage());
            return false;
        }
        return false;
    }

    // LISTAR todos los clientes
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, nit, telefono, correo, estado FROM Cliente ORDER BY id ASC ";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCliente(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Clientes", "ListarClientes", "Se listaron todos los clientes");
        return lista;
    }

    // BUSCAR cliente por ID
    public Cliente buscarPorId(int id) {
        String sql = "SELECT id, nombre, nit, telefono, correo, estado FROM Cliente WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("Clientes", "BuscarCliente", "Se busco el cliente con ID: " + id);
                    return mapCliente(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
        }
        return null;
    }

    // BUSCAR clientes por nombre (búsqueda parcial)
    public List<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, nit, telefono, correo, estado FROM Cliente WHERE nombre LIKE ? ORDER BY nombre DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapCliente(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por nombre: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Clientes", "BuscarCliente", "Se busco el cliente: " + nombre);
        return lista;
    }

    public boolean existeNit(String nit, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE nit = ? AND id != ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nit);
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("Clientes", "ValidarNit", "Se valido el nit: " + nit);
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar NIT: " + e.getMessage());
            throw e;
        }
        return false;
    }


    // Helper: mapear ResultSet a Cliente
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

    // mapeador 2
    private Cliente mapClientes(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setNit(rs.getString("nit"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setEstado(rs.getInt("estado"));
        return cliente;
    }

}