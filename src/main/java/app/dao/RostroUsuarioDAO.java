package app.dao;

import app.db.Conexion;
import app.model.RostroUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class RostroUsuarioDAO extends BaseDAO {

    // INSERTAR RostroUsuario
    public int insertar(RostroUsuario r) throws SQLException {
        if (r == null) throw new SQLException("RostroUsuario no puede ser nulo");

        String sql = "INSERT INTO RostroUsuario (idUsuario, plantilla, fechaRegistro, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getIdUsuario());
            ps.setBytes(2, r.getPlantilla());
            ps.setDate(3, new java.sql.Date(r.getFechaRegistro().getTime()));
            ps.setInt(4, r.getEstado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    r.setId(id);
                    JOptionPane.showMessageDialog(null, "Rostro registrado correctamente.");
                    //Registo en auditoria
                    auditar("Seguridad", "CrearRegistroRostro",
                            "Se creo el registro de rostro para el usuario ID: " + r.getIdUsuario());
                    return id;
                }
            }
        }
        return -1;
    }

    // ACTUALIZAR RostroUsuario
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

    // LISTAR todos los rostros
    public List<RostroUsuario> listar() throws SQLException {
        List<RostroUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM RostroUsuario ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRostroUsuario(rs));
            }
        }
        //Registo en auditoria
        auditar("Seguridad", "ListarRegistroRostro",
                "Se listaron todos los registros de rostros" );
        return lista;
    }

    // BUSCAR por ID
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
    }

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
