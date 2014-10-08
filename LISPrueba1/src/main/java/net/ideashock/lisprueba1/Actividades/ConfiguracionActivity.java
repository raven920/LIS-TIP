package net.ideashock.lisprueba1.Actividades;

import android.app.Activity;;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import net.ideashock.lisprueba1.R;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.TextView;

public class ConfiguracionActivity extends Activity implements View.OnClickListener{

    private TextView dir;
    private CheckBox san;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_configuracion);
        dir = (TextView)findViewById(R.id.dirText);
        san = (CheckBox)findViewById(R.id.checkSancion);

    }

    @Override
    protected void onResume(){
        super.onResume();
        cargarConfiguracion();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.bGuardar){
            guardarConfiguracion();
            finish();
        }
        else if(view.getId()==R.id.bCancelar){
            finish();
        }
    }

    public void cargarConfiguracion()
    {
        SharedPreferences prefs = getSharedPreferences("Config", Context.MODE_PRIVATE);
        dir.setText(prefs.getString("dir_serv", "http://droidsaurio.esy.es/p1/p1.php"));
        san.setChecked(prefs.getBoolean("sanciones", true));
    }

    public void guardarConfiguracion()
    {
        SharedPreferences prefs = getSharedPreferences("Config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if(dir.getText().toString().equals("")){
            editor.putString("dir_serv", "http://droidsaurio.esy.es/p1/p1.php");
        }else{
            editor.putString("dir_serv", dir.getText().toString());
        }
        editor.putBoolean("sanciones", san.isChecked());
        editor.commit();
    }
}
