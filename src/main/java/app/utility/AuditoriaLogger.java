package app.utility;

import app.dao.AuditoriaDAO;
import app.model.Auditoria;
import app.core.Sesion;

import java.sql.SQLException;
import java.util.Date;

public class AuditoriaLogger
{
    public static void registrar(String modulo, String accion, String detalle) {
        try {
            // Verifica si hay un usuario logueado
            if (!Sesion.isLogged()) {
                System.err.println("⚠️ No se puede registrar auditoría: no hay usuario en sesión.");
                return;
            }

            // Obtiene el usuario actual
            int idUsuario = Sesion.getUsuario().getId();

            // Crea el objeto Auditoria
            Auditoria a = new Auditoria(new Date(), idUsuario, modulo, accion, detalle);

            // Inserta en base de datos
            new AuditoriaDAO().insertar(a);

            System.out.println("🧾 Auditoría registrada → [" + modulo + "] " + accion + ": " + detalle);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Error al registrar auditoría: " + e.getMessage());
        }
    }
}
