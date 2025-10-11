package app.dao;

import app.db.Conexion;
import app.model.Libro;
import app.model.LibroConAutor;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    // ➕ INSERTAR LIBRO
    public boolean agregarLibro(Libro libro) {
        if (libro == null || libro.getNombre() == null || libro.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del libro no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (libro.getAnio() <= 0) {
            JOptionPane.showMessageDialog(null, "El año del libro debe ser válido.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO Libro (nombre, anio, idAutor, idCategoria, estado) VALUES (?, ?, ?, ?, 1)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libro.getNombre());
            ps.setInt(2, libro.getAnio());
            ps.setInt(3, libro.getIdAutor());
            ps.setInt(4, libro.getIdCategoria());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Libro agregado exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar el libro: " + e.getMessage());
        }
        return false;
    }

    // ✏️ ACTUALIZAR LIBRO
    public boolean actualizarLibro(Libro libro) {
        if (libro == null || libro.getNombre() == null || libro.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del libro no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Libro SET nombre=?, anio=?, idAutor=?, idCategoria=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libro.getNombre());
            ps.setInt(2, libro.getAnio());
            ps.setInt(3, libro.getIdAutor());
            ps.setInt(4, libro.getIdCategoria());
            ps.setInt(5, libro.getId());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Libro actualizado correctamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el libro: " + e.getMessage());
        }
        return false;
    }

    // ❌ ELIMINACIÓN LÓGICA (Desactivar)
    public boolean desactivarLibro(int id) {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Deseas desactivar este libro?", "Confirmar desactivación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        String sql = "UPDATE Libro SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Libro desactivado exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar el libro: " + e.getMessage());
        }
        return false;
    }

    // 🔁 REACTIVAR LIBRO
    public boolean activarLibro(int id) {
        String sql = "UPDATE Libro SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Libro reactivado correctamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al reactivar el libro: " + e.getMessage());
        }
        return false;
    }

    // 📋 LISTAR SOLO LIBROS ACTIVOS
    public List<LibroConAutor> listarLibrosActivos() {
        List<LibroConAutor> lista = new ArrayList<>();
        String sql = """
                SELECT l.id, l.nombre, l.anio,
                       a.nombre AS autorNombre,
                       c.nombre AS categoriaNombre,
                       l.estado
                FROM Libro l
                JOIN Autor a ON a.id = l.idAutor
                JOIN Categoria c ON c.id = l.idCategoria
                WHERE l.estado = 1
                ORDER BY l.id DESC
                """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LibroConAutor libro = new LibroConAutor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("anio"),
                        rs.getString("autorNombre"),
                        rs.getString("categoriaNombre"),
                        rs.getInt("estado")
                );
                lista.add(libro);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar libros activos: " + e.getMessage());
        }

        return lista;
    }

    // 📚 LISTAR TODOS (administración)
    public List<LibroConAutor> listarTodos() {
        List<LibroConAutor> lista = new ArrayList<>();
        String sql = """
                SELECT l.id, l.nombre, l.anio,
                       a.nombre AS autorNombre,
                       c.nombre AS categoriaNombre,
                       l.estado
                FROM Libro l
                JOIN Autor a ON a.id = l.idAutor
                JOIN Categoria c ON c.id = l.idCategoria
                ORDER BY l.id DESC
                """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LibroConAutor libro = new LibroConAutor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("anio"),
                        rs.getString("autorNombre"),
                        rs.getString("categoriaNombre"),
                        rs.getInt("estado")
                );
                lista.add(libro);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar los libros: " + e.getMessage());
        }

        return lista;
    }

    // 🔍 BUSCAR LIBRO POR ID
    public Libro buscarPorId(int id) {
        String sql = "SELECT * FROM Libro WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Libro(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getInt("anio"),
                            rs.getInt("idAutor"),
                            rs.getInt("idCategoria"),
                            rs.getInt("estado")
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar el libro: " + e.getMessage());
        }
        return null;
    }
}
