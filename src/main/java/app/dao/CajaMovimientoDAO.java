package app.dao;

import app.db.Conexion;
import app.model.CajaMovimiento;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CajaMovimientoDAO {

    // 🔹 INSERTAR MOVIMIENTO
    public int insertar(CajaMovimiento movimiento) throws SQLException {
        if (movimiento == null) throw new SQLException("Movimiento no puede ser nulo.");

        String sql = "INSERT INTO CajaMovimiento (fecha, hora, tipo, subtipo, monto, idUsuario, descripcion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, new java.sql.Date(movimiento.getFecha().getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(movimiento.getHora().getTime()));
            ps.setInt(3, movimiento.getTipo());
            ps.setString(4, movimiento.getSubtipo());
            ps.setBigDecimal(5, movimiento.getMonto());
            ps.setInt(6, movimiento.getIdUsuario());
            ps.setString(7, movimiento.getDescripcion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    movimiento.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔹 LISTAR TODOS LOS MOVIMIENTOS
    public List<CajaMovimiento> listar() throws SQLException {
        List<CajaMovimiento> lista = new ArrayList<>();
        String sql = "SELECT id, fecha, hora, tipo, subtipo, monto, idUsuario, descripcion FROM CajaMovimiento ORDER BY fecha DESC, hora DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCajaMovimiento(rs));
            }
        }
        return lista;
    }

    // 🔹 LISTAR POR USUARIO
    public List<CajaMovimiento> listarPorUsuario(int idUsuario) throws SQLException {
        List<CajaMovimiento> lista = new ArrayList<>();
        String sql = "SELECT id, fecha, hora, tipo, subtipo, monto, idUsuario, descripcion FROM CajaMovimiento WHERE idUsuario=? ORDER BY fecha DESC, hora DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapCajaMovimiento(rs));
                }
            }
        }
        return lista;
    }

    // 🔹 BUSCAR POR ID
    public CajaMovimiento buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, fecha, hora, tipo, subtipo, monto, idUsuario, descripcion FROM CajaMovimiento WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCajaMovimiento(rs);
            }
        }
        return null;
    }

    // 🔹 Mapeo del ResultSet a CajaMovimiento
    private CajaMovimiento mapCajaMovimiento(ResultSet rs) throws SQLException {
        return new CajaMovimiento(
                rs.getInt("id"),
                rs.getDate("fecha"),
                rs.getTimestamp("hora"),
                rs.getInt("tipo"),
                rs.getString("subtipo"),
                rs.getBigDecimal("monto"),
                rs.getInt("idUsuario"),
                rs.getString("descripcion")
        );
    }
}
