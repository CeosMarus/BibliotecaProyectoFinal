package app.dao;

import app.db.Conexion;
import app.model.Auditoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AuditoriaDAO extends BaseDAO {

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
                if (rs.next())
                {
                    //Registar la accion en Auditoria
                    auditar("Auditoria", "ListarAuditoria", "Se listo el registro con ID: " + id);
                    return mapAuditoria(rs);
                }
            }
        }
        return null;
    }

    // 🔹 LISTAR POR MÓDULO
    public List<Map<String, Object>> listarPorModulo(String modulo) throws SQLException {
        String sql = """
        SELECT A.id, A.fechaHora, A.modulo, A.accion, A.detalle,
               U.id AS idUsuario, U.nombreUsuario, U.rol
        FROM Auditoria A
        INNER JOIN Usuario U ON A.idUsuario = U.id
        WHERE A.modulo = ?
        ORDER BY A.fechaHora DESC
    """;

        List<Map<String, Object>> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, modulo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAuditoriaConUsuario(rs));
                }
            }

            // Registrar accion en auditoria
            auditar("Auditoria", "ListarPorModulo",
                    "Se listaron los registros de auditoría del módulo: " + modulo);

        } catch (SQLException e) {
            System.err.println("Error al listar auditorías por módulo: " + e.getMessage());
            throw e;
        }

        return lista;
    }

    // 🔹 LISTAR AUDITORÍAS POR USUARIO
    public List<Map<String, Object>> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = """
        SELECT A.id, A.fechaHora, A.modulo, A.accion, A.detalle,
               U.id AS idUsuario, U.nombreUsuario, U.rol
        FROM Auditoria A
        INNER JOIN Usuario U ON A.idUsuario = U.id
        WHERE U.id = ?
        ORDER BY A.fechaHora DESC
    """;

        List<Map<String, Object>> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAuditoriaConUsuario(rs));
                }
            }

            // Registrar acción en auditoría
            auditar("Auditoria", "ListarPorUsuario",
                    "Se listaron los registros de auditoría del usuario ID: " + idUsuario);

        } catch (SQLException e) {
            System.err.println("Error al listar auditorías por usuario: " + e.getMessage());
            throw e;
        }

        return lista;
    }

    // 🔹 LISTAR TODOS LOS REGISTROS
    public List<Map<String, Object>> listarConUsuario() throws SQLException {
        String sql = """
        SELECT A.id, A.fechaHora, A.modulo, A.accion, A.detalle,
               U.id AS idUsuario, U.nombreUsuario, U.rol
        FROM Auditoria A
        INNER JOIN Usuario U ON A.idUsuario = U.id
        ORDER BY A.fechaHora DESC
    """;

        List<Map<String, Object>> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapAuditoriaConUsuario(rs)); // 👈 aquí usamos el map
            }

            // Registrar la acción en auditoría
            auditar("Auditoria", "ListarAuditoria", "Se listaron todas las auditorías realizadas");

        } catch (SQLException e) {
            System.err.println("Error al listar auditorías con usuario: " + e.getMessage());
            throw e;
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
    // 🔹 Mapeo de ResultSet a Map con datos de Auditoría y Usuario
    private Map<String, Object> mapAuditoriaConUsuario(ResultSet rs) throws SQLException {
        Map<String, Object> fila = new HashMap<>();
        fila.put("id", rs.getInt("id"));
        fila.put("fechaHora", rs.getTimestamp("fechaHora"));
        fila.put("modulo", rs.getString("modulo"));
        fila.put("accion", rs.getString("accion"));
        fila.put("detalle", rs.getString("detalle"));
        fila.put("idUsuario", rs.getInt("idUsuario"));
        fila.put("usuario", rs.getString("nombreUsuario"));
        fila.put("rol", rs.getString("rol"));
        return fila;
    }
}
