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
            btnDevoluciones.setEnabled(false);
            btnReporteria.setEnabled(true);
        }
        else if (Sesion.hasRole("Financiero")) {
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
        }
        else if (!Sesion.hasRole("ADMIN")) {
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
        if (btnCaja  != null)  btnCaja.addActionListener(e -> abrirAperturaCaja());
        if (btnUsuario != null) btnUsuario.addActionListener(e -> abrirUsuario());
        if (btnInventario != null) btnInventario.addActionListener(e -> abrirInventario());
        if (btnClientes != null) btnClientes.addActionListener(e -> abrirClientes());
        if( btnDevolucion != null)btnDevolucion.addActionListener(e -> abrirDevolucion());
        if (btnAuditoria != null) btnAuditoria.addActionListener(e -> abrirAuditoria());
        if (btnReservas != null) btnReservas.addActionListener(e -> abrirReservas());
        if (btnReporteria != null) btnReporteria.addActionListener(e -> abrirReportes());


        // 🔐 Cerrar sesión
        if(btnCerrarSesion != null) btnCerrarSesion.addActionListener(e -> {
            Sesion.cerrarSesion();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panelPrincipal);
            parentFrame.dispose();
            new LoginForm().showForm();
        });
    }

    private void abrirMenuCliente() {
       MenuClienteForm menuClienteForm = new MenuClienteForm();
       menuClienteForm.setVisible(true);
    }
    private void abrirDevolucion() {
        DevolucionForm devolucionForm = new DevolucionForm();
        devolucionForm.setVisible(true);
    }

    private void abrirAperturaCaja() {
        JFrame f = new JFrame("Apertura de Caja");
        f.setContentPane(new AperturaCajaView());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    private void abrirUsuario() {
        JFrame f = new JFrame("Registro de Usuario");
        f.setContentPane(new UsuariosForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    private void abrirInventario() {
        JFrame f = new JFrame("Gestión de Inventario Físico");
        InventarioForm inventarioForm = new InventarioForm();
        inventarioForm.setVisible(true);
    }

    private void abrirClientes() {
        Usuario u = new Usuario(1, "admin", "Administrador", "ADMIN", 1);
        JFrame f = new JFrame("Gestión de Clientes");
        f.setContentPane(new ClienteForm(u).panelPrincipal);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void abrirAuditoria() {
        JFrame f = new JFrame("Auditoria");
        f.setContentPane(new AuditoriaForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }
    private void abrirReservas() {
        JFrame f = new JFrame("Reservas");
        f.setContentPane(new ReservasForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    private void abrirReportes() {
        JFrame f = new JFrame("Reportes");
        f.setContentPane(new ReportesForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    // main tests
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Menú Principal – Librería");
            f.setContentPane(new MainMenuForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
        });
    }
}