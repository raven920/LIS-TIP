package net.ideashock.lisprueba1.Actividades;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import net.ideashock.lisprueba1.Funciones;
import net.ideashock.lisprueba1.JSONParser;
import net.ideashock.lisprueba1.LectorMifareC;
import net.ideashock.lisprueba1.R;
import net.ideashock.lisprueba1.csv.ListaUsuario;
import net.ideashock.lisprueba1.csv.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActividadPrincipal extends Activity {

    private boolean sResume = true;
    private Intent mOldIntent = null;
    private EditText ced;
    private EditText nom;
    private TextView estado;
    private String url = "";
    private boolean sancion = true;
    private ListaUsuario usuarios = new ListaUsuario();
    JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_principal); //Carga la actividad

        if (savedInstanceState == null) { //Carga el fragmento
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Funciones.setNfcAdapter(NfcAdapter.getDefaultAdapter(this)); //Define el adaptador NFC en la clase funciones.

        if (Funciones.getNfcAdapter() == null) { //Si el aparato no soporta NFC...
            sResume = false; //No intentar buscar Tags NFC
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Funciones.disableNfcForegroundDispatch(this); //Cuando se pausa la aplicacion, devuelve el manejo de intentos de NFC
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actividad_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ConfiguracionActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarConfiguracion();
        Toast toast = Toast.makeText(getApplicationContext(), "Url: "+getDireccionJSON(), Toast.LENGTH_SHORT);
        toast.show();
        if (sResume) { //Si estamos volviendo...
            revisaNfc(); //Revisamos si hay TAG NFC.
        }
    }

    @Override
    public void onNewIntent(Intent intent) { //Cuando estamos en la APP y llega una nueva TAG
        int tipo = Funciones.tratarComoNuevaTag(intent, this);
        if(tipo == 0){ //Es una Mifare?
            leerID(); //Lee la info
        }

    }

    private void revisaNfc() {
        // Revisa si el hardware NFC esta habilitado.
        if (Funciones.getNfcAdapter() != null
                && !Funciones.getNfcAdapter().isEnabled()) { //No esta habilitado el lector NFC?
            return;
        } else {
            if (mOldIntent != getIntent()) { //Es un intento diferente al anterior?
                int typeCheck = Funciones.tratarComoNuevaTag(getIntent(), this);
                if(typeCheck == 0){

                    leerID();
                }
                mOldIntent = getIntent(); //Guardamos este como el intento viejo
            }
            //Habilitamos el envio de eventos NFC a la misma aplicacion, si esta en resumen.
            Funciones.enableNfcForegroundDispatch(this);
        }
    }

    public void leerID(){

        LectorMifareC lector = Funciones.checkForTagAndCreateReader(this); //Revisamos si hay tarjeta y creamos un lector

        try{
            if(lector == null){ //Si el lector no se creo...
                Toast.makeText(this,R.string.no_hay_tarjeta,Toast.LENGTH_SHORT).show();
                return;
            }
            if( lector.getSize() != MifareClassic.SIZE_4K){ //Si no es una TIP (Asumimos que todas las 4K son TIP)
                Toast.makeText(this,R.string.tarjeta_invalida,Toast.LENGTH_LONG).show();
                return;
            }
            //Leemos el primer bloque del sector 18 usando la llave definida...
            String[] stringCedula = lector.readSectorUsingKeyA(18,0, 1, Funciones.hexStringToByteArray("A0A1A2A3A4A5"));
            String[] stringNombre = lector.readSectorUsingKeyA(17,0, 3, Funciones.hexStringToByteArray("A0A1A2A3A4A5"));
            if(stringCedula == null || stringNombre == null){//Si por alguna razón alguno de nuestros vectores no existe...
                Toast.makeText(this,R.string.error_de_lectura,Toast.LENGTH_LONG).show();
                return;
            }
            //Sacamos la cedula del string
            //TODO: Revisar si funciona con todas las cedulas y nombres
            String cedulaLimpia = Funciones.sacarCedulaDeTIP(stringCedula[0]);
            String nombreLimpio = Funciones.sacarNombreLimpio(stringNombre);
            String apellidoLimpio = Funciones.sacarApellidoLimpio(stringNombre);
            if(cedulaLimpia.equals("")){//Si no hay cedula por alguna razon
                Toast.makeText(this,R.string.tarjeta_invalida,Toast.LENGTH_LONG).show();
                return;
            }
            buscaCedula(false); // Buscamos en la base de datos, asumimos que sacamos el usuario
            setCedula(cedulaLimpia);//Pone la cedula en el campo correspondiente
            setNombre(nombreLimpio);
            setApellido(apellidoLimpio);
        }catch (TagLostException exc){
        }
    }

    public void setCedula(String cedula){
        this.ced = (EditText)findViewById(R.id.editText2);
        this.ced.setText(cedula); //Escribe la cedula.
    }

    public void setNombre(String nombre){
        nom = (EditText)findViewById(R.id.editText);
        nom.setText(nombre); //Escribe la cedula.
    }

    public void setApellido(String apellido){

        nom = (EditText)findViewById(R.id.editText3);
        nom.setText(apellido); //Escribe la cedula.
    }

    public void agregarUsuario(){
        String nombreUs = ((EditText)findViewById(R.id.editText)).getText().toString();
        String apellidoUs=((EditText)findViewById(R.id.editText3)).getText().toString();
        String estadoUs =((TextView)findViewById(R.id.textView4)).getText().toString();
        usuarios.insertarUsuario(new Usuario(nombreUs,apellidoUs,getCedula(), estadoUs));

    }

    public void setEstado(int est){
        estado = (TextView)findViewById(R.id.textView4);
        String p = null;
        if(est == 2){
            p = getResources().getString(R.string.buscando);
            estado.setTextColor(Color.BLUE);
        }
        else if(est == -1) {
            p = getResources().getString(R.string.sancionado);
            estado.setTextColor(Color.RED);
        }else if(est == 0){
            p = getResources().getString(R.string.rechazado);
            estado.setTextColor(Color.RED);
        }else if(est == 1){
            p = getResources().getString(R.string.aceptado);
            estado.setTextColor(Color.GREEN);
        }
        else if(est == -2){
            estado.setTextColor(Color.parseColor("#ff9c21"));
            Toast.makeText(this,R.string.error_conexion,Toast.LENGTH_LONG).show();
            p = getResources().getString(R.string.desconectado);
        }
        estado.setText(p); //Escribe el estado;

        //Agregamos a la lista de los usuarios
        switch (est){
            case -1:
            case 0:
            case 1:
                agregarUsuario();
        }
    }

    public String getCedula(){
        ced = (EditText)findViewById(R.id.editText2);
        return ced.getText().toString();
    }

    public String getSancion() {
        if(sancion){
            return "1";
        }
        else{
            return "0";
        }

    }

    public String getDireccionJSON() {
        return url;
    }

    public void onClickBuscar (View v) {
        //Se hizo click en buscar, se asume que NO SABEMOS EL NOMBRE DEL INDIVIDUO
        buscaCedula(true);
    }

    public void buscaCedula(boolean buscarNombre){
        setEstado(2);
        new BuscaUsuario(buscarNombre).execute();
    }

    class BuscaUsuario extends AsyncTask<String, String, String> {

        private int sanc1,exito;
        private boolean datos,buscarNombre;
        private String nom, ap;


        public BuscaUsuario(boolean buscarNombre){
            this.buscarNombre = buscarNombre;
        }

         protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("numCedula", getCedula()));
            params.add(new BasicNameValuePair("sancion", getSancion()));
            params.add(new BasicNameValuePair("buscarNombre", getBuscarNombre()));
            JSONObject json = jParser.makeHttpRequest(getDireccionJSON(), "POST", params);
            if(json == null){
                datos = false;
                return null;
            }
            datos = true;

            try {

                exito = json.getInt("exito");
                if (exito == 1) {
                    sanc1 = json.getInt("sancion");
                    nom = json.getString("nombre1");
                    ap = json.getString("apellido1");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private String getBuscarNombre(){
            return buscarNombre? "1" : "0";
        }



        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    if(!datos){
                        setEstado(-2);
                        return;
                    }
                    if(exito == 1){
                        if(buscarNombre){ //sacamos el nombre de la BD
                            setNombre(nom);
                            setApellido(ap);
                        }
                        if(sanc1 == 1){
                         setEstado(-1);
                        }else{
                            setEstado(1);
                        }
                    }else{
                        if(buscarNombre){
                            setNombre("");
                            setApellido("");
                        }
                        setEstado(0);
                    }

                }
            });

        }
    }

    /**
     * Fragmento que implementa la parte grafica de la actividad.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_actividad_principal, container, false);
            return rootView;
        }
    }

    public void cargarConfiguracion(){
        SharedPreferences prefs = getSharedPreferences("Config", Context.MODE_PRIVATE);
        url = (prefs.getString("dir_serv", "http://pruebalis.esy.es/p1/p1.php"));
        sancion = (prefs.getBoolean("sanciones", true));
    }
}
