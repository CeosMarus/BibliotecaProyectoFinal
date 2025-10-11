package app.dao;

import app.db.Conexion;
import app.model.Multa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class MultaDAO {

    // 🔹 INSERTAR MULTA
    public int insertar(Multa multa) throws SQLException {
        if (multa == null) throw new SQLException("La multa no puede ser nula.");

        String sql = "INSERT INTO Multa (idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, multa.getIdPrestamo());
            ps.setInt(2, multa.getIdCliente());
            ps.setBigDecimal(3, multa.getMonto());
            ps.setInt(4, multa.getDiasAtraso());
            ps.setInt(5, multa.getEstadoPago());
            if (multa.getFechaPago() != null) {
                ps.setDate(6, new java.sql.Date(multa.getFechaPago().getTime()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, multa.getObservaciones());
            ps.setInt(8, 1); // Activo al insertar

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    multa.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔹 ACTUALIZAR MULTA
    public boolean actualizar(Multa multa) throws SQLException {
        if (multa == null) throw new SQLException("La multa no puede ser nula.");

        String sql = "UPDATE Multa SET idPrestamo=?, idCliente=?, monto=?, diasAtraso=?, estadoPago=?, fechaPago=?, observaciones=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, multa.getIdPrestamo());
            ps.setInt(2, multa.getIdCliente());
            ps.setBigDecimal(3, multa.getMonto());
            ps.setInt(4, multa.getDiasAtraso());
            ps.setInt(5, multa.getEstadoPago());
            if (multa.getFechaPago() != null) {
                ps.setDate(6, new java.sql.Date(multa.getFechaPago().getTime()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, multa.getObservaciones());
            ps.setInt(8, multa.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 ELIMINACIÓN LÓGICA (Desactivar)
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea desactivar esta multa?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE Multa SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 BUSCAR POR ID
    public Multa buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado FROM Multa WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMulta(rs);
            }
        }
        return null;
    }

    // 🔹 LISTAR TODAS
    public List<Multa> listar() throws SQLException {
        String sql = "SELECT id, idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado FROM Multa ORDER BY id DESC";
        List<Multa> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapMulta(rs));
        }
        return lista;
    }

    // 🔹 LISTAR SOLO ACTIVAS
    public List<Multa> listarActivas() throws SQLException {
        String sql = "SELECT id, idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado FROM Multa WHERE estado=1 ORDER BY id DESC";
        List<Multa> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapMulta(rs));
        }
        return lista;
    }

    // 🔹 Mapeo del ResultSet a Multa
    private Multa mapMulta(ResultSet rs) throws SQLException {
        Multa multa = new Multa();
        multa.setId(rs.getInt("id"));
        multa.setIdPrestamo(rs.getInt("idPrestamo"));
        multa.setIdCliente(rs.getInt("idCliente"));
        multa.setMonto(rs.getBigDecimal("monto"));
        multa.setDiasAtraso(rs.getInt("diasAtraso"));
        multa.setEstadoPago(rs.getInt("estadoPago"));
        Date fechaPago = rs.getDate("fechaPago");
        multa.setFechaPago(fechaPago != null ? new java.util.Date(fechaPago.getTime()) : null);
        multa.setObservaciones(rs.getString("observaciones"));
        multa.setEstado(rs.getInt("estado"));
        return multa;
    }
}
