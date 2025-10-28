package app.dao;

import app.model.Arqueo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArqueoDAO extends BaseDAO {

    private Connection conn;

    public ArqueoDAO(Connection conn) {
        this.conn = conn;
    }

    // 🏗️ Insertar nuevo arqueo
    public boolean insertar(Arqueo arqueo) throws SQLException {
        String sql = "INSERT INTO Arqueo (fecha, hora, idApertura, esperadoEfectivo, contadoEfectivo, diferencia, idUsuario, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(arqueo.getFecha().getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(arqueo.getHora().getTime()));
            ps.setInt(3, arqueo.getIdApertura());
            ps.setFloat(4, arqueo.getEsperadoEfectivo());
            ps.setFloat(5, arqueo.getContadoEfectivo());
            ps.setFloat(6, arqueo.getDiferencia());
            ps.setInt(7, arqueo.getIdUsuario());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                auditar("Financiero", "NuevoArqueo", "Se realizó arqueo, ID: " + arqueo.getId());
                return true;
            }
            return false;
        }
    }

    // 🏗️ Actualizar arqueo existente
    public boolean actualizar(Arqueo arqueo) throws SQLException {
        String sql = "UPDATE Arqueo SET fecha = ?, hora = ?, idApertura = ?, esperadoEfectivo = ?, contadoEfectivo = ?, diferencia = ?, idUsuario = ? " +
                "WHERE id = ? AND estado = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(arqueo.getFecha().getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(arqueo.getHora().getTime()));
            ps.setInt(3, arqueo.getIdApertura());
            ps.setFloat(4, arqueo.getEsperadoEfectivo());
            ps.setFloat(5, arqueo.getContadoEfectivo());
            ps.setFloat(6, arqueo.getDiferencia());
            ps.setInt(7, arqueo.getIdUsuario());
            ps.setInt(8, arqueo.getId());
            //Registar la accion en Auditoria
            auditar("Financiero", "ActualizarArqueo", "Se modifico el arqueo con ID: " + arqueo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // 🏗️ Eliminación lógica
    public boolean eliminar(int id) throws SQLException {
        String sql = "UPDATE Arqueo SET estado = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            //Registar la accion en Auditoria
            auditar("Financiero", "DesactivarArqueo", "Se Desactivo el arqueo con ID: " + id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🏗️ Obtener arqueo por ID
    public Arqueo obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Arqueo WHERE id = ? AND estado = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                //Registar la accion en Auditoria
                auditar("Financiero", "ListarArqueo", "Se busco el arqueo con ID: " + id);
                return mapearArqueo(rs);
            }
            return null;
        }
    }

    // 🏗️ Listar todos los arqueos activos
    public List<Arqueo> listarTodos() throws SQLException {
        List<Arqueo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Arqueo WHERE estado = 1";
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                lista.add(mapearArqueo(rs));
            }
        }
        //Registar la accion en Auditoria
        auditar("Financiero", "ListarArqueos", "Se listaron todos los arqueos activos");
        return lista;
    }

    // 🏗️ Método privado para mapear ResultSet a objeto Arqueo
    private Arqueo mapearArqueo(ResultSet rs) throws SQLException {
        Arqueo arqueo = new Arqueo();
        arqueo.setId(rs.getInt("id"));
        arqueo.setFecha(rs.getDate("fecha"));
        arqueo.setHora(rs.getTimestamp("hora"));
        arqueo.setIdApertura(rs.getInt("idApertura"));
        arqueo.setEsperadoEfectivo(rs.getFloat("esperadoEfectivo"));
        arqueo.setContadoEfectivo(rs.getFloat("contadoEfectivo"));
        arqueo.setDiferencia(rs.getFloat("diferencia"));
        arqueo.setIdUsuario(rs.getInt("idUsuario"));
        arqueo.setEstado(rs.getInt("estado"));
        return arqueo;
    }
}
