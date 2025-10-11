package app.dao;

import app.db.Conexion;
import app.model.Auditoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    // 🔹 INSERTAR NUEVO REGISTRO DE AUDITORÍA
    public int insertar(Auditoria a) throws SQLException {
        if (a == null) throw new SQLException("Auditoria no puede ser nula.");

        String sql = "INSERT INTO Auditoria (fechaHora, idUsuario, modulo, accion, detalle) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, new java.sql.Timestamp(a.getFechaHora().getTime()));
            ps.setInt(2, a.getIdUsuario());
            ps.setString(3, a.getModulo());
            ps.setString(4, a.getAccion());
            ps.setString(5, a.getDetalle());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    a.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔹 BUSCAR POR ID
    public Auditoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, fechaHora, idUsuario, modulo, accion, detalle FROM Auditoria WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAuditoria(rs);
            }
        }
        return null;
    }

    // 🔹 LISTAR TODOS LOS REGISTROS
    public List<Auditoria> listar() throws SQLException {
        String sql = "SELECT id, fechaHora, idUsuario, modulo, accion, detalle FROM Auditoria ORDER BY fechaHora DESC";
        List<Auditoria> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapAuditoria(rs));
        }
        return lista;
    }

    // 🔹 Mapeo de ResultSet a objeto Auditoria
    private Auditoria mapAuditoria(ResultSet rs) throws SQLException {
        return new Auditoria(
                rs.getInt("id"),
                rs.getTimestamp("fechaHora"),
                rs.getInt("idUsuario"),
                rs.getString("modulo"),
                rs.getString("accion"),
                rs.getString("detalle")
        );
    }
}
