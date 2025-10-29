package app.dao;

import app.core.PasswordUtil;
import app.db.Conexion;
import app.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO extends BaseDAO {

    /** Crear usuario: guarda el HASH en la columna 'password' */
    public int crearUsuario(String username, String plainPassword, String nombre, String rol, int estado) throws SQLException {
        String sql = "INSERT INTO usuario (username, password, nombre, rol, estado) VALUES (?, ?, ?, ?, ?)";
        String hash = PasswordUtil.hash(plainPassword);

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, nombre);
            ps.setString(4, rol);
            ps.setInt(5, estado);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if(rs.next())
                {
                    int idGenerado = rs.getInt(1);

                    //Registo en auditoria
                    auditar("Usuarios", "CrearUsuario",
                            "Se creo un nuevo usuario con username: " + username +
                                    ", rol: " + rol +
                                    ", ID generado: " + idGenerado);

                    return idGenerado;
                }

            }
            catch (SQLException e) {
                System.err.println("Error al insertar el usuario: " + e.getMessage());
            }
            return -1; // Si no se generó ID
        }
    }

    /** Actualizar usuario (sin cambiar contraseña) */
    public boolean actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET username = ?, nombre = ?, rol = ?, estado = ? WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getRol());
            ps.setInt(4, u.getEstado());
            ps.setInt(5, u.getId());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("Usuarios", "ActualizarUsuario",
                        "Se actualizo el usuario ID: " + u.getId() + ", username: " + u.getUsername());
                return true;
            }

            return false;
        }
    }

    /** Eliminación lógica */
    public boolean eliminar(int id) throws SQLException {
        String sql = "UPDATE usuario SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("Usuarios", "DesactivarUsuario",
                        "Se desactivo el usuario ID: " + id);
                return true;
            }

            return false;
        }
    }

    /** Listar usuarios activos */
    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT id, username, nombre, password, rol, estado FROM usuario WHERE estado=1 ORDER BY id DESC";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapUsuario(rs));
            }
        }
        //Registo en auditoria
        auditar("Usuarios", "ListarUsuario",
                "Se listo los usuarios activos");
        return lista;
    }
    /** Listar únicamente usuarios con rol = 'Cliente' y estado = 1 */
    public List<Usuario> listarClientes() throws SQLException {
        String sql = "SELECT id, username, nombre, password, rol, estado FROM usuario WHERE estado = 1 AND rol = 'Cliente' ORDER BY nombre ASC";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("nombre"),
                        rs.getString("rol"),
                        rs.getInt("estado")
                ));
            }
        }
        //Registo en auditoria
        auditar("Usuarios", "ListarUsuario",
                "Se listo los usuarios activos con rol Cliente");
        return lista;
    }
    /** Buscar usuarios por username */
    public List<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, nombre, password, rol, estado FROM usuario WHERE username LIKE ? AND estado=1";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + username + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapUsuario(rs));
                }
            }
        }
        //Registo en auditoria
        auditar("Usuarios", "ListarUsuario",
                "Se listo los usuarios activos con username: " + username);
        return lista;
    }

    /** Validar login */
    public Usuario validarLogin(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, password, nombre, rol, estado FROM usuario WHERE username=? AND estado=1";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String hash = rs.getString("password");
                if (!PasswordUtil.verify(plainPassword, hash)) return null;
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("nombre"),
                        rs.getString("rol"),
                        rs.getInt("estado")
                );
            }
        }
    }

    /** Mapear ResultSet a Usuario */
    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("nombre"),
                rs.getString("rol"),
                rs.getInt("estado")
        );
    }
}
