package net.ideashock.lisprueba1.Mifare;

import java.io.IOException;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;

/**
 * Esta clase contiene funciones de lectura de bloques para ser usadas con tags MIFARE.
 *
 * Ciertas partes del codigo pertenencen a G3rhard Kl0stermeier y estan cubiertas por
 * GNU/GPL v3.
 *
 * @author Christian Delany
 */
public class LectorMifareC {

    private final Tag tTag; //Tag que sera leida
    private final MifareClassic tMFC; //Instancia de MC de la tag.

    private LectorMifareC(Tag tag) {
        tTag = tag;
        tMFC = MifareClassic.get(tTag);
    }

    /*
    * Retorna un LectorMifareC Asociado a la tag que ingresa.
    * Si la tag no es MifareClassic retorna NULL
     */
    public static LectorMifareC get(Tag tag) {
        LectorMifareC mcr = null;
        if (tag != null) {
            mcr = new LectorMifareC(tag);
            if (mcr.isMifareClassic() == false) {
                return null;
            }
        }
        return mcr;
    }

    /*
    * Esta funcion lee un bloque de la Mifare Classic
    *
    * Primero se autentica en el sector para poder leerlo,
     * luego procede a leer el bloque.
     */
    public String[] readSectorUsingKeyA(int sectorIndex,int blockIndexRelativeToSector,int blockAmount ,byte[] key) throws TagLostException{
        boolean auth = autenticarConLlaveA(sectorIndex, key); //Se autentifica en el sector, con la llave A ingresada.
        if(auth){ //Si la autenticacion fue exitosa....
            int firstBlock = tMFC.sectorToBlock(sectorIndex)+blockIndexRelativeToSector; //Busca el bloque que queremos leer.
            /*if(blockAmount < 1){ // Revisamos que el usuario quiera leer bloques
                return "";
            }
            if(tMFC.getSize() == MifareClassic.SIZE_4K && sectorIndex > 31){
                if(blockAmount > 16) //Cada sector a partir del 32 tiene 16 bloques
                    blockAmount = 16;

            }else if(blockAmount > 4){ //Las tarjetas nomales solo tienen 4 bloques por sector.
                blockAmount = 4;
            }*/

            int lastBlock = firstBlock+blockAmount;

            try {
                String[] salida = new String[blockAmount];
                for(int i = firstBlock; i < lastBlock;i++){ //recorremos los bloques que necesitamos
                    salida[i-firstBlock] = Funciones.hex2ASCIIString(tMFC.readBlock(i));  //Aquí se guarda cada sector del bloque que se lee
                }
                return salida; //Lee el bloque (luego de haberse autenticado) y convertimos el arreglo de bytes a string
            } catch (TagLostException e) {
                throw e;
            } catch (IOException e) {

                if (!tMFC.isConnected()) {
                    throw new TagLostException(
                            "Se quito la etiqueta mientras se leia(...)");
                }
            }
        }
        return null;
    }

    private boolean autenticarConLlaveA(int sectorIndex, byte[] key) {
        try {
            // Nos autenticamos con la llave A
            return tMFC.authenticateSectorWithKeyA(sectorIndex, key);
        } catch (IOException e) {
        }
        return false;
    }


    public boolean isMifareClassic() { //Es una MFC?
        if (tMFC == null) {
            return false;
        }
        return true;
    }

    /**
     * Devuelve el tamano de la tarjeta en bits.
     * (e.g. Mifare Classic 1k = 1024)
     */
    public int getSize() {
        return tMFC.getSize();
    }

    /**
     * Revisa si el lector esta conectado.
     */
    public boolean isConnected() {
        return tMFC.isConnected();
    }

    /**
     * Conecta el lector.
     */
    public void connect() {
        try {
            tMFC.connect();
        } catch (IOException e) {
        }
    }

    /**
     * Cierra la conexion entre el lector y la tarjeta.
     */
    public void close() {
        try {
            tMFC.close();
        }
        catch (IOException e) {
        }
    }
}