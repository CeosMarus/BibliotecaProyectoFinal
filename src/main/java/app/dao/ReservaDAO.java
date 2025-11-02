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

    // VERIFICAR ejemplares disponibles por título (excluyendo los prestados y reservados)
    public int verificarEjemplaresDisponibles(String tituloLibro) throws SQLException {
        String sql = """
        SELECT COUNT(e.id) AS disponibles
        FROM Ejemplar e
        JOIN Libro l ON e.idLibro = l.id
        WHERE l.titulo = ?
          AND e.estado = 1
          AND e.id NOT IN (
              SELECT p.idEjemplar
              FROM Prestamo p
              WHERE p.estado = 1
          )
          AND l.id NOT IN (
              SELECT r.idLibro
              FROM Reserva r
              WHERE r.estadoReserva IN (1, 2) -- 1 = En cola, 2 = Ejemplar disponible
          )
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tituloLibro);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("disponibles");
                }
            }
        }
        return 0;
    }

    //LISTAR reservas con datos del cliente y libro
    public List<Object[]> listarReservasConDetalles() throws SQLException {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
        SELECT 
            r.id, 
            c.nombre AS cliente, 
            c.nit, 
            l.titulo AS libro, 
            r.fechaReserva, 
            CASE r.estadoReserva
                WHEN 0 THEN 'Retirada'
                WHEN 1 THEN 'Activo, en cola'
                WHEN 2 THEN 'Ejemplar Disponible'
                WHEN 3 THEN 'Vencida'
                ELSE 'Desconocido'
            END AS estadoReservaNombre,
            r.posicionCola
        FROM Reserva r
        JOIN Cliente c ON r.idCliente = c.id
        JOIN Libro l ON r.idLibro = l.id
        WHERE r.estadoReserva != 0
        ORDER BY r.fechaReserva DESC
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("cliente"),
                        rs.getString("nit"),
                        rs.getString("libro"),
                        rs.getDate("fechaReserva"),
                        rs.getString("estadoReservaNombre"), // aquí ya viene como texto
                        rs.getInt("posicionCola")
                });
            }
        }

        auditar("Reservas", "ListarConDetalles",
                "Se listaron reservas con información de cliente y libro");
        return lista;
    }
    // LISTAR reservas filtradas por cliente con datos del libro
    public List<Object[]> listarReservasPorCliente(int idCliente) throws SQLException {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
        SELECT 
            r.id, 
            c.nombre AS cliente, 
            c.nit, 
            l.titulo AS libro, 
            r.fechaReserva, 
            CASE r.estadoReserva
                WHEN 0 THEN 'Retirada'
                WHEN 1 THEN 'Activo, en cola'
                WHEN 2 THEN 'Ejemplar Disponible'
                WHEN 3 THEN 'Vencida'
                ELSE 'Desconocido'
            END AS estadoReservaNombre,
            r.posicionCola
        FROM Reserva r
        JOIN Cliente c ON r.idCliente = c.id
        JOIN Libro l ON r.idLibro = l.id
        WHERE r.idCliente = ?
          AND r.estadoReserva != 0
        ORDER BY r.fechaReserva DESC
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("cliente"),
                            rs.getString("nit"),
                            rs.getString("libro"),
                            rs.getDate("fechaReserva"),
                            rs.getString("estadoReservaNombre"),
                            rs.getInt("posicionCola")
                    });
                }
            }
        }

        auditar("Reservas", "ListarPorCliente",
                "Se listaron reservas del cliente con ID: " + idCliente);

        return lista;
    }

    //Actualizar estado de reserva
    //Estados posibles
    // 0 - retirado
    // 1- Activo
    // 2 - libro disponible confirmar prestamo?
    // 3 - Vencida
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

    //ACTUALIZAR posiciones en cola
    public void actualizarPosicionesCola(int idLibro) throws SQLException {
        String sqlSelect = """
        SELECT id 
        FROM Reserva 
        WHERE idLibro = ? 
          AND estadoReserva IN (1, 2)
        ORDER BY fechaReserva ASC
    """;

        String sqlUpdate = "UPDATE Reserva SET posicionCola = ? WHERE id = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement psSelect = con.prepareStatement(sqlSelect);
             PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {

            psSelect.setInt(1, idLibro);
            try (ResultSet rs = psSelect.executeQuery()) {
                int pos = 1;
                while (rs.next()) {
                    psUpdate.setInt(1, pos++);
                    psUpdate.setInt(2, rs.getInt("id"));
                    psUpdate.addBatch();
                }
                psUpdate.executeBatch();
            }

            auditar("Reservas", "ActualizarPosiciones",
                    "Se reordenaron las posiciones de cola para libro ID: " + idLibro);
        }
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
    //Calcular posición en cola desde SQL (solo reservas activas en cola para ese libro)
    public int calcularPosicionCola(int idLibro) throws SQLException {
        String sql = """
        SELECT COUNT(*) AS total
        FROM Reserva
        WHERE idLibro = ? AND estadoReserva IN (1, 2)
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") + 1; // la nueva será la siguiente
                }
            }
        }
        return 1; // si no hay reservas previas, será la primera
    }
    // ELIMINACIÓN LÓGICA: cambia estadoReserva a 0
    public boolean eliminar(int id) throws SQLException {

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
    public int obtenerSiguienteEnCola(int idLibro) throws SQLException {
        String sql = """
        SELECT id 
        FROM Reserva
        WHERE idLibro = ? AND estadoReserva = 1
        ORDER BY posicionCola ASC
        LIMIT 1
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1; // no hay reservas en cola
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



    /*// BUSCAR por ID
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
    }*/
    /*//ACTUALIZAR reservas vencidas (más de 24h en estado 2)
    public void actualizarReservasVencidas() throws SQLException {
        String sql = """
            UPDATE Reserva
            SET estadoReserva = 3
            WHERE estadoReserva = 2
              AND DATEDIFF(HOUR, fechaReserva, GETDATE()) >= 24
        """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int filas = ps.executeUpdate();
            if (filas > 0) {
                auditar("Reservas", "ActualizarVencidas",
                        "Se actualizaron " + filas + " reservas vencidas");
            }
        }
    }*/

}
