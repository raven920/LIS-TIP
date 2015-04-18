package net.ideashock.lisprueba1.csv;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Clase ListaUsuario:
 *
 * Esta clase almacena los usuarios que han ingresado al LIS en un HashMap, el cual es entregado a
 * las demas clases como una lista.
 *
 * @author Christian Delany
 */
public class ListaUsuario {
    private HashMap<String,Usuario> usuarios;

    public ListaUsuario() {
        usuarios = new HashMap<String, Usuario>();
    }

    public void insertarUsuario(Usuario u){
        usuarios.put(u.getCedula(),u);
    }

    public LinkedList<Usuario> getLista(){
        return new LinkedList<Usuario>(usuarios.values());
    }
}
