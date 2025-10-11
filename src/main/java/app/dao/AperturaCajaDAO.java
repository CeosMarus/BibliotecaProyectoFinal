package app.dao;

import app.db.Conexion;
import app.model.AperturaCaja;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AperturaCajaDAO {

    // ➕ INSERTAR NUEVA APERTURA DE CAJA
    public int abrirCaja(AperturaCaja caja) throws SQLException {
        if (caja == null) throw new SQLException("Apertura de caja no puede ser nula.");
        if (caja.getIdUsuario() <= 0) throw new SQLException("Usuario inválido.");
        if (caja.getSaldoInicial() < 0) throw new SQLException("Saldo inicial no puede ser negativo.");

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
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔁 CERRAR CAJA (actualización de estado)
    public boolean cerrarCaja(int id) throws SQLException {
        String sql = "UPDATE AperturaCaja SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Caja cerrada correctamente.");
                return true;
            }
        }
        return false;
    }

    // ❌ ELIMINACIÓN LÓGICA (solo si fuera necesario)
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea desactivar esta apertura de caja?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE AperturaCaja SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Apertura de caja desactivada correctamente.");
                return true;
            }
        }
        return false;
    }

    // 🔹 BUSCAR POR ID
    public AperturaCaja buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAperturaCaja(rs);
            }
        }
        return null;
    }

    // 🔹 LISTAR TODAS LAS APERTURAS DE CAJA
    public List<AperturaCaja> listar() throws SQLException {
        List<AperturaCaja> lista = new ArrayList<>();
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapAperturaCaja(rs));
        }
        return lista;
    }

    // 🔹 LISTAR SOLO ACTIVAS
    public List<AperturaCaja> listarActivas() throws SQLException {
        List<AperturaCaja> lista = new ArrayList<>();
        String sql = "SELECT id, idUsuario, fecha, hora, saldoincial, estado FROM AperturaCaja WHERE estado=1 ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapAperturaCaja(rs));
        }
        return lista;
    }

    // 🔹 MAPEO DEL RESULTSET A OBJETO
    private AperturaCaja mapAperturaCaja(ResultSet rs) throws SQLException {
        AperturaCaja caja = new AperturaCaja();
        caja.setId(rs.getInt("id"));
        caja.setIdUsuario(rs.getInt("idUsuario"));
        caja.setFecha(rs.getDate("fecha"));
        caja.setHora(rs.getTimestamp("hora"));
        caja.setSaldoInicial(rs.getBigDecimal("saldoincial"));
        caja.setEstado(rs.getInt("estado"));
        return caja;
    }
}
