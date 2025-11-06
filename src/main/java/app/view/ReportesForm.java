package app.view;

import app.dao.ReportesDAO;
import app.core.Sesion;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
    private JButton btnSalir;

    private final ReportesDAO reportesDAO = new ReportesDAO();
    private DefaultTableModel model = null;

    public ReportesForm() {
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
            }
        });
        btnGenerar.addActionListener(e -> generarReporte());
        btnExcel.addActionListener(e -> exportarExcel());
        btnPDF.addActionListener(e -> exportarPDF());
        btnLimpiar.addActionListener(e -> limpiarTabla());
        btnSalir.addActionListener(e -> onSalir());

    }

    private Date parseFecha(String texto) throws ParseException {
        if (texto == null || texto.isBlank()) return new Date(); // si no se ingresó, usar fecha actual
        return new SimpleDateFormat("yyyy-MM-dd").parse(texto);
    }


    private void generarReporte() {
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

    private DefaultTableModel actualizarModelo() {
        String modelo = (String) cboCategoria.getSelectedItem();
        DefaultTableModel model = null;
        switch (modelo) {
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
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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

    private void ocultarTodo() {
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

    private void cargarModulos() {
        if (Sesion.hasRole("ADMIN")) {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Catalogo");
            cboCategoria.addItem("Prestamos");
            cboCategoria.addItem("Top_Libros");
            cboCategoria.addItem("Tasa_de_Rotacion");
            cboCategoria.addItem("Multas_Recaudadas");
            cboCategoria.addItem("Clientes_Morosos");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        } else if (Sesion.hasRole("Financiero")) {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Multas_Recaudadas");
            cboCategoria.addItem("Clientes_Morosos");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        } else if (Sesion.hasRole("Bibliotecario")) {
            cboCategoria.removeAllItems();
            cboCategoria.addItem("Catalogo");
            cboCategoria.addItem("Prestamos");
            cboCategoria.addItem("Top_Libros");
            cboCategoria.addItem("Tasa_de_Rotacion");
            cboCategoria.addItem("Inventario");
            cboCategoria.addItem("Adquisiciones");
        } else {
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

    private void exportarExcel() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar reporte como Excel");
            chooser.setSelectedFile(new File("reporte.xlsx"));
            int userSelection = chooser.showSaveDialog(panelPrincipal);

            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Reporte");

            DefaultTableModel model = (DefaultTableModel) tblReporte.getModel();

            // Encabezados
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < model.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(model.getColumnName(i));
            }

            // Datos
            for (int i = 0; i < model.getRowCount(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    row.createCell(j).setCellValue(value != null ? value.toString() : "");
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < model.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
            workbook.close();

            reportesDAO.registrarAuditoria("ExportarExcel", "Se exportó un reporte a Excel.");

            JOptionPane.showMessageDialog(null, "Reporte exportado exitosamente a Excel.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al exportar a Excel: " + e.getMessage());
        }
    }

    private void exportarPDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar reporte como PDF");
            chooser.setSelectedFile(new File("reporte.pdf"));
            int userSelection = chooser.showSaveDialog(panelPrincipal);

            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            DefaultTableModel model = (DefaultTableModel) tblReporte.getModel();

            document.add(new Paragraph("Reporte generado - " + cboCategoria.getSelectedItem()));
            document.add(new Paragraph("Fecha: " + new Date().toString()));
            document.add(new Paragraph(" ")); // Espacio

            PdfPTable table = new PdfPTable(model.getColumnCount());

            // Encabezados
            for (int i = 0; i < model.getColumnCount(); i++) {
                table.addCell(new Phrase(model.getColumnName(i)));
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

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(16, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Arista Pro Light", -1, 20, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Reportes \uD83D\uDCDC");
        panelPrincipal.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Categoria:");
        panelPrincipal.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboCategoria = new JComboBox();
        panelPrincipal.add(cboCategoria, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoFecha1 = new JLabel();
        infoFecha1.setText("Fecha Inicio: ");
        panelPrincipal.add(infoFecha1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFechaInicio = new JTextField();
        panelPrincipal.add(txtFechaInicio, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        infoFecha2 = new JLabel();
        infoFecha2.setText("Fecha Fin:");
        panelPrincipal.add(infoFecha2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFechaFin = new JTextField();
        txtFechaFin.setText("");
        panelPrincipal.add(txtFechaFin, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        infoUser = new JLabel();
        infoUser.setText("Usuario:");
        panelPrincipal.add(infoUser, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboUsuario = new JComboBox();
        panelPrincipal.add(cboUsuario, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoRol = new JLabel();
        infoRol.setText("Rol:");
        panelPrincipal.add(infoRol, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboRol = new JComboBox();
        panelPrincipal.add(cboRol, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoTitulo = new JLabel();
        infoTitulo.setText("Titulo: ");
        panelPrincipal.add(infoTitulo, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboTitulo = new JComboBox();
        panelPrincipal.add(cboTitulo, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnGenerar = new JButton();
        btnGenerar.setText("Generar");
        panelPrincipal.add(btnGenerar, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnExcel = new JButton();
        btnExcel.setText("Excel");
        panelPrincipal.add(btnExcel, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPDF = new JButton();
        btnPDF.setText("PDF");
        panelPrincipal.add(btnPDF, new com.intellij.uiDesigner.core.GridConstraints(11, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPrincipal.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(13, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tblReporte = new JTable();
        scrollPane1.setViewportView(tblReporte);
        final JLabel label3 = new JLabel();
        label3.setText("Vista Previa");
        panelPrincipal.add(label3, new com.intellij.uiDesigner.core.GridConstraints(12, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoExportar = new JLabel();
        infoExportar.setText("Exportar:");
        panelPrincipal.add(infoExportar, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoFechas = new JLabel();
        infoFechas.setText("Formato de fechas yyyy-MM-dd");
        panelPrincipal.add(infoFechas, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar Tabla");
        panelPrincipal.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoAutor = new JLabel();
        infoAutor.setText("Autor: ");
        panelPrincipal.add(infoAutor, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboAutor = new JComboBox();
        panelPrincipal.add(cboAutor, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        panelPrincipal.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(15, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(14, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelPrincipal;
    }
}
