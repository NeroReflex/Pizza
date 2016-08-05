package plugins;

/**
 * Plugin per notificare nuovi messaggi sul forum di pierotofy.it
 *
 * @author Nitti Gianluca
 */
public final class PTforum extends pizza.Trancio{
  String chan = "#pierotofy.it";
  int interval = 20000; //TODO: rendere configurabili

  protected final void onPoll(){
    while(true){
      sendMessage(new pizza.Message(chan, "PROVA (viene mandato ogni 20s)"));
      try{
        Thread.sleep(interval);
      }catch(InterruptedException e){
        e.printStackTrace();
      }
    }
  }
}
