package app.dev;

import app.dao.UsuarioDAO;

public class SeedAdmin {
    public static void main(String[] args) {
        try {
            UsuarioDAO dao = new UsuarioDAO();

            // Datos del administrador
            String username = "admin";
            String password = "admin123"; // Contraseña en texto plano, se guardará como hash
            String nombre = "Administrador";
            String rol = "ADMIN";
            int estado = 1; // Activo

            // Crear el usuario admin
            int id = dao.crearUsuario(username, password, nombre, rol, estado);

            if (id != -1) {
                System.out.println("Admin creado correctamente con id=" + id);
            } else {
                System.out.println("Error al crear el admin.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
