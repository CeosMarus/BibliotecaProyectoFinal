package app.dev;

import app.dao.UsuarioDAO;
import app.model.Usuario;

public class SeedUser2 {
    public static void main(String[] args) {
        try {
            UsuarioDAO dao = new UsuarioDAO();

            int estado = 1; // 1 = activo
            String password = "user123"; // contraseña del usuario

            // Crear usuario normal
            int id = dao.crearUsuario("user2", password, "Usuario 2", "USER", estado);

            System.out.println("Usuario 2 creado con ID = " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
