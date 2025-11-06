package app.view;

import app.core.Sesion;
import app.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class MainMenuForm {
    public JPanel panelPrincipal;
    private JLabel lblUsuario;
    private JButton btnUsuario;
    private JButton btnLibros;
    private JButton btnAutores;
    private JButton btnCerrarSesion;
    private JButton btnClientes;
    private JButton btnCategorias;
    private JButton btnPrestamos;
    private JButton btnReservas;
    private JButton btnDevoluciones;
    private JButton btnMultas;
    private JButton btnCaja;
    private JButton btnAdquisiciones;
    private JButton btnInventario;
    private JButton btnReporteria;
    private JButton btnAuditoria;
    private JButton btnMenuCliente; // ✅ Ya está en tu diseño
    private JButton btnDevolucion;
    private JButton btnEjemplares;
    private JButton btnSolicitudCompra;

    public MainMenuForm() {
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        if (Sesion.isLogged() && lblUsuario != null) {
            lblUsuario.setText("Usuario: " + Sesion.getUsuario().getNombre()
                    + " (" + Sesion.getUsuario().getRol() + ")");
        }

        // ===== CONFIGURACIÓN DE ROLES =====
        if (Sesion.hasRole("Bibliotecario")) {
            btnUsuario.setEnabled(false);
            btnLibros.setEnabled(false);
            btnAutores.setEnabled(false);
            btnMultas.setEnabled(false);
            btnCaja.setEnabled(false);
            btnAdquisiciones.setEnabled(false);
            btnAuditoria.setEnabled(false);
            btnReporteria.setEnabled(true);
            btnEjemplares.setEnabled(true);
        } else if (Sesion.hasRole("Financiero")) {
            btnUsuario.setEnabled(false);
            btnLibros.setEnabled(false);
            btnAutores.setEnabled(false);
            btnClientes.setEnabled(false);
            btnCategorias.setEnabled(false);
            btnPrestamos.setEnabled(false);
            btnReservas.setEnabled(false);
            btnDevoluciones.setEnabled(false);
            btnAdquisiciones.setEnabled(false);
            btnInventario.setEnabled(false);
            btnAuditoria.setEnabled(false);
            btnReporteria.setEnabled(true);
            btnEjemplares.setEnabled(false);
        } else if (!Sesion.hasRole("ADMIN")) {
            btnUsuario.setEnabled(false);
            btnLibros.setEnabled(false);
            btnAutores.setEnabled(false);
            btnClientes.setEnabled(false);
            btnCategorias.setEnabled(false);
            btnPrestamos.setEnabled(false);
            btnReservas.setEnabled(false);
            btnDevoluciones.setEnabled(false);
            btnMultas.setEnabled(false);
            btnCaja.setEnabled(false);
            btnAdquisiciones.setEnabled(false);
            btnInventario.setEnabled(false);
            btnReporteria.setEnabled(false);
            btnAuditoria.setEnabled(false);
            btnEjemplares.setEnabled(false);
            btnSolicitudCompra.setEnabled(false);
        }

        // ✅ Habilitar botón portal cliente SOLO si es CLIENTE o ADMIN
        if (Sesion.hasRole("CLIENTE") || Sesion.hasRole("ADMIN")) {
            if (btnMenuCliente != null) {
                btnMenuCliente.setEnabled(true);
                btnMenuCliente.addActionListener(e -> abrirMenuCliente());
            }
        } else {
            if (btnMenuCliente != null) {
                btnMenuCliente.setEnabled(false);
            }
        }

        // ---- Botones funcionales ----
        //Bloque 1
        if (btnAutores != null) btnAutores.addActionListener(e -> abrirAutores());
        if (btnCategorias != null) btnCategorias.addActionListener(e -> abrirCategorias());
        if (btnLibros != null) btnLibros.addActionListener(e -> abrirLibros());

        if (btnPrestamos != null) btnPrestamos.addActionListener(e -> abrirPrestamos());
        if (btnReservas != null) btnReservas.addActionListener(e -> abrirReservas());
        if (btnDevoluciones != null) btnDevoluciones.addActionListener(e -> abrirDevoluciones());

        if (btnEjemplares != null) btnEjemplares.addActionListener(e -> abrirEjemplar());
        if (btnInventario != null) btnInventario.addActionListener(e -> abrirInventario());
        if (btnReporteria != null) btnReporteria.addActionListener(e -> abrirReportes());
        if (btnAuditoria != null) btnAuditoria.addActionListener(e -> abrirAuditoria());

        if (btnSolicitudCompra != null) btnSolicitudCompra.addActionListener(e -> abrirSolicitudCompra());
        if (btnAdquisiciones != null) btnAdquisiciones.addActionListener(e -> abrirAdquisiciones());
        if (btnCaja != null) btnCaja.addActionListener(e -> abrirAperturaCaja());
        btnMultas.addActionListener(e -> abrirMultas());

        if (btnClientes != null) btnClientes.addActionListener(e -> abrirClientes());
        if (btnUsuario != null) btnUsuario.addActionListener(e -> abrirUsuario());


        // 🔐 Cerrar sesión
        if (btnCerrarSesion != null) btnCerrarSesion.addActionListener(e -> {
            Sesion.cerrarSesion();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panelPrincipal);
            parentFrame.dispose();
            new LoginForm().showForm();
        });
    }


    // Views
    private void abrirAutores() {
        JFrame f = new JFrame("Gestión de Autores");
        f.setContentPane(new AutorForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirCategorias() {
        JFrame f = new JFrame("Gestión de Categorías");
        f.setContentPane(new CategoriaForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirLibros() {
        JFrame f = new JFrame("Gestión de Libros");
        f.setContentPane(new LibroForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirPrestamos() {
        JFrame f = new JFrame("Gestión de Prestamos");
        f.setContentPane(new PrestamosForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirReservas() {
        JFrame f = new JFrame("Gestión de Reservas");
        f.setContentPane(new ReservasForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirDevoluciones() {
        JFrame f = new JFrame("Gestión de Devoluciones");
        f.setContentPane(new DevolucionForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirEjemplar() {
        JFrame f = new JFrame("Gestión de Ejemplares");
        f.setContentPane(new EjemplarForm().mainPanel);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirInventario() {
        JFrame f = new JFrame("Gestión de Inventario Físico");
        InventarioForm inventarioForm = new InventarioForm();
        inventarioForm.setVisible(true);
    }

    private void abrirReportes() {
        JFrame f = new JFrame("Gestión de Reportes");
        f.setContentPane(new ReportesForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirAuditoria() {
        JFrame f = new JFrame("Gestión de Auditoria");
        f.setContentPane(new AuditoriaForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirSolicitudCompra() {
        JFrame f = new JFrame("Gestión de Solicitudes (compras)");
        f.setContentPane(new SolicitudCompraForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirAdquisiciones() {
        JFrame f = new JFrame("Gestión de Adquisiciones (compras)");
        f.setContentPane(new CompraLibroForm().panel);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirAperturaCaja() {
        JFrame f = new JFrame("Apertura de Caja");
        f.setContentPane(new AperturaCajaView());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirMultas() {
        JFrame f = new JFrame("Registro de Multas");
        f.setContentPane(new MultaView());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirClientes() {
        Usuario u = new Usuario(1, "admin", "Administrador", "ADMIN", 1);
        JFrame f = new JFrame("Gestión de Clientes");
        f.setContentPane(new ClienteForm(u).panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirUsuario() {
        JFrame f = new JFrame("Registro de Usuario");
        f.setContentPane(new UsuariosForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirMenuCliente() {
        MenuClienteForm menuClienteForm = new MenuClienteForm();
        menuClienteForm.setVisible(true);
    }

    // main tests
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Menú Principal – Librería");
            f.setContentPane(new MainMenuForm().panelPrincipal);
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
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(17, 6, new Insets(0, 0, 0, 0), -1, -1));
        lblUsuario = new JLabel();
        lblUsuario.setText("Mensaje");
        panelPrincipal.add(lblUsuario, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCerrarSesion = new JButton();
        btnCerrarSesion.setText("Cerrar Sesion");
        panelPrincipal.add(btnCerrarSesion, new com.intellij.uiDesigner.core.GridConstraints(16, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnPrestamos = new JButton();
        btnPrestamos.setText("Prestamos");
        panelPrincipal.add(btnPrestamos, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnClientes = new JButton();
        btnClientes.setText("Clientes");
        panelPrincipal.add(btnClientes, new com.intellij.uiDesigner.core.GridConstraints(12, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnUsuario = new JButton();
        btnUsuario.setText("Usuarios");
        panelPrincipal.add(btnUsuario, new com.intellij.uiDesigner.core.GridConstraints(12, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAuditoria = new JButton();
        btnAuditoria.setText("Seguridad y Auditoria");
        panelPrincipal.add(btnAuditoria, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnMenuCliente = new JButton();
        btnMenuCliente.setText("Menu Cliente");
        panelPrincipal.add(btnMenuCliente, new com.intellij.uiDesigner.core.GridConstraints(15, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnReservas = new JButton();
        btnReservas.setText("Reservas");
        panelPrincipal.add(btnReservas, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnDevoluciones = new JButton();
        btnDevoluciones.setText("Devoluciones");
        panelPrincipal.add(btnDevoluciones, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEjemplares = new JButton();
        btnEjemplares.setText("Ejemplares");
        panelPrincipal.add(btnEjemplares, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAutores = new JButton();
        btnAutores.setText("Autores");
        panelPrincipal.add(btnAutores, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCategorias = new JButton();
        btnCategorias.setText("Categorias");
        panelPrincipal.add(btnCategorias, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLibros = new JButton();
        btnLibros.setText("Libros");
        panelPrincipal.add(btnLibros, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnInventario = new JButton();
        btnInventario.setText("Inventario");
        panelPrincipal.add(btnInventario, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnReporteria = new JButton();
        btnReporteria.setText("Reportería");
        panelPrincipal.add(btnReporteria, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(13, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer9 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer9, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer10 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer10, new com.intellij.uiDesigner.core.GridConstraints(14, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnMultas = new JButton();
        btnMultas.setText("Multas");
        panelPrincipal.add(btnMultas, new com.intellij.uiDesigner.core.GridConstraints(10, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCaja = new JButton();
        btnCaja.setText("Caja");
        panelPrincipal.add(btnCaja, new com.intellij.uiDesigner.core.GridConstraints(9, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAdquisiciones = new JButton();
        btnAdquisiciones.setText("Adquisiciones");
        panelPrincipal.add(btnAdquisiciones, new com.intellij.uiDesigner.core.GridConstraints(8, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSolicitudCompra = new JButton();
        btnSolicitudCompra.setText("Solicitud Compra");
        panelPrincipal.add(btnSolicitudCompra, new com.intellij.uiDesigner.core.GridConstraints(7, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelPrincipal;
    }
}