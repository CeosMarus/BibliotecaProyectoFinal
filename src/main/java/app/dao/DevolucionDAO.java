package app.dao;

import app.db.Conexion;
import app.model.Devolucion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAO {

    // INSERTAR devolución
    public int insertar(Devolucion d) throws SQLException {
        String sql = "INSERT INTO Devolucion (idPrestamo, fechaDevolucion, estadoCopia, observaciones, idUsuario, estado) "
                + "VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getIdPrestamo());
            ps.setDate(2, new java.sql.Date(d.getFechaDevolucion().getTime()));
            ps.setString(3, d.getEstadoCopia());
            ps.setString(4, d.getObservaciones());
            ps.setInt(5, d.getIdUsuario());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setId(rs.getInt(1));
                    return d.getId();
                }
            }
        }
        return -1;
    }

    // LISTAR TODAS
    public List<Devolucion> listar() throws SQLException {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Devolucion ORDER BY id DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(map(rs));
            }
        }

        return lista;
    }

    // LISTAR POR PRESTAMO
    public List<Devolucion> listarPorPrestamo(int idPrestamo) throws SQLException {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Devolucion WHERE idPrestamo=? ORDER BY id DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        }

        return lista;
    }

    // BUSCAR POR ID
    public Devolucion buscar(int id) throws SQLException {
        String sql = "SELECT * FROM Devolucion WHERE id=?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return map(rs);
        }

        return null;
    }

    // ANULAR (lógica)
    public boolean anular(int id) throws SQLException {
        String sql = "UPDATE Devolucion SET estado=0 WHERE id=?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // MAPEO
    private Devolucion map(ResultSet rs) throws SQLException {
        return new Devolucion(
                rs.getInt("id"),
                rs.getInt("idPrestamo"),
                rs.getDate("fechaDevolucion"),
                rs.getString("estadoCopia"),
                rs.getString("observaciones"),
                rs.getInt("idUsuario"),
                rs.getInt("estado")
        );
    }
}