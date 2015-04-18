package net.ideashock.lisprueba1.csv;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by raven on 18/04/15.
 */
public class ListaUsuario {
    private HashMap<String,Usuario> usuarios;

    public ListaUsuario() {
        usuarios = new HashMap<String, Usuario>();
    }

    public void insertarUsuario(Usuario u){
        if(!usuarios.containsKey(u.getCedula())){
            usuarios.put(u.getCedula(),u);
        }
        for (Usuario i : usuarios.values()){
            Log.d("listip","Cedula: "+i.getCedula()+" Nombres: "+i.getNombres()+i.getApellidos()+" Estado: "+i.getAceptado());
        }
    }

    public LinkedList<Usuario> getLista(){
        return new LinkedList<Usuario>(usuarios.values());
    }
}
