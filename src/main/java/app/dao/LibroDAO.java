package app.dao;

import app.db.Conexion;
import app.model.Libro;
import app.model.LibroConAutor;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    // Insertar libro
    public boolean agregarLibro(Libro libro) {
        if (libro == null || libro.getTitulo().isEmpty() || libro.getIsbn().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Título e ISBN son obligatorios.", "Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO Libro (titulo, isbn, anio, idAutor, idCategoria, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getIsbn());
            ps.setInt(3, libro.getAnio());
            ps.setInt(4, libro.getIdAutor());
            ps.setInt(5, libro.getIdCategoria());
            ps.setInt(6, libro.getEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar el libro: " + e.getMessage());
            return false;
        }
    }

    // Actualizar libro
    public boolean actualizarLibro(Libro libro) {
        if (libro == null || libro.getTitulo().isEmpty() || libro.getIsbn().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Título e ISBN son obligatorios.", "Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Libro SET titulo=?, isbn=?, anio=?, idAutor=?, idCategoria=?, estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getIsbn());
            ps.setInt(3, libro.getAnio());
            ps.setInt(4, libro.getIdAutor());
            ps.setInt(5, libro.getIdCategoria());
            ps.setInt(6, libro.getEstado());
            ps.setInt(7, libro.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el libro: " + e.getMessage());
            return false;
        }
    }

    // Desactivar libro
    public boolean desactivarLibro(int id) {
        String sql = "UPDATE Libro SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar: " + e.getMessage());
            return false;
        }
    }

    // Listar todos los libros (con Autor y Categoria)
    public List<LibroConAutor> listarTodos() {
        List<LibroConAutor> lista = new ArrayList<>();
        String sql = """
                SELECT l.id, l.titulo, l.anio, a.nombre AS autorNombre, c.nombre AS categoriaNombre, l.estado, l.isbn
                FROM Libro l
                JOIN Autor a ON a.id = l.idAutor
                JOIN Categoria c ON c.id = l.idCategoria
                ORDER BY l.id DESC
                """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new LibroConAutor(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getInt("anio"),
                        rs.getString("autorNombre"),
                        rs.getString("categoriaNombre"),
                        rs.getInt("estado"),
                        rs.getString("isbn")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar libros: " + e.getMessage());
        }
        return lista;
    }
}
