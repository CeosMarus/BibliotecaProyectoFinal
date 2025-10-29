package app.dao;

import app.model.SolicitudCompra;
import app.db.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCompraDAO extends BaseDAO {

    private Connection conn;

    public SolicitudCompraDAO() {
        try {
            conn = Conexion.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🟢 Listar todas las solicitudes activas
    public List<SolicitudCompra> listar() {
        List<SolicitudCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM SolicitudCompra WHERE estado <> 1"; // Sólo activas
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SolicitudCompra solicitud = new SolicitudCompra(
                        rs.getInt("id"),
                        rs.getDate("fecha"),
                        rs.getInt("idUsuario"),
                        rs.getInt("idLibro"),
                        rs.getInt("cantidad"),
                        rs.getBigDecimal("costoUnitario").doubleValue(),
                        rs.getInt("estado")
                );
                lista.add(solicitud);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Registo en auditoria
        auditar("SolicitudesCompra", "ListarSolicitud",
                "Se listo las solicitudes de compra activas");
        return lista;
    }

    // 🟢 Obtener una solicitud por ID
    public SolicitudCompra obtenerPorId(int id) {
        String sql = "SELECT * FROM SolicitudCompra WHERE id = ? AND estado <> 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Registo en auditoria
                    auditar("SolicitudesCompra", "ListarSolicitud",
                            "Se listo las solicitudes de compra con ID = " + id);
                    return new SolicitudCompra(
                            rs.getInt("id"),
                            rs.getDate("fecha"),
                            rs.getInt("idUsuario"),
                            rs.getInt("idLibro"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("costoUnitario").doubleValue(),
                            rs.getInt("estado")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🟢 Insertar nueva solicitud
    public boolean insertar(SolicitudCompra solicitud) {
        String sql = "INSERT INTO SolicitudCompra (fecha, idUsuario, idLibro, cantidad, costoUnitario, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(solicitud.getFecha().getTime()));
            ps.setInt(2, solicitud.getIdUsuario());
            ps.setInt(3, solicitud.getIdLibro());
            ps.setInt(4, solicitud.getCantidad());
            ps.setBigDecimal(5, new java.math.BigDecimal(solicitud.getCostoUnitario()));
            ps.setInt(6, solicitud.getEstado());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("SolicitudesCompra", "NuevaSolicitud",
                        "Se creo una nueva solicitud de compra para el libro ID: " + solicitud.getIdLibro());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 🟢 Actualizar solicitud existente
    public boolean actualizar(SolicitudCompra solicitud) {
        String sql = "UPDATE SolicitudCompra SET fecha = ?, idUsuario = ?, idLibro = ?, cantidad = ?, costoUnitario = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(solicitud.getFecha().getTime()));
            ps.setInt(2, solicitud.getIdUsuario());
            ps.setInt(3, solicitud.getIdLibro());
            ps.setInt(4, solicitud.getCantidad());
            ps.setBigDecimal(5, new java.math.BigDecimal(solicitud.getCostoUnitario()));
            ps.setInt(6, solicitud.getEstado());
            ps.setInt(7, solicitud.getId());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("SolicitudesCompra", "ActualizarSolicitud",
                        "Se actualizo la solicitud de compra ID: " + solicitud.getId());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 🟢 Eliminación lógica de una solicitud (estado = 0)
    public boolean eliminar(int id) {
        String sql = "UPDATE SolicitudCompra SET estado = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("SolicitudesCompra", "DesactivarSolicitud",
                        "Se desactivo la solicitud de compra ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}