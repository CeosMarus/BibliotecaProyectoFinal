package app.dao;

//Clase comun para todos los dao, necesario para Auditoria
//Atajo para Auditoria Logger
import app.utility.AuditoriaLogger; //Iportacion para Auditoria, registrar acciones

//Clase abstracta ya que ofrece comportamiento compartido
//No representa una entidad concreta por si mismo
public abstract class BaseDAO
{
    //metodo accesible
    protected void auditar(String modulo, String accion, String detalle) {
        AuditoriaLogger.registrar(modulo, accion, detalle);
    }
}
