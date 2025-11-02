package app.dao;

import app.db.Conexion;
import app.model.Multa;
import app.model.MultaDetalle;
import app.model.CajaMovimiento;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultaDAO {

    // Instancia del DAO de movimientos para registrar los ingresos en caja
    private final CajaMovimientoDAO movimientoDAO = new CajaMovimientoDAO();

    // JOIN para obtener el nombre del cliente y otros detalles
    private final String SQL_LISTAR_DETALLES =
            "SELECT M.id, C.nombre AS nombreCliente, P.id AS idPrestamo, M.idCliente, M.monto, M.diasAtraso, M.estadoPago, M.fechaPago, M.observaciones, M.estado " +
                    "FROM Multa M " +
                    "JOIN Cliente C ON M.idCliente = C.id " +
                    "JOIN Prestamo P ON M.idPrestamo = P.id " +
                    "WHERE M.estado = 1 ORDER BY M.id DESC";

    //Query para buscar por nombre de cliente
    private final String SQL_BUSCAR_POR_NOMBRE =
            "SELECT M.id, C.nombre AS nombreCliente, P.id AS idPrestamo, M.idCliente, M.monto, M.diasAtraso, M.estadoPago, M.fechaPago, M.observaciones, M.estado " +
                    "FROM Multa M " +
                    "JOIN Cliente C ON M.idCliente = C.id " +
                    "JOIN Prestamo P ON M.idPrestamo = P.id " +
                    "WHERE M.estado = 1 AND C.nombre LIKE ? " + // 💡 Condición de búsqueda
                    "ORDER BY C.nombre ASC, M.id DESC";

    public int insertar(Multa multa, int idUsuario) throws SQLException {
        if (multa == null) throw new SQLException("La multa no puede ser nula.");

        String sql = "INSERT INTO Multa (idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        // Inserción de la Multa
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, multa.getIdPrestamo());
            ps.setInt(2, multa.getIdCliente());
            ps.setBigDecimal(3, multa.getMonto());
            ps.setInt(4, multa.getDiasAtraso());
            ps.setInt(5, multa.getEstadoPago());

            // Manejo de Fecha Pago
            if (multa.getFechaPago() != null) {
                ps.setDate(6, new java.sql.Date(multa.getFechaPago().getTime()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            //registro de observacion de multas
            ps.setString(7, multa.getObservaciones());
            ps.setInt(8, 1); // Activo

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                    multa.setId(idGenerado);
                }
            }
        }

        // Registra Movimiento en Caja si la multa fue pagada al momento de crearla
        if (idGenerado > 0 && multa.getEstadoPago() == 1) { // estadoPago = 1 (Pagado)
            Date ahora = new Date();
            CajaMovimiento movimiento = new CajaMovimiento(
                    ahora,
                    ahora,
                    1,
                    "Multa",
                    multa.getMonto(),
                    idUsuario,
                    "Pago de Multa ID: " + idGenerado + ". Préstamo: " + multa.getIdPrestamo()
            );
            //se usa el metodo de insertar
            try {
                movimientoDAO.insertar(movimiento);
            } catch (SQLException e) {
                //En caso que falle se mostrara un mensaje en la consola sobre el error
                Logger.getLogger(MultaDAO.class.getName()).log(Level.SEVERE, "Error al registrar movimiento de caja para Multa ID: " + idGenerado, e);
            }
        }

        return idGenerado;
    }
    // actualiza el dato de la multa
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
    // Registra la fecha de pago
    public boolean registrarCobroYCaja(Multa multa, int idUsuario) throws SQLException {
        if (multa == null || multa.getId() == null) throw new SQLException("Multa o ID nulo.");

        // Marcar como pagada y establecer fecha actual
        multa.setEstadoPago(1);
        multa.setFechaPago(new Date());

        // Actualizar la multa en la base de datos
        boolean multaActualizada = actualizar(multa);

        // Registrar el movimiento en caja (si se actualizó correctamente)
        if (multaActualizada) {
            Date ahora = new Date();
            CajaMovimiento movimiento = new CajaMovimiento(
                    ahora,
                    ahora,
                    1, // Tipo 1: Ingreso
                    "Cobro Multa Pendiente",
                    multa.getMonto(),
                    idUsuario,
                    "Cobro de Multa Pendiente ID: " + multa.getId() + ". Préstamo: " + multa.getIdPrestamo()
            );
            try {
                movimientoDAO.insertar(movimiento);
            } catch (SQLException e) {
                Logger.getLogger(MultaDAO.class.getName()).log(Level.SEVERE, "Error al registrar cobro en caja para Multa ID: " + multa.getId(), e);
            }
            return true;
        }
        return false;
    }
    // Realiza la eliminacion logica
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
    //Busca por el ID del cliente
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

    // Busca Multas por el nombre del cliente
    public List<MultaDetalle> buscarPorNombreCliente(String nombre) throws SQLException {
        List<MultaDetalle> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            // Permite la busqueda haciendo similitud en los caracteres
            ps.setString(1, "%" + nombre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapMultaDetalle(rs));
                }
            }
        }
        return lista;
    }
    // Listar Datos
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
    //listara los pendientes
    public List<MultaDetalle> listarPendientesConDetalles() throws SQLException {
        List<MultaDetalle> lista = new ArrayList<>();
        // Filtra por estado=1 (Activa) y estadoPago=0 (Pendiente) y trae el nombre del cliente
        String sql = "SELECT M.id, C.nombre AS nombreCliente, P.id AS idPrestamo, M.idCliente, M.monto, M.diasAtraso, M.estadoPago, M.fechaPago, M.observaciones, M.estado " +
                "FROM Multa M " +
                "JOIN Cliente C ON M.idCliente = C.id " +
                "JOIN Prestamo P ON M.idPrestamo = P.id " +
                "WHERE M.estado = 1 AND M.estadoPago = 0 ORDER BY C.nombre ASC, M.id DESC"; // Filtrar pendientes

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapMultaDetalle(rs));
            }
        }
        return lista;
    }

    //Metodo para listar los detalles de las multas
    public List<MultaDetalle> listarConDetalles() throws SQLException {
        List<MultaDetalle> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_DETALLES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapMultaDetalle(rs));
            }
        }
        return lista;
    }
    // Listar multas del cliente (solo lectura para cliente)
    public List<MultaDetalle> listarMultasPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT M.id, C.nombre AS nombreCliente, P.id AS idPrestamo, M.idCliente, " +
                "M.monto, M.diasAtraso, M.estadoPago, M.fechaPago, M.observaciones, M.estado " +
                "FROM Multa M " +
                "JOIN Cliente C ON M.idCliente = C.id " +
                "JOIN Prestamo P ON M.idPrestamo = P.id " +
                "WHERE M.estado = 1 AND M.idCliente = ? " +
                "ORDER BY M.id DESC";

        List<MultaDetalle> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapMultaDetalle(rs));
            }
        }
        return lista;
    }

    private Multa mapMulta(ResultSet rs) throws SQLException {
        Multa multa = new Multa();
        multa.setId(rs.getInt("id"));
        multa.setIdPrestamo(rs.getInt("idPrestamo"));
        multa.setIdCliente(rs.getInt("idCliente"));
        multa.setMonto(rs.getBigDecimal("monto"));
        multa.setDiasAtraso(rs.getInt("diasAtraso"));
        multa.setEstadoPago(rs.getInt("estadoPago"));
        Date fechaPago = rs.getDate("fechaPago");
        // Convertir java.sql.Date a java.util.Date
        multa.setFechaPago(fechaPago != null ? new java.util.Date(fechaPago.getTime()) : null);
        multa.setObservaciones(rs.getString("observaciones"));
        multa.setEstado(rs.getInt("estado"));
        return multa;
    }

    private MultaDetalle mapMultaDetalle(ResultSet rs) throws SQLException {
        MultaDetalle detalle = new MultaDetalle();

        //campos de Multa (usa los setters heredados)
        detalle.setId(rs.getInt("id"));
        detalle.setIdPrestamo(rs.getInt("idPrestamo"));
        detalle.setIdCliente(rs.getInt("idCliente"));
        detalle.setMonto(rs.getBigDecimal("monto"));
        detalle.setDiasAtraso(rs.getInt("diasAtraso"));
        detalle.setEstadoPago(rs.getInt("estadoPago"));
        Date fechaPago = rs.getDate("fechaPago");
        detalle.setFechaPago(fechaPago != null ? new java.util.Date(fechaPago.getTime()) : null);
        detalle.setObservaciones(rs.getString("observaciones"));
        detalle.setEstado(rs.getInt("estado"));

        // 2. Mapear campos nuevos del JOIN
        detalle.setNombreCliente(rs.getString("nombreCliente"));

        return detalle;
    }
}