# 📚 SGIB - Sistema de Gestión Integral de Biblioteca

Proyecto final de biblioteca desarrollado en **Java 21**, empaquetado como un **fat JAR** y un **ejecutable `.exe`** para Windows.  
El sistema integra autenticación facial, gestión de usuarios, generación de reportes en PDF y exportación de datos a Excel.  
Está diseñado para ofrecer una solución completa de administración de bibliotecas con seguridad, portabilidad y facilidad de uso.

---

## 🚀 Tecnologías utilizadas

- **Java 21** – Lenguaje principal
- **Maven** – Gestión de dependencias y empaquetado
- **Swing / IntelliJ Forms** – Interfaz gráfica
- **SQL Server JDBC Driver** – Conexión a base de datos en la nube
- **BCrypt** – Hashing seguro de contraseñas
- **iText PDF** – Generación de reportes en PDF
- **Apache POI** – Exportación e importación de datos en Excel
- **OpenCV + JavaCV** – Reconocimiento facial (LBPH)
- **forms_rt (IntelliJ)** – Soporte para formularios `.form`

---

## 📦 Distribución

El proyecto se comparte en dos formatos:

1. **Fat JAR**  
   - Contiene todas las dependencias integradas.  
   - Se ejecuta con:
     ```bash
     java -jar ProyectoFinalBiblioteca-1.0-SNAPSHOT.jar
     ```

2. **Ejecutable `.exe` (Windows)**  
   - Generado con **Launch4j**.  
   - Basta con hacer doble clic en `BibliotecaSGBI.exe`.

---

## 🖥️ Funcionalidades principales

- **Login seguro** con usuario/contraseña (hashing con BCrypt).
- **Login facial biométrico** usando OpenCV (LBPH).
- **Gestión de usuarios**: creación, edición y eliminación.
- **Generación de reportes PDF** con iText.
- **Exportación de datos a Excel** con Apache POI.
- **Interfaz gráfica amigable** construida con Swing y formularios de IntelliJ.
- **Conexión a base de datos en la nube** para centralizar la información y permitir acceso remoto.

---

## 🔐 Política de privacidad facial

El sistema implementa un enfoque ético y seguro para el manejo de datos biométricos:

- Se almacenan únicamente **representaciones matemáticas (plantillas LBPH)** derivadas de los rostros capturados.  
- **Nunca** se guardan fotografías crudas ni imágenes completas.  
- Las plantillas se almacenan de forma **cifrada o como datos binarios**.  
- Se utilizan **exclusivamente para autenticación biométrica** dentro del sistema.  
- **No se comparten con terceros** ni se emplean para otros fines distintos al inicio de sesión facial autorizado.

---

## ⚙️ Requisitos del sistema

- **Windows 10/11** (para el `.exe`)  
- **Java 21 JDK**  
- **Conexión a Internet** (para acceder a la base de datos en la nube)  

---

## 📌 Notas de empaquetado

- El proyecto utiliza **maven-shade-plugin** para generar un JAR ejecutable con todas las dependencias incluidas.  
- Se excluyen firmas digitales de librerías externas para evitar errores de seguridad en tiempo de ejecución.  
- El `.exe` fue creado con **Launch4j** para mayor portabilidad.  

