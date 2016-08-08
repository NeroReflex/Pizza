package plugins;

import java.util.HashMap;
import java.util.ArrayList;
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
  int interval = 300000; //TODO: rendere configurabili

  HashMap<String, Integer> topics = new HashMap<String, Integer>(); //chiave=url, valore=numero risposte

  protected final void onPoll(){
      try{
          InputStream is = new URL(apiEndpoint).openStream();
          JsonArray topicList = Json.createReader(is).readArray();
          for(JsonValue v: topicList){
              JsonObject obj = (JsonObject)v;
              Integer oldReplies = topics.get(obj.getString("url"));
              //System.out.println(obj.getString("subject")); //per debug
              //System.out.println(obj.getInt("replies"));
              if(oldReplies == null)
                  topics.put(obj.getString("url"), obj.getInt("replies")); //se non è già in elenco, lo aggiungo
              else if(obj.getInt("replies") > oldReplies) //Se il numero di risposte è maggiore, allora ci sono stati nuovi post
                  sendMessage(new Message(chan, "Ci sono nuovi messaggi nel topic \"" + obj.getString("subject") + "\": http://pierotofy.it" + obj.getString("url")));
          }
          ArrayList<String> topicsToRemove = new ArrayList<String>();
          //Pulisce la hashmap dai vecchi topic (che non sono più nella risposta dell'api)
          for(String url: topics.keySet()){
              boolean toRemove = true;
              for(JsonValue v: topicList){
                  if(((JsonObject)v).getString("url").equals(url)){
                      toRemove = false;
                      break;
                  }
              }
              if(toRemove)
                  topicsToRemove.add(url);
          }
          for(String s: topicsToRemove){
              topics.remove(s);
              System.out.println("Rimosso vecchio topic " + s); //debug
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
