package net.ideashock.lisprueba1.csv;

/**
 * Clase Usuario:
 *
 * Esta clase se encarga de almacenar en la RAM un usuario determinado.
 *
 * @author Christian Delany
 */
public class Usuario {
    private String nombres;
    private String apellidos;
    private String cedula;
    private String aceptado;

    public Usuario(String nombres, String apellidos, String cedula, String aceptado) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cedula = cedula;
        this.aceptado = aceptado;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCedula() {
        return cedula;
    }

    public String getAceptado() {
        return aceptado;
    }
}
