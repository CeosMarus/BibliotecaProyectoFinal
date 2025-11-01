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
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea retirar esta reserva?", "Confirmar", JOptionPane.YES_NO_OPTION);
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
                        "Se retiro la reserva con ID: " + id);
                JOptionPane.showMessageDialog(null, "Reserva retirada correctamente.");
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

    // REORDENAR la cola de reservas de un libro
    public void reordenarCola(int idLibro) throws SQLException {
        String sqlSelect = "SELECT id FROM Reserva WHERE idLibro = ? AND estadoReserva = 1 ORDER BY fechaReserva ASC";
        String sqlUpdate = "UPDATE Reserva SET posicionCola = ? WHERE id = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement psSelect = con.prepareStatement(sqlSelect);
             PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {

            psSelect.setInt(1, idLibro);
            try (ResultSet rs = psSelect.executeQuery()) {

                int posicion = 1;
                while (rs.next()) {
                    int idReserva = rs.getInt("id");
                    psUpdate.setInt(1, posicion);
                    psUpdate.setInt(2, idReserva);
                    psUpdate.addBatch(); // agrupa las actualizaciones
                    posicion++;
                }

                psUpdate.executeBatch(); // ejecuta todas las actualizaciones juntas
            }

            // Registrar en auditoría
            auditar("Reservas", "ReordenarCola",
                    "Se reordenó la cola de reservas para el libro ID: " + idLibro);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al reordenar la cola: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
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
    //Actualizar estado de reserva
    //Estados posibles 1- Activo
    // 0 - retirado
    // 2 - Vencida
    // 3 - libro disponible confirmar prestamo?
    public boolean actualizarEstadoReserva(int idReserva, int nuevoEstado) throws SQLException {
        // Validación básica
        if (idReserva <= 0) {
            JOptionPane.showMessageDialog(null,
                    "ID de reserva inválido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Reserva SET estadoReserva = ? WHERE id = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevoEstado);
            ps.setInt(2, idReserva);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null,
                        "Estado de la reserva actualizado correctamente.",
                        "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);

                // 🔹 Registrar acción en auditoría
                auditar("Reservas", "ActualizarEstadoReserva",
                        "Se actualizó el estado de la reserva ID: " + idReserva +
                                " al estado: " + nuevoEstado);

                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontró la reserva con ID: " + idReserva,
                        "Sin resultados", JOptionPane.WARNING_MESSAGE);
                return false;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al actualizar el estado de la reserva: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
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
