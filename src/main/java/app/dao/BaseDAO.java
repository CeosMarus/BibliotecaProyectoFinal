package app.dao;

//Clase comun para todos los dao, necesario para Auditoria
//Atajo para Auditoria Logger
import app.utility.AuditoriaLogger; //Iportacion para Auditoria, registrar acciones

//Clase abstracta ya que ofrece comportamiento compartido
//No representa una entidad concreta por si mismo
public abstract class BaseDAO
{
    protected void auditar(String modulo, String accion, String detalle) {
        try {
            AuditoriaLogger.registrar(modulo, accion, detalle);
        } catch (Exception e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
    }
}
