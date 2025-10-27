package app.dao;

import app.db.Conexion;
import app.model.AperturaCaja;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Importación necesaria para el saldo

//Se utiliza extends para utilizar auditar(), en cualquier metodo
public class AperturaCajaDAO extends BaseDAO {

    // 🟢 ABRIR CAJA (Insertar nueva apertura)
    public int abrirCaja(AperturaCaja caja) throws SQLException {
        if (caja == null) throw new SQLException("Apertura de caja no puede ser nula.");
        if (caja.getIdUsuario() <= 0) throw new SQLException("Usuario inválido.");
        if (caja.getSaldoInicial().compareTo(BigDecimal.ZERO) < 0)
            throw new SQLException("Saldo inicial no puede ser negativo.");

        String sql = "INSERT INTO AperturaCaja (idUsuario, fecha, hora, saldoincial, estado) VALUES (?, ?, ?, ?, 1)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, caja.getIdUsuario());
            ps.setDate(2, new java.sql.Date(caja.getFecha().getTime()));
            ps.setTimestamp(3, new java.sql.Timestamp(caja.getHora().getTime()));
            ps.setBigDecimal(4, caja.getSaldoInicial());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    caja.setId(id);
                    JOptionPane.showMessageDialog(null, "Caja abierta correctamente.");
                    //Registar la accion en Auditoria
                    auditar("Financiero", "AbrirCaja", "Se realizo apertura de caja:  ID " + caja.getId());
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔒 CERRAR CAJA (Actualización de estado)
    public boolean cerrarCaja(int id) throws SQLException {
        // Tu método ya estaba correcto
        String sql = "UPDATE AperturaCaja SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Caja cerrada correctamente.");
                //Registar la accion en Auditoria
                auditar("Financiero", "CerrarCaja", "Se realizo el cierre de caja:  ID " + id);
                return true;
            }
        }
        return false;
    }

    // 🗑 ELIMINACIÓN LÓGICA (Desactivar caja)
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Desea desactivar esta apertura de caja?",
                "Confirmar eliminación lógica",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE AperturaCaja SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Apertura de caja desactivada correctamente.");
                //Registar la accion en Auditoria
                auditar("Financiero", "DesactivarAperturaCaja", "Se desactivo apertura de caja :  ID " + id);
                return true;
            }
        }
        return false;
    }

    // 🔍 BUSCAR POR ID
    public AperturaCaja buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {   //Registar la accion en Auditoria
                    auditar("Financiero", "BuscarAperturaCaja", "Se realizo busqueda de apertura:  ID " + id);
                    return mapAperturaCaja(rs);
                }
            }
        }
        return null;
    }

    // 📋 LISTAR TODAS LAS APERTURAS DE CAJA
    public List<AperturaCaja> listar() throws SQLException {
        List<AperturaCaja> lista = new ArrayList<>();
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapAperturaCaja(rs));
            }
        }
        //Registar la accion en Auditoria
        auditar("Financiero", "ListarAperturasCaja", "Se listo todas las aperturas de caja");
        return lista;
    }

    // 📋 LISTAR SOLO ACTIVAS
    public List<AperturaCaja> listarActivas() throws SQLException {
        List<AperturaCaja> lista = new ArrayList<>();
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja WHERE estado = 1 ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapAperturaCaja(rs));
            }
        }
        //Registar la accion en Auditoria
        auditar("Financiero", "ListarAperturasActivas", "Se listo apeturas de caja activas");
        return lista;
    }

    /**
     * Obtiene la única caja activa actualmente.
     * @return El objeto AperturaCaja activa o null.
     */
    public AperturaCaja obtenerActiva() throws SQLException {
        // La columna en SQL es saldoincial (minúscula)
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja WHERE estado = 1";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                //Registar la accion en Auditoria
                auditar("Financiero", "ObtenerAperturaActiva", "Se busco paertura de caja activa");
                // Se usa el método de mapeo, que ya es correcto
                return mapAperturaCaja(rs);
            }
        }
        //Registar la accion en Auditoria
        auditar("Financiero", "ObtenerAperturaActiva", "Se busco paertura de caja activa, no hay caja activa");
        return null; // Retorna null si no hay caja activa
    }

    // 🧭 MAPEO DE RESULTSET A OBJETO AperturaCaja
    private AperturaCaja mapAperturaCaja(ResultSet rs) throws SQLException {
        AperturaCaja caja = new AperturaCaja();
        caja.setId(rs.getInt("id"));
        caja.setIdUsuario(rs.getInt("idUsuario"));
        caja.setFecha(rs.getDate("fecha"));
        caja.setHora(rs.getTimestamp("hora"));
        // El nombre de la columna en la BD es saldoincial
        caja.setSaldoInicial(rs.getBigDecimal("saldoincial"));
        caja.setEstado(rs.getInt("estado"));
        return caja;
    }
}