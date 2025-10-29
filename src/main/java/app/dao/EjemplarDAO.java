package app.dao;

import app.db.Conexion;
import app.model.Ejemplar;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EjemplarDAO extends BaseDAO {

    // 🔹 INSERTAR nuevo ejemplar
    public int insertar(Ejemplar e) throws SQLException {
        String sql = """
            INSERT INTO Ejemplar 
            (idLibro, codigoInventario, sala, estante, nivel, estadoCopia, fechaAlta, fechaBaja, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, e.getIdLibro());
            ps.setString(2, e.getCodigoInventario());
            ps.setString(3, e.getSala());
            ps.setString(4, e.getEstante());
            ps.setString(5, e.getNivel());
            ps.setString(6, e.getEstadoCopia());
            ps.setDate(7, e.getFechaAlta());
            ps.setDate(8, e.getFechaBaja());
            ps.setInt(9, e.getEstado()); // <-- cambio a int

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setId(id);
                    //Registar la accion en Auditoria
                    auditar("Catalogo-Ejemplar", "NuevoEjemplar", "Se creo el ejemplar" + e.getCodigoInventario());
                    return id;
                }
            }
        }
        return -1;
    }

    // 🔹 ACTUALIZAR datos del ejemplar
    public boolean actualizar(Ejemplar e) throws SQLException {
        String sql = """
            UPDATE Ejemplar 
            SET idLibro=?, codigoInventario=?, sala=?, estante=?, nivel=?, 
                estadoCopia=?, fechaAlta=?, fechaBaja=?, estado=? 
            WHERE id=?
        """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, e.getIdLibro());
            ps.setString(2, e.getCodigoInventario());
            ps.setString(3, e.getSala());
            ps.setString(4, e.getEstante());
            ps.setString(5, e.getNivel());
            ps.setString(6, e.getEstadoCopia());
            ps.setDate(7, e.getFechaAlta());
            ps.setDate(8, e.getFechaBaja());
            ps.setInt(9, e.getEstado()); // <-- cambio a int
            ps.setInt(10, e.getId());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Catalogo-Ejemplar", "ActualizarEjemplar", "Se actualizo el ejemplar" + e.getCodigoInventario());
                return  true;
            }
            return false;
        }
    }

    // 🔹 ELIMINACIÓN LÓGICA (cambia estado a 0)
    public boolean eliminar(int id) throws SQLException {
        String sql = "UPDATE Ejemplar SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Catalogo-Ejemplar", "DesactivarEjemplar", "Se deshabilito el ejemplar ID:" + id);
                return true;
            }
            return false;
        }
    }

    // 🔹 REACTIVAR ejemplar (estado = 1)
    public boolean reactivar(int id) throws SQLException {
        String sql = "UPDATE Ejemplar SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                //Registar la accion en Auditoria
                auditar("Catalogo-Ejemplar", "ActivarEjemplar", "Se habilito el ejemplar ID:" + id);
                return true;
            }
            return false;
        }
    }

    // 🔹 LISTAR todos los ejemplares
    public List<Ejemplar> listar() throws SQLException {
        String sql = """
            SELECT id, idLibro, codigoInventario, sala, estante, nivel, 
                   estadoCopia, fechaAlta, fechaBaja, estado
            FROM Ejemplar
            ORDER BY id DESC
        """;

        List<Ejemplar> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapEjemplar(rs));
            }
        }
        //Registar la accion en Auditoria
        auditar("Catalogo-Ejemplar", "ListarEjemplar", "Se listaron todos los ejemplares");
        return lista;
    }

    // 🔹 BUSCAR POR ID
    public Ejemplar buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Ejemplar WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Registar la accion en Auditoria
                    auditar("Catalogo-Ejemplar", "ListarEjemplar", "Se listo el ejemplar con ID: " + id);
                    return mapEjemplar(rs);
                }
            }
        }
        return null;
    }

    // 🔹 BUSCAR POR CÓDIGO DE INVENTARIO
    public List<Ejemplar> buscarPorCodigoInventario(String codigoInventario) throws SQLException {
        String sql = """
            SELECT * FROM Ejemplar 
            WHERE codigoInventario LIKE ? 
            ORDER BY id DESC
        """;

        List<Ejemplar> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + codigoInventario + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapEjemplar(rs));
                }
            }
        }
        //Registar la accion en Auditoria
        auditar("Catalogo-Ejemplar", "ListarEjemplar", "Se busco el ejemplar: " + codigoInventario);
        return lista;
    }

    // 🔹 LISTAR CON JOIN A LIBRO (titulo del libro)
    public List<Ejemplar> listarConLibro() throws SQLException {
        String sql = """
            SELECT e.id, e.idLibro, e.codigoInventario, e.sala, e.estante, e.nivel,
                   e.estadoCopia, e.fechaAlta, e.fechaBaja, e.estado, l.titulo AS libroTitulo
            FROM Ejemplar e
            JOIN Libro l ON e.idLibro = l.id
            ORDER BY e.id DESC
        """;

        List<Ejemplar> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ejemplar ej = mapEjemplar(rs);
                ej.setLibroNombre(rs.getString("libroTitulo")); // usamos titulo
                lista.add(ej);
            }
        }
        //Registar la accion en Auditoria
        auditar("Catalogo-Ejemplar", "ListarEjemplar", "Se listaron los ejemplares con titulos");
        return lista;
    }

    // 🔹 Helper para mapear datos del ResultSet → Objeto Ejemplar
    private Ejemplar mapEjemplar(ResultSet rs) throws SQLException {
        Ejemplar e = new Ejemplar();
        e.setId(rs.getInt("id"));
        e.setIdLibro(rs.getInt("idLibro"));
        e.setCodigoInventario(rs.getString("codigoInventario"));
        e.setSala(rs.getString("sala"));
        e.setEstante(rs.getString("estante"));
        e.setNivel(rs.getString("nivel"));
        e.setEstadoCopia(rs.getString("estadoCopia"));
        e.setFechaAlta(rs.getDate("fechaAlta"));
        e.setFechaBaja(rs.getDate("fechaBaja"));
        e.setEstado(rs.getInt("estado")); // <-- cambio a int
        return e;
    }
}
