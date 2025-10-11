package app.dao;

import app.model.Autor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import app.db.Conexion;

public class AutorDAO {

    // INSERTAR
    public boolean agregarAutor(Autor autor) {
        if (autor == null || autor.getNombre() == null || autor.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del autor no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO Autores (nombre, nacionalidad, estado) VALUES (?, ?, 1)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor agregado exitosamente.");
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

        String sql = "UPDATE Autores SET nombre = ?, nacionalidad = ? WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            ps.setInt(3, autor.getId());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor actualizado exitosamente.");
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

        String sql = "UPDATE Autores SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor desactivado exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar el autor: " + e.getMessage());
        }
        return false;
    }

    // 📝 REACTIVAR AUTOR (opcional — útil para administración)
    public boolean activarAutor(int id) {
        String sql = "UPDATE Autores SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Autor activado nuevamente.");
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
        String sql = "SELECT * FROM Autores WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor autor = new Autor();
                autor.setId(rs.getInt("id"));
                autor.setNombre(rs.getString("nombre"));
                autor.setNacionalidad(rs.getString("nacionalidad"));
                autor.setEstado(rs.getInt("estado"));
                lista.add(autor);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar autores activos: " + e.getMessage());
        }

        return lista;
    }

    // LISTAR TODOS (opcional — para administración)
    public List<Autor> listarTodosLosAutores() {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM Autores ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor autor = new Autor();
                autor.setId(rs.getInt("id"));
                autor.setNombre(rs.getString("nombre"));
                autor.setNacionalidad(rs.getString("nacionalidad"));
                autor.setEstado(rs.getInt("estado"));
                lista.add(autor);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar todos los autores: " + e.getMessage());
        }

        return lista;
    }
}
