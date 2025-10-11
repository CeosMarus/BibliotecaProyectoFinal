package app.dao;

import app.db.Conexion;
import app.model.Categoria;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // ➕ INSERTAR NUEVA CATEGORÍA
    public boolean agregarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre de la categoría no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO Categoria (nombre, estado) VALUES (?, 1)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        categoria.setId(rs.getInt(1));
                    }
                }
                JOptionPane.showMessageDialog(null, "Categoría agregada exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar la categoría: " + e.getMessage());
        }
        return false;
    }

    // ✏️ ACTUALIZAR CATEGORÍA
    public boolean actualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre de la categoría no puede estar vacío.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE Categoria SET nombre=? WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Categoría actualizada exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar la categoría: " + e.getMessage());
        }
        return false;
    }

    // ❌ ELIMINACIÓN LÓGICA (estado = 0)
    public boolean eliminarCategoria(int id) {
        int confirm = JOptionPane.showConfirmDialog(null, "¿Estás seguro de desactivar esta categoría?", "Confirmar desactivación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        String sql = "UPDATE Categoria SET estado = 0 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Categoría desactivada correctamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al desactivar la categoría: " + e.getMessage());
        }
        return false;
    }

    // 🔁 REACTIVAR CATEGORÍA (estado = 1)
    public boolean activarCategoria(int id) {
        String sql = "UPDATE Categoria SET estado = 1 WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Categoría activada correctamente.");
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al activar la categoría: " + e.getMessage());
        }
        return false;
    }

    // 📋 LISTAR TODAS LAS CATEGORÍAS
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, estado FROM Categoria ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCategoria(rs));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar las categorías: " + e.getMessage());
        }
        return lista;
    }

    // ✅ LISTAR SOLO ACTIVAS
    public List<Categoria> listarActivas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, estado FROM Categoria WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCategoria(rs));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar las categorías activas: " + e.getMessage());
        }
        return lista;
    }

    // 🔍 BUSCAR POR ID
    public Categoria buscarPorId(int id) {
        String sql = "SELECT id, nombre, estado FROM Categoria WHERE id=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategoria(rs);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar la categoría: " + e.getMessage());
        }
        return null;
    }

    // 🔎 BUSCAR POR NOMBRE (coincidencia parcial)
    public List<Categoria> buscarPorNombre(String nombre) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, estado FROM Categoria WHERE nombre LIKE ? ORDER BY nombre ASC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapCategoria(rs));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar categorías: " + e.getMessage());
        }
        return lista;
    }

    // 🧩 MAPEADOR: convierte ResultSet en objeto Categoria
    private Categoria mapCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setEstado(rs.getInt("estado"));
        return categoria;
    }
}
