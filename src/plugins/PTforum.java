package plugins;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import pizza.Message;

/**
 * Plugin per notificare nuovi messaggi sul forum di pierotofy.it
 *
 * @author Nitti Gianluca
 */
public final class PTforum extends pizza.Trancio{
  String chan = "#pierotofy.it";
  String apiEndpoint = "http://www.pierotofy.it/pages/extras/forum/api/last_topics.php";
  int interval = 60000; //TODO: rendere configurabili

  protected final void onPoll(){
      //sendMessage(new Message(chan, "PROVA (viene mandato ogni 20s)"));
      try{
          InputStream is = new URL(apiEndpoint).openStream();
          JsonArray topicList = Json.createReader(is).readArray();
          for(JsonValue v: topicList){
              JsonObject obj = (JsonObject)v;
              sendMessage(new Message(chan, obj.getString("subject") + " http://pierotofy.it" + obj.getString("url")));
          }
          Thread.sleep(interval);
      }catch(IOException e){
          e.printStackTrace();
          sendMessage(new Message(chan, "Attenzione: impossibile recuperare gli aggiornamenti dal forum."));
      }catch(InterruptedException e){
          e.printStackTrace();
      }
  }
}
