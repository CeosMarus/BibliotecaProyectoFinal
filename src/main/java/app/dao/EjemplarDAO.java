package app.dao;

import app.db.Conexion;
import app.model.Ejemplar;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EjemplarDAO {

    // 🔹 INSERTAR: crea un nuevo ejemplar
    public int insertar(Ejemplar e) throws SQLException {
        String sql = "INSERT INTO Ejemplar (codigo, idLibro, estado) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getCodigo());
            ps.setInt(2, e.getIdLibro());
            ps.setInt(3, e.getEstado());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔹 ACTUALIZAR: modifica datos del ejemplar
    public boolean actualizar(Ejemplar e) throws SQLException {
        String sql = "UPDATE Ejemplar SET codigo=?, idLibro=?, estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getCodigo());
            ps.setInt(2, e.getIdLibro());
            ps.setInt(3, e.getEstado());
            ps.setInt(4, e.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 ELIMINACIÓN LÓGICA: cambia estado de 1 a 0
    public boolean eliminar(int id) throws SQLException {
        String sql = "UPDATE Ejemplar SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 REACTIVAR: cambia estado de 0 a 1
    public boolean reactivar(int id) throws SQLException {
        String sql = "UPDATE Ejemplar SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 LISTAR todos los ejemplares (últimos primero)
    public List<Ejemplar> listar() throws SQLException {
        String sql = "SELECT id, codigo, idLibro, estado FROM Ejemplar ORDER BY id DESC";
        List<Ejemplar> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapEjemplar(rs));
            }
        }
        return lista;
    }

    // 🔹 BUSCAR POR ID
    public Ejemplar buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, codigo, idLibro, estado FROM Ejemplar WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEjemplar(rs);
                }
            }
        }
        return null;
    }

    // 🔹 BUSCAR POR CÓDIGO (búsqueda parcial)
    public List<Ejemplar> buscarPorCodigo(String codigo) throws SQLException {
        List<Ejemplar> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, idLibro, estado FROM Ejemplar WHERE codigo LIKE ? ORDER BY id DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + codigo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapEjemplar(rs));
                }
            }
        }
        return lista;
    }

    // 🔹 LISTAR CON JOIN a LIBRO (para mostrar nombre del libro)
    public List<Ejemplar> listarConLibro() throws SQLException {
        String sql = """
                SELECT e.id, e.codigo, e.idLibro, l.nombre AS libroNombre, e.estado
                FROM Ejemplar e
                JOIN Libro l ON e.idLibro = l.id
                ORDER BY e.id DESC
                """;

        List<Ejemplar> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ejemplar ej = new Ejemplar(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getInt("idLibro"),
                        rs.getInt("estado")
                );
                ej.setLibroNombre(rs.getString("libroNombre"));
                lista.add(ej);
            }
        }
        return lista;
    }

    // 🔹 Helper: mapear ResultSet → objeto Ejemplar
    private Ejemplar mapEjemplar(ResultSet rs) throws SQLException {
        return new Ejemplar(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getInt("idLibro"),
                rs.getInt("estado")
        );
    }
}
