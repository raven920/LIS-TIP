package net.ideashock.lisprueba1.csv;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.ideashock.lisprueba1.Actividades.ActividadPrincipal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Clase Convertidor:
 *
 * Esta clase se encarga de guardar en un archivo CSV los usuarios que han ingresado al LIS.
 *
 * @author Christian Delany
 */
public class Convertidor {
    private List<Usuario> usuarios;
    private String nomArchivo;
    private ActividadPrincipal ap;

    public Convertidor(List<Usuario> usuarios, String nomArchivo, ActividadPrincipal ap) {
        this.usuarios = usuarios;
        this.nomArchivo = nomArchivo;
        this.ap = ap;
    }

    public void crearCSV(){
        if(!isExternalStorageWritable()){
            Toast.makeText(ap.getApplicationContext(),"ERROR: No se puede escribir en memoria externa",Toast.LENGTH_LONG).show();
            return;
        }
        String nomFinal = nombreCorrecto();
        File dirFinal = crearDir();
        boolean resultado = escribirCSV(dirFinal,nomFinal);
        if(resultado){
            Toast.makeText(ap.getApplicationContext(),"Archivo creado: "+dirFinal.getAbsolutePath()+"/"+nomFinal,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(ap.getApplicationContext(),"ERROR INESPERADO: Archivo no creado",Toast.LENGTH_LONG).show();
        }
    }

    private String nombreCorrecto(){
        return nomArchivo.toLowerCase().endsWith(".csv") ? nomArchivo: nomArchivo+".csv";

    }

    private boolean escribirCSV(File dir, String nombreArchivo){
        String datos = parseDatos(usuarios);
        File file = new File(dir, nombreArchivo);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.print(datos);
            pw.flush();
            pw.close();
            f.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String parseDatos(List<Usuario> usr){
        StringBuilder sb = new StringBuilder("Cedula,Nombres,Apellidos,Estado");
        for(Usuario u : usr){
            sb.append("\n");
            sb.append(u.getCedula());
            sb.append(",");
            sb.append(u.getNombres());
            sb.append(",");
            sb.append(u.getApellidos());
            sb.append(",");
            sb.append(u.getAceptado());
        }

        return sb.toString();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File crearDir() {
        // Get the directory for the user's public pictures directory.
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/LIS-TIP");
        if (!dir.mkdirs()) {
            Log.d("listip", "Directorio no creado");
        }else{
            Log.d("listip", "Directorio creado");
        }
        return dir;
    }
}
