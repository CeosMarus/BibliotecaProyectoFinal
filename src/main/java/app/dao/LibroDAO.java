package app.dao;

import app.db.Conexion;
import app.model.Libro;
import app.model.LibroConAutor;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO extends BaseDAO {

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

            int filas = ps.executeUpdate();
            if (filas > 0)
            {
                //Registo en auditoria
                auditar("Catalogo-Libro", "CrearLibro",
                        "Se creo un nuevo libro ID: " + libro.getId() + ", con titulo: " + libro.getTitulo() );
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar el libro: " + e.getMessage());
            return false;
        }
        return false;
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

            int filas = ps.executeUpdate();
            if (filas > 0)
            {
                //Registo en auditoria
                auditar("Catalogo-Libro", "ActualizarLibro",
                        "Se actualizo libro ID: " + libro.getId() + ", con titulo: " + libro.getTitulo() );
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el libro: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Desactivar libro
    public boolean desactivarLibro(int id) {
        String sql = "UPDATE Libro SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0)
            {
                //Registo en auditoria
                auditar("Catalogo-Libro", "DesactivarLibro",
                        "Se desactivo libro ID: " + id );
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Buscar libro por ID
    public Libro obtenerPorId(int id) {
        String sql = "SELECT id, titulo, isbn, anio, idAutor, idCategoria, estado FROM Libro WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Libro libro = new Libro();
                    libro.setId(rs.getInt("id"));
                    libro.setTitulo(rs.getString("titulo"));
                    libro.setIsbn(rs.getString("isbn"));
                    libro.setAnio(rs.getInt("anio"));
                    libro.setIdAutor(rs.getInt("idAutor"));
                    libro.setIdCategoria(rs.getInt("idCategoria"));
                    libro.setEstado(rs.getInt("estado"));

                    auditar("Catalogo-Libro", "BuscarLibroPorID",
                            "Se buscó libro ID: " + id);

                    return libro;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener libro por ID: " + e.getMessage());
        }
        return null;
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
        //Registo en auditoria
        auditar("Catalogo-Libro", "ListarLibro",
                "Se listaron todos los libros");
        return lista;
    }

    // Listar libros activos
    public List<Libro> listar() {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, isbn, anio, idAutor, idCategoria, estado FROM Libro WHERE estado = 1 ORDER BY titulo ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Libro libro = new Libro();
                libro.setId(rs.getInt("id"));
                libro.setTitulo(rs.getString("titulo"));
                libro.setIsbn(rs.getString("isbn"));
                libro.setAnio(rs.getInt("anio"));
                libro.setIdAutor(rs.getInt("idAutor"));
                libro.setIdCategoria(rs.getInt("idCategoria"));
                libro.setEstado(rs.getInt("estado"));
                lista.add(libro);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar libros activos: " + e.getMessage());
        }
        //Registo en auditoria
        auditar("Catalogo-Libro", "ListarLibro",
                "Se listaron todos los libros activos" );
        return lista;
    }
}
