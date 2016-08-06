package plugins;

import java.util.Vector;
import pizza.Message;

/**
 * Un plugin per eseguire semplici operazioni matematiche
 *
 * @author Nitti Gianluca
 */
public class Calc extends pizza.Trancio {
    protected final void onCall(String user, String channel, Vector<String> args) {
        if(args.size() != 3)
            sendMessage(new Message(channel, user + " non ho capito :(. Dopo \"calc\", specifica il primo operando, seguito da un'operatore e quindi dal secondo operando, separati da spazi."));
        else{
            double a;
            double b;
            try{
                a = Double.parseDouble(args.get(0));
                b = Double.parseDouble(args.get(2));
            }catch(NumberFormatException e){
                sendMessage(new Message(channel, user + " gli operandi inseriti non sono entrambi numeri :(."));
                return;
            }
            char op = args.get(1).charAt(0);
            double r = 0;
            switch(op){
                case '+':
                    r = a + b;
                    break;
                case '-':
                    r = a - b;
                    break;
                case '*':
                    r = a * b;
                    break;
                case '/':
                    r = a / b;
                    break;
                default:
                    sendMessage(new Message(channel, user + " non conosco l'operatore \"" + args.get(1) + "\" :(. Sono supportati +,-,*,/"));
                    return;
            }
            sendMessage(new Message(channel, user + " " + a + op + b + "=" + r));
        }
    }
}
