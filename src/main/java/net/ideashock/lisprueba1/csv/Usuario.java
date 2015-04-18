package net.ideashock.lisprueba1.csv;

/**
 * Created by raven on 18/04/15.
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
