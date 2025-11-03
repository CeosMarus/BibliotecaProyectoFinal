package app.view;

import app.dao.ReportesDAO;
import app.core.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ReportesForm {
    private JComboBox<String> cboCategoria;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JComboBox<String> cboUsuario;
    private JComboBox<String> cboRol;
    private JComboBox<String> cboTitulo;
    private JComboBox<String> cboAutor;
    private JButton btnGenerar;
    private JButton btnExcel;
    private JButton btnPDF;
    private JTable tblReporte;
    private JLabel infoFecha1;
    private JLabel infoFecha2;
    private JLabel infoUser;
    private JLabel infoRol;
    private JLabel infoTitulo;
    private JLabel infoAutor;
    private JLabel infoExportar;
    public JPanel panelPrincipal;
    private JLabel infoFechas;
    private JButton btnLimpiar;

    private final ReportesDAO reportesDAO = new ReportesDAO();
    private DefaultTableModel model = null;

    public ReportesForm()
    {
        //Dimensiones de la ventana
        panelPrincipal.setPreferredSize(new Dimension(950, 600));
        //Cargamos los datos de cboModulos
        ocultarTodo();
        cargarModulos();

        //Listeners
        cboCategoria.addActionListener(e -> {
            if ("Catalogo".equals(cboCategoria.getSelectedItem())) {
                cargarFiltrosCatalogo();
            }
            model = actualizarModelo();
            tblReporte.setModel(model);
            //Los botones se muestran pero deshabilitados
            btnExcel.setEnabled(false);
            btnPDF.setEnabled(false);
        });
        cboAutor.addActionListener(e -> {
            String autorSeleccionado = (String) cboAutor.getSelectedItem();
            if (autorSeleccionado != null && !"Todos".equals(autorSeleccionado)) {
                cargarTitulosPorAutor(autorSeleccionado);
            } else {
                cargarTodosLosTitulos();
            }});
        btnGenerar.addActionListener(e -> generarReporte());
        btnExcel.addActionListener(e -> exportarExcel());
        btnPDF.addActionListener(e -> exportarPDF());
        btnLimpiar.addActionListener(e -> limpiarTabla());

    }

    private Date parseFecha(String texto) throws ParseException {
        if (texto == null || texto.isBlank()) return new Date(); // si no se ingresó, usar fecha actual
        return new SimpleDateFormat("yyyy-MM-dd").parse(texto);
    }


    private void generarReporte()
    {
        model.setRowCount(0);
        String categoria = (String) cboCategoria.getSelectedItem();
        if (categoria == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una categoría de reporte.");
            return;
        }

        try {
            limpiarTabla();
            Date inicioUtil = parseFecha(txtFechaInicio.getText());
            Date finUtil = parseFecha(txtFechaFin.getText());

            // Validacion de fechas si el reporte lo requiere
            if (categoria.matches("Prestamos|Top_Libros|Tasa_de_Rotacion|Multas_Recaudadas|Adquisiciones")) {
                if (inicioUtil == null || finUtil == null) {
                    JOptionPane.showMessageDialog(null, "Debe ingresar ambas fechas (inicio y fin).");
                    return;
                }
                if (finUtil.before(inicioUtil)) {
                    JOptionPane.showMessageDialog(null, "La fecha final debe ser posterior a la fecha de inicio.");
                    return;
                }
            }

            //Conveertir a sql.date
            java.sql.Date inicio = new java.sql.Date(inicioUtil.getTime());
            java.sql.Date fin = new java.sql.Date(finUtil.getTime());

            List<Object[]> datos = null;

            switch (categoria) {
                case "Catalogo": {
                    String autorSel = (String) cboAutor.getSelectedItem();
                    String tituloSel = (String) cboTitulo.getSelectedItem();
                    datos = reportesDAO.catalogoPorAutorTitulo(autorSel, tituloSel);
                    break;
                }
                case "Prestamos": {
                    datos = reportesDAO.prestamosPorPeriodo(inicio, fin);
                    break;
                }
                case "Top_Libros": {
                    datos = reportesDAO.topLibrosPrestados(inicio, fin, 10);
                    break;
                }
                case "Tasa_de_Rotacion": {
                    datos = reportesDAO.tasaRotacionPorTitulo(inicio, fin);
                    break;
                }
                case "Multas_Recaudadas": {
                    datos = reportesDAO.multasRecaudadas(inicio, fin);
                    break;
                }
                case "Clientes_Morosos": {
                    datos = reportesDAO.clientesMorosos();
                    break;
                }
                case "Inventario": {
                    datos = reportesDAO.inventarioPorUbicacion();
                    break;
                }
                case "Adquisiciones": {
                    datos = reportesDAO.adquisiciones(inicio, fin);
                    break;
                }
                default:
                    JOptionPane.showMessageDialog(null, "Seleccione una categoría válida.");
                    return;
            }

            // Mostrar los datos
            if (datos != null) {
                for (Object[] fila : datos) {
                    model.addRow(fila);
                }
                JOptionPane.showMessageDialog(null, "Reporte generado correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No se encontraron datos para el reporte.");
            }

            // Habilitar exportación
            btnExcel.setEnabled(true);
            btnPDF.setEnabled(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error SQL al generar el reporte: " + e.getMessage());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Use yyyy-MM-dd");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
        }
    }

    private DefaultTableModel actualizarModelo()
    {
        String modelo = (String) cboCategoria.getSelectedItem();
        DefaultTableModel model = null;
        switch(modelo)
        {
            case "Catalogo" -> {
            mostrarSolo("titulo", "autor", "exportar");
            model = crearModelo(new String[]{"Categoría", "Autor", "Título", "Año", "# Ejemplares", "Disponibles"});
            }
            case "Prestamos" -> {
                mostrarSolo("fechas", "exportar");
                model = crearModelo(new String[]{"Fecha", "Total Préstamos", "Promedio Días"});
            }
            case "Top_Libros" -> {
                mostrarSolo("fechas", "exportar");
                model = crearModelo(new String[]{"Título", "Autor", "Veces Prestado"});
            }
            case "Tasa_de_Rotacion" -> {
                mostrarSolo("fechas", "exportar");
                model = crearModelo(new String[]{"Título", "Total Préstamos", "Ejemplares", "Tasa Rotación"});
            }
            case "Multas_Recaudadas" -> {
                mostrarSolo("fechas", "exportar");
                model = crearModelo(new String[]{"Fecha", "Total Recaudado"});
            }
            case "Clientes_Morosos" -> {
                mostrarSolo("exportar");
                model = crearModelo(new String[]{"Cliente", "NIT", "Multas Pendientes", "Deuda Total"});
            }
            case "Inventario" -> {
                mostrarSolo("exportar");
                model = crearModelo(new String[]{"Sala", "Estante", "Nivel", "Total Ejemplares", "Disponibles"});
            }
            case "Adquisiciones" -> {
                mostrarSolo("fechas", "exportar");
                model = crearModelo(new String[]{"Título", "Fecha Alta", "Ejemplares Adquiridos"});
            }
            default -> JOptionPane.showMessageDialog(null, "Seleccione una categoría válida.");
        }
        return model;
    }
    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    private void limpiarTabla() {
        if (model != null) {
            model.setRowCount(0);
        }
    }

    private void mostrarSolo(String... visibles) {
        ocultarTodo();
        btnGenerar.setVisible(true);
        for (String v : visibles) {
            switch (v) {
                case "fechas" -> {
                    txtFechaInicio.setVisible(true);
                    txtFechaFin.setVisible(true);
                    infoFecha1.setVisible(true);
                    infoFecha2.setVisible(true);
                    infoFechas.setVisible(true);
                }
                case "titulo" -> {
                    cboTitulo.setVisible(true);
                    infoTitulo.setVisible(true);
                }
                case "autor" -> {
                    cboAutor.setVisible(true);
                    infoAutor.setVisible(true);
                }
                case "exportar" -> {
                    btnExcel.setVisible(true);
                    btnExcel.setEnabled(false); // inicialmente deshabilitado
                    btnPDF.setVisible(true);
                    btnPDF.setEnabled(false);   // inicialmente deshabilitado
                    infoExportar.setVisible(true);
                }
            }
        }
    }
    // Cargar todos los autores y títulos (cuando se selecciona "Catálogo")
    private void cargarFiltrosCatalogo() {
        try {
            cboAutor.removeAllItems();
            cboAutor.addItem("Todos");
            for (String autor : reportesDAO.listarAutores()) {
                cboAutor.addItem(autor);
            }

            cargarTodosLosTitulos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar filtros de catalogo: " + e.getMessage());
        }
    }

    // Cargar todos los títulos (sin filtrar)
    private void cargarTodosLosTitulos() {
        try {
            cboTitulo.removeAllItems();
            cboTitulo.addItem("Todos");
            for (String titulo : reportesDAO.listarTitulos()) {
                cboTitulo.addItem(titulo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar titulos: " + e.getMessage());
        }
    }

    // Cargar títulos solo del autor seleccionado
    private void cargarTitulosPorAutor(String autor) {
        try {
            cboTitulo.removeAllItems();
            cboTitulo.addItem("Todos");
            for (String titulo : reportesDAO.listarTitulosPorAutor(autor)) {
                cboTitulo.addItem(titulo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar titulos del autor: " + e.getMessage());
        }
    }

    private void ocultarTodo()
    {
        infoFechas.setVisible(false);
        txtFechaInicio.setVisible(false);
        txtFechaFin.setVisible(false);
        cboUsuario.setVisible(false);
        cboRol.setVisible(false);
        cboTitulo.setVisible(false);
        cboAutor.setVisible(false);
        btnGenerar.setVisible(false);
        btnExcel.setVisible(false);
        btnPDF.setVisible(false);
        infoFecha1.setVisible(false);
        infoFecha2.setVisible(false);
        infoUser.setVisible(false);
        infoRol.setVisible(false);
        infoTitulo.setVisible(false);
        infoAutor.setVisible(false);
        infoExportar.setVisible(false);
    }
    private void cargarModulos()
    {
        if(Sesion.hasRole("ADMIN"))
        {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Catalogo");
            cboCategoria.addItem("Prestamos");
            cboCategoria.addItem("Top_Libros");
            cboCategoria.addItem("Tasa_de_Rotacion");
            cboCategoria.addItem("Multas_Recaudadas");
            cboCategoria.addItem("Clientes_Morosos");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        }
        else if (Sesion.hasRole("Financiero"))
        {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Multas_Recaudadas");
            cboCategoria.addItem("Clientes_Morosos");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        }
        else if (Sesion.hasRole("Bibliotecario"))
        {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Catalogo");
            cboCategoria.addItem("Prestamos");
            cboCategoria.addItem("Top_Libros");
            cboCategoria.addItem("Tasa_de_Rotacion");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        }
        else
        {
            JOptionPane.showMessageDialog(null, "No hay usuario valido en sesion.");
            //Se activa para fines de pruebas
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Catalogo");
            cboCategoria.addItem("Prestamos");
            cboCategoria.addItem("Top_Libros");
            cboCategoria.addItem("Tasa_de_Rotacion");
            cboCategoria.addItem("Multas_Recaudadas");
            cboCategoria.addItem("Clientes_Morosos");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
            return;
        }

    }

    private void exportarExcel()
    {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar reporte como Excel");
            chooser.setSelectedFile(new java.io.File("reporte.xlsx"));
            int userSelection = chooser.showSaveDialog(panelPrincipal);

            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            java.io.File file = chooser.getSelectedFile();

            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Reporte");

            DefaultTableModel model = (DefaultTableModel) tblReporte.getModel();

            // Encabezados
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < model.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(model.getColumnName(i));
            }

            // Datos
            for (int i = 0; i < model.getRowCount(); i++) {
                org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(i + 1);
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    row.createCell(j).setCellValue(value != null ? value.toString() : "");
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < model.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                workbook.write(out);
            }
            workbook.close();

            reportesDAO.registrarAuditoria( "ExportarExcel", "Se exportó un reporte a Excel.");

            JOptionPane.showMessageDialog(null, "Reporte exportado exitosamente a Excel.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al exportar a Excel: " + e.getMessage());
        }
    }

    private void exportarPDF()
    {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar reporte como PDF");
            chooser.setSelectedFile(new java.io.File("reporte.pdf"));
            int userSelection = chooser.showSaveDialog(panelPrincipal);

            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            java.io.File file = chooser.getSelectedFile();

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            DefaultTableModel model = (DefaultTableModel) tblReporte.getModel();

            document.add(new com.itextpdf.text.Paragraph("Reporte generado - " + cboCategoria.getSelectedItem()));
            document.add(new com.itextpdf.text.Paragraph("Fecha: " + new java.util.Date().toString()));
            document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(model.getColumnCount());

            // Encabezados
            for (int i = 0; i < model.getColumnCount(); i++) {
                table.addCell(new com.itextpdf.text.Phrase(model.getColumnName(i)));
            }

            // Datos
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    table.addCell(value != null ? value.toString() : "");
                }
            }

            document.add(table);
            document.close();
            reportesDAO.registrarAuditoria("ExportarPDF", "Se exportó un reporte a PDF.");
            JOptionPane.showMessageDialog(null, "Reporte exportado exitosamente a PDF.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al exportar a PDF: " + e.getMessage());
        }
    }

    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Reportes");
            f.setContentPane(new ReportesForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
