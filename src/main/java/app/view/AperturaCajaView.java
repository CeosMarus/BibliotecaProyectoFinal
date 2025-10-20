package app.view;

import app.dao.AperturaCajaDAO;
import app.core.Sesion;
import app.model.AperturaCaja;
import app.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vista (como JPanel) para registrar la apertura de caja.
 * Debe ser colocado dentro de un JFrame o JDialog.
 */
public class AperturaCajaView extends JPanel {

    // Componentes
    private JLabel lblUsuarioActual;
    private JTextField txtSaldoInicial;
    private JButton btnAbrirCaja;

    // DAOs y Modelos
    private final AperturaCajaDAO cajaDAO;
    private final Usuario usuarioLogueado;

    /**
     * Constructor de la vista.
     */
    public AperturaCajaView() {
        cajaDAO = new AperturaCajaDAO();

        // 1. Verificar la sesión antes de construir la vista
        if (!Sesion.isLogged()) {
            JOptionPane.showMessageDialog(this, // Usamos 'this' (el panel) como padre
                    "Debe iniciar sesión para realizar la apertura de caja.",
                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            usuarioLogueado = null;
            return;
        }

        usuarioLogueado = Sesion.getUsuario();
        setPreferredSize(new Dimension(500, 250));
        setLayout(new BorderLayout(10, 5));

        // 3. Inicialización de componentes y lógica
        initComponents();
        btnAbrirCaja.addActionListener(e -> abrirCaja());

        // **SE ELIMINA:** setVisible(true)
    }

    // --------------------------------------------------------------------------
    // INICIALIZACIÓN DE COMPONENTES
    // --------------------------------------------------------------------------
    private void initComponents() {
        // --- Panel Superior (Título) ---
        JLabel lblTitulo = new JLabel("Registrar Apertura de Caja", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        add(lblTitulo, BorderLayout.NORTH);

        // --- Panel Central (Formulario) ---
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Campo 1: Usuario (tomado de Sesion)
        JLabel lblCajero = new JLabel("Cajero Actual:");
        lblUsuarioActual = new JLabel(usuarioLogueado.getNombre() + " (" + usuarioLogueado.getRol() + ")");
        lblUsuarioActual.setFont(new Font("Arial", Font.BOLD, 12));

        // Campo 2: Saldo Inicial
        JLabel lblSaldoInicial = new JLabel("Saldo Inicial:");
        txtSaldoInicial = new JTextField("0.00");

        panelFormulario.add(lblCajero);
        panelFormulario.add(lblUsuarioActual);
        panelFormulario.add(lblSaldoInicial);
        panelFormulario.add(txtSaldoInicial);

        // Espaciado
        panelFormulario.add(new JLabel(""));
        panelFormulario.add(new JLabel(""));

        add(panelFormulario, BorderLayout.CENTER);

        // --- Panel Inferior (Botón) ---
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAbrirCaja = new JButton("Abrir Caja y Empezar Turno");
        btnAbrirCaja.setFont(new Font("Arial", Font.BOLD, 14));
        panelBoton.add(btnAbrirCaja);
        add(panelBoton, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------------------------
    // LÓGICA DE APERTURA
    // --------------------------------------------------------------------------
    /**
     * Lógica para abrir la caja, llama al DAO y utiliza el usuario de la sesión.
     */
    private void abrirCaja() {
        try {
            // Obtener ID del usuario de la sesión
            int idUsuario = usuarioLogueado.getId();

            // Validación de formato y creación de BigDecimal
            String saldoText = txtSaldoInicial.getText().trim().replace(',', '.');
            if(saldoText.isEmpty()){
                JOptionPane.showMessageDialog(this,"Por favor ingresar un saldo inicial","Saldo inicial vacio",JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal saldoInicial = new BigDecimal(saldoText);
            if(saldoInicial.compareTo(BigDecimal.ZERO)<0){
                JOptionPane.showMessageDialog(this,"El saldo inicial no puede ser negativo","Saldo inicial Negativo",JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Crear el objeto AperturaCaja
            AperturaCaja nuevaCaja = new AperturaCaja(
                    idUsuario,
                    new Date(),
                    new Date(),
                    saldoInicial,
                    1 // Estado Activa
            );

            // Llamar al DAO para insertar
            int idGenerado = cajaDAO.abrirCaja(nuevaCaja);

            if (idGenerado > 0) {
                // Éxito. En lugar de this.dispose(), cerramos la ventana contenedora.
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (topFrame != null) {
                    topFrame.dispose();
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un valor numérico válido para el saldo inicial (ej. 100.00).",
                    "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(AperturaCajaView.class.getName()).log(Level.SEVERE, "Error al abrir la caja:", ex);
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la caja: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(AperturaCajaView.class.getName()).log(Level.SEVERE, "Error inesperado:", ex);
            JOptionPane.showMessageDialog(this,
                    "Ocurrió un error inesperado: " + ex.getMessage(),
                    "Error General", JOptionPane.ERROR_MESSAGE);
        }
    }
}