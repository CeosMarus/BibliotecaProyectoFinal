package app.dao;

import app.db.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportesDAO extends BaseDAO
{
    // Reporte catalogo filtrado por autor y titulo
    public List<Object[]> catalogoPorAutorTitulo(String autor, String titulo) throws SQLException {
        List<Object[]> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT 
            c.nombre AS categoria,
            a.nombre AS autor,
            l.titulo,
            l.anio,
            COUNT(e.id) AS ejemplares,
            SUM(CASE WHEN e.estadoCopia = 'Disponible' THEN 1 ELSE 0 END) AS disponibles
        FROM Libro l
        JOIN Categoria c ON l.idCategoria = c.id
        JOIN Autor a ON l.idAutor = a.id
        LEFT JOIN Ejemplar e ON e.idLibro = l.id
        WHERE 1=1
    """);

        // Filtros dinámicos
        if (autor != null && !"Todos".equals(autor)) {
            sql.append(" AND a.nombre = ? ");
        }
        if (titulo != null && !"Todos".equals(titulo)) {
            sql.append(" AND l.titulo = ? ");
        }

        sql.append("""
        GROUP BY c.nombre, a.nombre, l.titulo, l.anio
        ORDER BY c.nombre, a.nombre, l.titulo
    """);

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (autor != null && !"Todos".equals(autor)) ps.setString(index++, autor);
            if (titulo != null && !"Todos".equals(titulo)) ps.setString(index++, titulo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("categoria"),
                            rs.getString("autor"),
                            rs.getString("titulo"),
                            rs.getInt("anio"),
                            rs.getInt("ejemplares"),
                            rs.getInt("disponibles")
                    });
                }
            }
        }

        auditar("Reportes", "CatalogoPorAutorTitulo",
                "Se generó reporte de catálogo filtrado por autor y/o título");

        return lista;
    }
    public List<String> listarTitulosPorAutor(String autor) throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = """
        SELECT DISTINCT l.titulo
        FROM Libro l
        JOIN Autor a ON l.idAutor = a.id
        WHERE a.nombre = ?
        ORDER BY l.titulo
    """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, autor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("titulo"));
                }
            }
        }

        return lista;
    }
        //Reporte prestamo en un periodo definido
    public List<Object[]> prestamosPorPeriodo(Date inicio, Date fin) throws SQLException {
        String sql = """
            SELECT 
                FORMAT(p.fechaPrestamo, 'yyyy-MM-dd') AS fecha,
                COUNT(*) AS totalPrestamos,
                AVG(DATEDIFF(DAY, p.fechaPrestamo, p.fechaDevolucion)) AS promedioDias
            FROM Prestamo p
            WHERE p.fechaPrestamo BETWEEN ? AND ?
            GROUP BY FORMAT(p.fechaPrestamo, 'yyyy-MM-dd')
            ORDER BY fecha DESC;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inicio.getTime()));
            ps.setDate(2, new java.sql.Date(fin.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("fecha"),
                            rs.getInt("totalPrestamos"),
                            rs.getDouble("promedioDias")
                    });
                }
            }
        }

        auditar("Reportes", "PrestamosPorPeriodo",
                "Se generó reporte de préstamos entre " + inicio + " y " + fin);
        return lista;
    }

    //Reporte TOPPrestamos en un periodo y cantidad de libros definido
    public List<Object[]> topLibrosPrestados(Date inicio, Date fin, int limite) throws SQLException {
        String sql = """
            SELECT TOP (?) 
                l.titulo,
                a.nombre AS autor,
                COUNT(p.id) AS vecesPrestado
            FROM Prestamo p
            JOIN Ejemplar e ON p.idEjemplar = e.id
            JOIN Libro l ON e.idLibro = l.id
            JOIN Autor a ON l.idAutor = a.id
            WHERE p.fechaPrestamo BETWEEN ? AND ?
            GROUP BY l.titulo, a.nombre
            ORDER BY vecesPrestado DESC;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            ps.setDate(2, new java.sql.Date(inicio.getTime()));
            ps.setDate(3, new java.sql.Date(fin.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getInt("vecesPrestado")
                    });
                }
            }
        }

        auditar("Reportes", "TopLibrosPrestados",
                "Se generó reporte de top " + limite + " libros prestados");
        return lista;
    }

    //Reporte tasa de rotacion en un periodo definido
    public List<Object[]> tasaRotacionPorTitulo(Date inicio, Date fin) throws SQLException {
        String sql = """
            SELECT 
                l.titulo,
                COUNT(p.id) AS totalPrestamos,
                COUNT(DISTINCT e.id) AS ejemplares,
                CAST(COUNT(p.id) * 1.0 / NULLIF(COUNT(DISTINCT e.id), 0) AS DECIMAL(10,2)) AS tasaRotacion
            FROM Prestamo p
            JOIN Ejemplar e ON p.idEjemplar = e.id
            JOIN Libro l ON e.idLibro = l.id
            WHERE p.fechaPrestamo BETWEEN ? AND ?
            GROUP BY l.titulo
            ORDER BY tasaRotacion DESC;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inicio.getTime()));
            ps.setDate(2, new java.sql.Date(fin.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("titulo"),
                            rs.getInt("totalPrestamos"),
                            rs.getInt("ejemplares"),
                            rs.getDouble("tasaRotacion")
                    });
                }
            }
        }

        auditar("Reportes", "TasaRotacion",
                "Se generó reporte de tasa de rotación entre " + inicio + " y " + fin);
        return lista;
    }

    //Reportede multas recaudadas en un periodo definido
    public List<Object[]> multasRecaudadas(Date inicio, Date fin) throws SQLException {
        String sql = """
            SELECT 
                FORMAT(m.fechaPago, 'yyyy-MM-dd') AS fecha,
                SUM(m.monto) AS totalRecaudado
            FROM Multa m
            WHERE m.fechaPago BETWEEN ? AND ? AND m.estado = 1
            GROUP BY FORMAT(m.fechaPago, 'yyyy-MM-dd')
            ORDER BY fecha;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inicio.getTime()));
            ps.setDate(2, new java.sql.Date(fin.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("fecha"),
                            rs.getDouble("totalRecaudado")
                    });
                }
            }
        }

        auditar("Reportes", "MultasRecaudadas",
                "Se generó reporte de multas recaudadas entre " + inicio + " y " + fin);
        return lista;
    }

    //Reporte prestamo en un periodo definido
    public List<Object[]> clientesMorosos() throws SQLException {
        String sql = """
            SELECT 
                c.nombre AS cliente,
                c.nit,
                COUNT(m.id) AS multasPendientes,
                SUM(m.monto) AS deudaTotal
            FROM Cliente c
            JOIN Multa m ON m.idCliente = c.id
            WHERE m.estado = 0
            GROUP BY c.nombre, c.nit
            ORDER BY deudaTotal DESC;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                        rs.getString("cliente"),
                        rs.getString("nit"),
                        rs.getInt("multasPendientes"),
                        rs.getDouble("deudaTotal")
                });
            }
        }

        auditar("Reportes", "ClientesMorosos",
                "Se generó reporte de clientes con multas pendientes");
        return lista;
    }

    //Reporte de inventario de libros disponibles
    public List<Object[]> inventarioPorUbicacion() throws SQLException {
        String sql = """
            SELECT 
                e.sala,
                e.estante,
                e.nivel,
                COUNT(e.id) AS totalEjemplares,
                SUM(CASE WHEN e.estadoCopia = 'Disponible' THEN 1 ELSE 0 END) AS disponibles
            FROM Ejemplar e
            WHERE e.estado = 1
            GROUP BY e.sala, e.estante, e.nivel
            ORDER BY e.sala, e.estante;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                        rs.getString("sala"),
                        rs.getInt("estante"),
                        rs.getString("nivel"),
                        rs.getInt("totalEjemplares"),
                        rs.getInt("disponibles")
                });
            }
        }

        auditar("Reportes", "InventarioPorUbicacion",
                "Se generó reporte de inventario por ubicación");
        return lista;
    }

    //Reporte de adquisiciones en un periodo definido
    public List<Object[]> adquisiciones(Date inicio, Date fin) throws SQLException {
        String sql = """
            SELECT 
                l.titulo,
                e.fechaAlta,
                COUNT(e.id) AS ejemplaresAdquiridos
            FROM Ejemplar e
            JOIN Libro l ON e.idLibro = l.id
            WHERE e.fechaAlta BETWEEN ? AND ?
            GROUP BY l.titulo, e.fechaAlta
            ORDER BY e.fechaAlta DESC;
        """;

        List<Object[]> lista = new ArrayList<>();

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inicio.getTime()));
            ps.setDate(2, new java.sql.Date(fin.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getString("titulo"),
                            rs.getDate("fechaAlta"),
                            rs.getInt("ejemplaresAdquiridos")
                    });
                }
            }
        }

        auditar("Reportes", "Adquisiciones",
                "Se generó reporte de adquisiciones entre " + inicio + " y " + fin);
        return lista;
    }

    //Apoyo
    public List<String> listarAutores() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT nombre FROM Autor ORDER BY nombre";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        }

        return lista;
    }
    public List<String> listarTitulos() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT titulo FROM Libro ORDER BY titulo";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("titulo"));
        }
        return lista;
    }
}
