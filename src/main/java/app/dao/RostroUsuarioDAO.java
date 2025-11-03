package app.dao;

import app.db.Conexion;
import app.model.RostroUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class RostroUsuarioDAO extends BaseDAO {

    // INSERTAR RostroUsuario
    public void guardarPlantilla(int idUsuario, byte[] plantilla) throws SQLException {
        String sql = """
            INSERT INTO RostroUsuario (idUsuario, plantilla, fechaRegistro, estado)
            VALUES (?, ?, GETDATE(), 1)
        """;
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setBytes(2, plantilla);
            ps.executeUpdate();
        }
    }
    // ACTUALIZAR RostroUsuario
    public boolean insertarOActualizar(int idUsuario, byte[] plantilla) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM RostroUsuario WHERE idUsuario = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {

            psCheck.setInt(1, idUsuario);
            ResultSet rs = psCheck.executeQuery();
            boolean existe = rs.next() && rs.getInt(1) > 0;

            if (existe) {
                String update = "UPDATE RostroUsuario SET plantilla=?, fechaRegistro=GETDATE(), estado=1 WHERE idUsuario=?";
                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setBytes(1, plantilla);
                    ps.setInt(2, idUsuario);
                    return ps.executeUpdate() > 0;
                }
            } else {
                String insert = "INSERT INTO RostroUsuario (idUsuario, plantilla, fechaRegistro, estado) VALUES (?, ?, GETDATE(), 1)";
                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setInt(1, idUsuario);
                    ps.setBytes(2, plantilla);
                    return ps.executeUpdate() > 0;
                }
            }
        }
    }

    // LISTAR las plantillas
    public byte[] obtenerPlantilla(int idUsuario) throws SQLException {
        String sql = "SELECT plantilla FROM RostroUsuario WHERE idUsuario = ? AND estado = 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes("plantilla");
            }
        }
        return null;
    }

    // ELIMINACIÓN LÓGICA: cambia estado a 0
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea desactivar este rostro de usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE RostroUsuario SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            boolean desactivado = ps.executeUpdate() > 0;
            if (desactivado)
            {
                //Registo en auditoria
                auditar("Seguridad", "DesactivarRegistroRostro",
                        "Se desactivo el registro de rostro ID: " + id);
                JOptionPane.showMessageDialog(null, "Rostro desactivado correctamente.");
            }
            return desactivado;
        }
    }

    /*// BUSCAR por ID
    public RostroUsuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM RostroUsuario WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                {
                    //Registo en auditoria
                    auditar("Seguridad", "ListarRegistroRostro",
                            "Se listo el registro de rostro ID: " + id);
                    return mapRostroUsuario(rs);
                }
            }
        }
        return null;
    }

    // LISTAR por Usuario
    public List<RostroUsuario> listarPorUsuario(int idUsuario) throws SQLException {
        List<RostroUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM RostroUsuario WHERE idUsuario=? ORDER BY fechaRegistro DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRostroUsuario(rs));
            }
        }
        //Registo en auditoria
        auditar("Seguridad", "ListarRegistroRostro",
                "Se listaron los registros para el usuario ID: " + idUsuario);
        return lista;
    }*/
    /*
    public boolean actualizar(RostroUsuario r) throws SQLException {
        if (r == null || r.getId() == null) {
            JOptionPane.showMessageDialog(null, "RostroUsuario inválido para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE RostroUsuario SET idUsuario=?, plantilla=?, fechaRegistro=?, estado=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdUsuario());
            ps.setBytes(2, r.getPlantilla());
            ps.setDate(3, new java.sql.Date(r.getFechaRegistro().getTime()));
            ps.setInt(4, r.getEstado());
            ps.setInt(5, r.getId());

            boolean actualizado = ps.executeUpdate() > 0;
            if (actualizado)
            {
                //Registo en auditoria
                auditar("Seguridad", "ActualizarRegistroRostro",
                        "Se actualizo el registro de rostro para el usuario ID: " + r.getIdUsuario());
                JOptionPane.showMessageDialog(null, "Rostro actualizado correctamente.");
            }
            return actualizado;
        }
    }*/
    // HELPER: mapear ResultSet a RostroUsuario
    private RostroUsuario mapRostroUsuario(ResultSet rs) throws SQLException {
        return new RostroUsuario(
                rs.getInt("id"),
                rs.getInt("idUsuario"),
                rs.getBytes("plantilla"),
                rs.getDate("fechaRegistro"),
                rs.getInt("estado")
        );
    }
}
