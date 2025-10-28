package app.dao;

import app.model.Autor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import app.db.Conexion;

public class AutorDAO extends BaseDAO{

    // INSERTAR
    public boolean agregarAutor(Autor autor) {
        if (autor == null || autor.getNombre() == null || autor.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del autor no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO Autor (nombre, biografia, estado) VALUES (?, ?, 1)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getBiografia());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor agregado exitosamente.");
                //Registar la accion en Auditoria
                auditar("Catalogo-Autor", "InsertarAutor", "Se ingreso un nuevo autor" + autor.getNombre());
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar el autor: " + e.getMessage());
        }
        return false;
    }

    // ACTUALIZAR
    public boolean actualizarAutor(Autor autor) {
        if (autor == null || autor.getNombre() == null || autor.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del autor no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Autor SET nombre = ?, biografia = ? WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getBiografia());
            ps.setInt(3, autor.getId());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor actualizado exitosamente.");
                //Registar la accion en Auditoria
                auditar("Catalogo-Autor", "ActualizarAutor", "Se modifico el autor" + autor.getId());
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el autor: " + e.getMessage());
        }
        return false;
    }

    // 🔥 ELIMINACIÓN LÓGICA (Desactivar Autor)
    public boolean eliminarAutor(int id) {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Estás seguro de desactivar este autor?", "Confirmar desactivación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        String sql = "UPDATE Autor SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor desactivado exitosamente.");
                //Registar la accion en Auditoria
                auditar("Catalogo-Autor", "DesactivarAutor", "Se desactivo el autor con ID " + id);
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar el autor: " + e.getMessage());
        }
        return false;
    }

    // 📝 REACTIVAR AUTOR
    public boolean activarAutor(int id) {
        String sql = "UPDATE Autor SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor activado nuevamente.");
                //Registar la accion en Auditoria
                auditar("Catalogo-Autor", "ReactivarAutor", "Se activo nuevamente el autor con ID " + id);
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al activar el autor: " + e.getMessage());
        }
        return false;
    }

    // LISTAR SOLO ACTIVOS
    public List<Autor> listarAutoresActivos() {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM Autor WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor autor = new Autor();
                autor.setId(rs.getInt("id"));
                autor.setNombre(rs.getString("nombre"));
                autor.setBiografia(rs.getString("biografia"));
                autor.setEstado(rs.getInt("estado"));
                lista.add(autor);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar autores activos: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Catalogo-Autor", "ListarAutores", "Se listo todos los autores activos");
        return lista;
    }

    // LISTAR TODOS (para administración)
    public List<Autor> listarTodosLosAutores() {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM Autor ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor autor = new Autor();
                autor.setId(rs.getInt("id"));
                autor.setNombre(rs.getString("nombre"));
                autor.setBiografia(rs.getString("biografia"));
                autor.setEstado(rs.getInt("estado"));
                lista.add(autor);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar todos los autores: " + e.getMessage());
        }
        //Registar la accion en Auditoria
        auditar("Catalogo-Autor", "ListarAutores", "Se listo todos los autores activos e inactivos");
        return lista;
    }
}
