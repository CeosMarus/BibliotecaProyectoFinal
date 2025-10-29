package app.view;

import app.dao.AuditoriaDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AuditoriaForm
{
    private JComboBox cboFiltro;
    private JLabel infoNombre;
    private JTextField txtNombre;
    private JTable tblAuditoria;
    private JButton btnCargar;

    //Definimos la estructura de nuestra tabla
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha y Hora", "Usuario", "Modulo", "Accion", "Detalle"}, 0
    )
    { //Evitamos que las celdas sean editables
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // ninguna celda editable
        }
    };

    public AuditoriaForm()
    {

    }


}
