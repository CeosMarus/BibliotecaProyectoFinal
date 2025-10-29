package app.dao;

import app.db.Conexion;
import app.model.Reserva;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ReservaDAO extends BaseDAO {

    // INSERTAR Reserva
    public int insertar(Reserva r) throws SQLException {
        if (r == null) throw new SQLException("Reserva no puede ser nula");

        String sql = "INSERT INTO Reserva (idCliente, idLibro, fechaReserva, estadoReserva, posicionCola) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, r.getIdLibro());
            ps.setDate(3, new java.sql.Date(r.getFechaReserva().getTime()));
            ps.setInt(4, r.getEstadoReserva());
            ps.setInt(5, r.getPosicionCola());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    r.setId(id);
                    JOptionPane.showMessageDialog(null, "Reserva registrada correctamente.");
                    //Registo en auditoria
                    auditar("Reservas", "NuevaReserva",
                            "Reserva de cliente ID: " + r.getIdCliente() + "para el libro ID: " + r.getIdLibro());
                    return id;
                }
            }
        }
        return -1;
    }

    // ACTUALIZAR Reserva
    public boolean actualizar(Reserva r) throws SQLException {
        if (r == null || r.getId() == null) {
            JOptionPane.showMessageDialog(null, "Reserva inválida para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Reserva SET idCliente=?, idLibro=?, fechaReserva=?, estadoReserva=?, posicionCola=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, r.getIdLibro());
            ps.setDate(3, new java.sql.Date(r.getFechaReserva().getTime()));
            ps.setInt(4, r.getEstadoReserva());
            ps.setInt(5, r.getPosicionCola());
            ps.setInt(6, r.getId());

            boolean actualizado = ps.executeUpdate() > 0;
            if (actualizado)
            {
                JOptionPane.showMessageDialog(null, "Reserva actualizada correctamente.");
                //Registo en auditoria
                auditar("Reservas", "ActualizaReserva",
                        "Se actualizo la reserva con ID: " + r.getId());
            }
            return actualizado;
        }
    }

    // ELIMINACIÓN LÓGICA: cambia estadoReserva a 0
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea desactivar esta reserva?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE Reserva SET estadoReserva=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            boolean desactivado = ps.executeUpdate() > 0;
            if (desactivado)
            {
                //Registo en auditoria
                auditar("Reservas", "DesactivarReserva",
                        "Se desactivo la reserva con ID: " + id);
                JOptionPane.showMessageDialog(null, "Reserva desactivada correctamente.");
            }
            return desactivado;
        }
    }

    // LISTAR todas las reservas
    public List<Reserva> listar() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM Reserva ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapReserva(rs));
            }
        }
        //Registo en auditoria
        auditar("Reservas", "ListarReserva",
                "Se listaron todas las reservas");
        return lista;
    }

    // LISTAR solo reservas activas
    public List<Reserva> listarActivas() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM Reserva WHERE estadoReserva=1 ORDER BY id DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapReserva(rs));
            }
        }
        //Registo en auditoria
        auditar("Reservas", "ListarReserva",
                "Se listaron las reservas activas");
        return lista;
    }

    // BUSCAR por ID
    public Reserva buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Reserva WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                {
                    //Registo en auditoria
                    auditar("Reservas", "ListarReserva",
                            "Se busco la reserva ID: " + id );
                    return mapReserva(rs);
                }
            }
        }
        return null;
    }

    // LISTAR por Cliente
    public List<Reserva> listarPorCliente(int idCliente) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM Reserva WHERE idCliente=? AND estadoReserva=1 ORDER BY fechaReserva DESC";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapReserva(rs));
            }
        }
        //Registo en auditoria
        auditar("Reservas", "ListarReserva",
                "Se listo las reservas del cliente ID: " + idCliente);
        return lista;
    }

    // HELPER: mapear ResultSet a Reserva
    private Reserva mapReserva(ResultSet rs) throws SQLException {
        return new Reserva(
                rs.getInt("id"),
                rs.getInt("idCliente"),
                rs.getInt("idLibro"),
                rs.getDate("fechaReserva"),
                rs.getInt("estadoReserva"),
                rs.getInt("posicionCola")
        );
    }
}
