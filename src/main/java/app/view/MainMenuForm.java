package app.view;

import app.core.Sesion;

import javax.swing.*;
import java.awt.*;

public class MainMenuForm {
    public JPanel panelPrincipal;
    private JLabel lblUsuario;
    private JButton btnUsuario;
    private JButton btnLibros;
    private JButton btnAutores;
    private JButton btnCerrarSesion;
    private JButton btnClientes;//b
    private JButton btnCategorias;//b
    private JButton btnPrestamos;//b
    private JButton btnReservas;//b
    private JButton btnDevoluciones;//b
    private JButton btnMultas;//f
    private JButton btnCaja;//f
    private JButton btnAdquisiciones;
    private JButton btnInventario;//b
    private JButton btnReporteria;//b-f
    private JButton btnAuditoria;


    public MainMenuForm() {
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        if (Sesion.isLogged() && lblUsuario != null) {
            lblUsuario.setText("Usuario: " + Sesion.getUsuario().getNombre()
                    + " (" + Sesion.getUsuario().getRol() + ")");
        }

//Condicionales para los accesos
       if (Sesion.hasRole("Bibliotecario")) {
           btnUsuario.setEnabled(false);
           btnLibros.setEnabled(false);
           btnAutores.setEnabled(false);
           btnMultas.setEnabled(false);
           btnCaja.setEnabled(false);
           btnAdquisiciones.setEnabled(false);
           btnAuditoria.setEnabled(false);
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
        }


/*
        boolean esAdmin = Sesion.hasRole("ADMIN");
        if (btnUsuario != null) {
            btnUsuario.setEnabled(esAdmin);
            // si prefieres ocultarlo para OPERADOR:
            // btnRegistrarUsuario.setVisible(esAdmin);
        }
*/
        //----Botones para acceder a los modulos----
        //if (btnAutores != null)  btnAutores.addActionListener(e -> abrirAutores());
        //if (btnLibros  != null)  btnLibros.addActionListener(e -> abrirLibros());
        if (btnCaja  != null)  btnCaja.addActionListener(e -> abrirAperturaCaja());
        if (btnUsuario != null) btnUsuario.addActionListener(e -> abrirUsuario());
        // metódo para cerrar sesión
        if(btnCerrarSesion != null) btnCerrarSesion.addActionListener(e -> {
            Sesion.cerrarSesion();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panelPrincipal);
            parentFrame.dispose();
            new LoginForm().showForm();
        });

    }
/*
    private void abrirAutores() {
        JFrame f = new JFrame("Gestión de Autores");
        f.setContentPane(new AutorForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    private void abrirLibros() {
        JFrame f = new JFrame("Gestión de Libros");
        f.setContentPane(new LibroForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }
*/
    private void abrirAperturaCaja() {
        //if (!Sesion.hasRole("ADMIN")) return;
        JFrame f = new JFrame("Apertura de Caja");
        f.setContentPane(new AperturaCajaView());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    private void abrirUsuario() {
        //if (!Sesion.hasRole("ADMIN")) return;
        JFrame f = new JFrame("Registro de Usuario");
        f.setContentPane(new UsuariosForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    // launcher opcional
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Menú Principal – Librería");
            f.setContentPane(new MainMenuForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
        });
    }
}
