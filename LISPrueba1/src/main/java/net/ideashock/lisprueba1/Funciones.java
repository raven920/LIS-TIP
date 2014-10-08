package net.ideashock.lisprueba1;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Esta clase contiene funciones genericas para ser usadas con tags MIFARE
 * y el lector de NFC que tienen algunos telefonos android.
 *
 * Ciertas partes del codigo pertenencen a G3rhard Kl0stermeier y estan cubiertas por
 * GNU/GPL v3.
 *
 * @author Christian Delany
 */
public class Funciones {

    private static NfcAdapter tNfcAdapter; //Aqui guardamos el adaptador NFC
    private static Tag tTag = null; //Aqui guardamos la Tag NFC
    private static byte[] mUID = null; //Aqui guardamos el ID de la tag NFC.


    public static void setNfcAdapter(NfcAdapter anfcAdapter) {
        tNfcAdapter = anfcAdapter;
    }

    public static NfcAdapter getNfcAdapter() {
        return tNfcAdapter;
    }

    public static void setTag(Tag tag) {
        tTag = tag;
        mUID = tag.getId();
    }

    public static int tratarComoNuevaTag(Intent intent, Context context) {
        // Check if Intent has a NFC Tag.
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {//Si el intento es de una tarjeta NFC...
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            setTag(tag); //Guardamos la Tag NFC
            if (Arrays.asList(tag.getTechList()).contains(
                    MifareClassic.class.getName())) {
                // Es una Mifare Classic..
                return 0;
            }
        }
        return -1; //No es mifare classic o no es el intento que esperabamos.
    }

    /*
    * Le decimos a android que vamos a manejar todos los intentos de NFC
    * desde esta actividad si esta al frente.
     */
    public static void enableNfcForegroundDispatch(Activity targetActivity) {
        if (tNfcAdapter != null && tNfcAdapter.isEnabled()) {

            Intent intent = new Intent(targetActivity,
                    targetActivity.getClass()).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    targetActivity, 0, intent, 0);
            tNfcAdapter.enableForegroundDispatch(
                    targetActivity, pendingIntent, null, new String[][] {
                    new String[] { NfcA.class.getName() } });
        }
    }

    /*
    * Le decimos a Android que vuelva a manejar los eventos NFC
    * a su manera.
     */
    public static void disableNfcForegroundDispatch(Activity targetActivity) {
        if (tNfcAdapter != null && tNfcAdapter.isEnabled()) {
            tNfcAdapter.disableForegroundDispatch(targetActivity);
        }
    }

    /*
    *Revisa si hay una etiqueta NFC y en caso de ser afirmativo
    * Retorna un objeto que nos permite realizar operaciones
    * con la tarjeta.
     */
    public static LectorMifareC checkForTagAndCreateReader(Context context) {
        LectorMifareC reader = null;
        boolean tagLost = false;
        // Revisa si tenemos la TAG (Tomada previamente en otra funcion)
        if (tTag != null && (reader = LectorMifareC.get(tTag)) != null) {
            try {
                reader.connect(); //Se conecta?
            } catch (Exception e) {
                tagLost = true;
            }
            if (!tagLost && !reader.isConnected()) { //Si no esta conectado...
                reader.close(); //Cierra el lector
                tagLost = true;
            } else {
                return reader; //Retorne lector
            }
        } else {
            tagLost = true;
        }
        //No esta la TIP
        return null;
    }

    /*
    * Convierte de byte[] (hex) a String (ASCII)
     */
    public static String hex2ASCIIString(byte[] hex){
        //Cambiamos cualquier caracter extrano por puntos
        for(int i = 0; i < hex.length; i++) {
            if (hex[i] < (byte)0x20 || hex[i] == (byte)0x7F) {
                hex[i] = (byte)0x2E;
            }
        }
        // Hex a ASCII
        try {
            return new String(hex, "US-ASCII"); //Retorna en ASCII
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    /*
    * Toma un String en el formato de las TIP que contenga la cedula
    * y devuelve un String con la cedula (Limpio)
     */
    public static String sacarCedulaDeTIP(String ent){
        int i = 5; //Usualmente inicia en la pos 5
        while(i< ent.length()){
            if(!Character.isDigit(ent.charAt(i))){ //Mientras sea numero....
                return ent.substring(5,i);
            }
            i++;
        }
        return ent.substring(5); //Llegamos al final, es toda?
    }

    /*
    * Convierte un String de HEX a byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {
        }
        return data;
    }

    /*
    * Convierte de byte[] a String (HEX)
     */
    public static String byte2HexString(byte[] bytes) {
        String ret = "";
        if (bytes != null) {
            for (Byte b : bytes) {
                ret += String.format("%02X", b.intValue() & 0xFF);
            }
        }
        return ret;
    }

    public static String sacarNombreLimpio(String[] stringNombre) { //Saca el nombre segun está en el formato de la tip.
        return formateaNombreOApellido(stringNombre[0].substring(1, stringNombre[0].length()));
    }


    public static String sacarApellidoLimpio(String[] stringNombre){
        return formateaNombreOApellido(stringNombre[1].substring(7,stringNombre[1].length())+stringNombre[2]);

    }

    private static String formateaNombreOApellido(String str){
        StringBuilder formatoLimpio = new StringBuilder();
        boolean espacio = false;
        for (char c: str.toCharArray()){
            if(c == ' '){
                if(espacio){
                    break;
                }
                else{
                    espacio = true;
                }
            }
            formatoLimpio.append(c);
        }
        return formatoLimpio.toString();
    }

}
