package plugins;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import pizza.Message;

/**
 * Un plugin per convertire tra basi numeriche comunemente usate
 *
 * @author Nitti Gianluca
 */
public class Convert extends pizza.Trancio {

    private Map<String, Integer> baseList;

    protected final void onInitialize(){
        baseList = new HashMap<String, Integer>();
        baseList.put("bin", 2);
        baseList.put("oct", 8);
        baseList.put("dec", 10);
        baseList.put("hex", 16);
    }

    protected final void onCall(String user, String channel, Vector<String> args) {
        if(args.size() != 2){
            sendMessage(new Message(channel, user + " sono necessari due argomenti: conversione (es. hex2bin) e valore da convertire."));
            return;
        }
        Message wrongConversionMsg = new Message(channel, user + " nome della conversione errato. Deve essere formato da base sorgente + '2' + destinazione. Le basi supportate sono bin, oct, dec, hex. Ad esempio bin2hex, hex2dec, ecc.");
        if(args.get(0).length() != 7){
            sendMessage(wrongConversionMsg);
            return;
        }
        String srcBaseName = args.get(0).substring(0, 3);
        String destBaseName = args.get(0).substring(4);
        Integer srcBase = baseList.get(srcBaseName);
        Integer destBase = baseList.get(destBaseName);
        String src = args.get(1);
        if(args.get(0).charAt(3) != '2' || srcBase == null || destBase == null)
            sendMessage(wrongConversionMsg);
        else{
            try{
                String dest = Integer.toString(Integer.parseInt(src, srcBase), destBase);
                sendMessage(new Message(channel, user + " " + src + " (" + srcBaseName + ") = " + dest + " (" + destBaseName + ")"));
            }catch(NumberFormatException e){
                sendMessage(new Message(channel, user + " formato del numero errato :( :\"" + src + "\" non è un numero in base " + srcBase));
            }
        }
    }
}
