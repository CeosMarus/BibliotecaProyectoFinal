package app.dao;

import app.db.Conexion;
import app.model.Prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class PrestamoDAO extends BaseDAO {

    // INSERTAR (guardando fechaDevolucion cuando venga desde el formulario)
    public int insertar(Prestamo p) throws SQLException {
        if (p == null) throw new SQLException("Prestamo no puede ser nulo.");
        if (isEjemplarPrestado(p.getIdEjemplar())) {
            throw new SQLException("El ejemplar ya está prestado actualmente.");
        }

        String sql = "INSERT INTO Prestamo " +
                "(idCliente, idEjemplar, fechaPrestamo, fechaVencimiento, fechaDevolucion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getIdCliente());
            ps.setInt(2, p.getIdEjemplar());
            ps.setDate(3, new java.sql.Date(p.getFechaPrestamo().getTime()));
            ps.setDate(4, new java.sql.Date(p.getFechaVencimiento().getTime()));

            // Si el formulario te envía la "fechaDevolucion prevista", la guardas;
            // si viniera null, guardas NULL en la BD.
            if (p.getFechaDevolucion() != null) {
                ps.setDate(5, new java.sql.Date(p.getFechaDevolucion().getTime()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setInt(6, 1); // estado = Activo al insertar

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setId(id);
                    // Auditoría
                    auditar("Prestamos", "PrestamoEjemplar",
                            "Se realizó el préstamo del ejemplar ID: " + p.getIdEjemplar() +
                                    ", al cliente " + p.getIdCliente());
                    return id;
                }
            }
        }
        return -1;
    }
    // DEVOLVER (Actualización de estado y fechaDevolucion)
    public boolean devolver(int id) throws SQLException {
        String sql = "UPDATE Prestamo SET estado=0, fechaDevolucion=GETDATE() WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("Prestamos", "Devolucion",
                        "Se registro la devolucion del prestamo ID: " + id);
                return true;
            }
            return false;
        }
    }

    //ELIMINACIÓN LÓGICA
    public boolean eliminar(int id) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Desea desactivar este préstamo?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        String sql = "UPDATE Prestamo SET estado=0 WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registo en auditoria
                auditar("Prestamos", "DesactivarPrestamo",
                        "Se descativo el prestamo ID: " + id);
                return true;
            }
            return false;
        }
    }

    //BUSCAR POR ID
    public Prestamo buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Prestamo WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                {
                    //Registo en auditoria
                    auditar("Prestamos", "ListarPrestamos",
                            "Se listo el prestamo con el ID: " + id);
                    return mapPrestamo(rs);
                }
            }
        }
        return null;
    }

    // LISTAR TODOS
    public List<Prestamo> listar() throws SQLException {
        String sql = "SELECT * FROM Prestamo ORDER BY id DESC";
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapPrestamo(rs));
        }
        //Registo en auditoria
        auditar("Prestamos", "ListarPrestamos",
                "Se listo todos los prestamos registrados");
        return lista;
    }

    // Muestra solo clientes activos
    public List<Prestamo> listarActivos() throws SQLException {
        String sql = "SELECT * FROM Prestamo WHERE estado=1 ORDER BY id DESC";
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapPrestamo(rs));
        }
        //Registo en auditoria
        auditar("Prestamos", "ListarPrestamos",
                "Se listaron los prestamos activos");
        return lista;
    }

    // listar por clientes
    public List<Prestamo> listarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT * FROM Prestamo WHERE idCliente=? AND estado=1 ORDER BY id DESC";
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapPrestamo(rs));
            }
        }
        //Registo en auditoria
        auditar("Prestamos", "ListarPrestamos",
                "Se listaron los prestamos del cliente con ID: " + idCliente);
        return lista;
    }

    // listar ejemplares
    public List<Prestamo> listarPorEjemplar(int idEjemplar) throws SQLException {
        String sql = "SELECT * FROM Prestamo WHERE idEjemplar=? AND estado=1 ORDER BY id DESC";
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapPrestamo(rs));
            }
        }
        //Registo en auditoria
        auditar("Prestamos", "ListarPrestamos",
                "Se listaron los prestamos del ejemplar con ID: " + idEjemplar);
        return lista;
    }

    //Comprobar si el libro ejemplar esta prestado
    public boolean isEjemplarPrestado(int idEjemplar) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Prestamo WHERE idEjemplar=? AND estado=1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                {
                    //Registo en auditoria
                    auditar("Prestamos", "VerificarDisponibilidad",
                            "Se verifico si el ejmplar con ID: " + idEjemplar + "Cuenta con disponibilidad");
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }
    
    // Listar préstamos del cliente con datos descriptivos
    public List<Object[]> listarPrestamosClienteDetalle(int idCliente) throws SQLException {
        String sql = "SELECT P.id, L.titulo, A.nombre AS autor, E.codigoInventario, " +
                "P.fechaPrestamo, P.fechaVencimiento, P.fechaDevolucion, " +
                "CASE WHEN P.estado = 1 THEN 'Activo' ELSE 'Devuelto' END AS estado " +
                "FROM Prestamo P " +
                "JOIN Ejemplar E ON P.idEjemplar = E.id " +
                "JOIN Libro L ON E.idLibro = L.id " +
                "JOIN Autor A ON L.idAutor = A.id " +
                "WHERE P.idCliente = ? " +
                "ORDER BY P.fechaPrestamo DESC";

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getString("codigoInventario"),
                            rs.getDate("fechaPrestamo"),
                            rs.getDate("fechaVencimiento"),
                            rs.getDate("fechaDevolucion"),
                            rs.getString("estado")
                    });
                }
            }
        }
        return lista;
    }

    // Informacion de las multas
    private Prestamo mapPrestamo(ResultSet rs) throws SQLException {
        return new Prestamo(
                rs.getInt("id"),
                rs.getInt("idCliente"),
                rs.getInt("idEjemplar"),
                rs.getDate("fechaPrestamo"),
                rs.getDate("fechaVencimiento"),
                rs.getDate("fechaDevolucion"),
                rs.getInt("estado")
        );
    }
}
