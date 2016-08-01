/*
 *    Pizza IRC Bot (for pierotofy.it community)
 *    Copyright (C) 2016 Benato Denis
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pizza;

import java.io.IOException;
import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import java.util.regex.*;

/**
 * The main bot class
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Pizza extends PircBot {

    public Pizza(String botName) {
        //set the name of the bot
        this.setName(botName);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.print("Pizza  Copyright (C) 2016  pierotofy.it community\n"
                + "This program comes with ABSOLUTELY NO WARRANTY;\n"
                + "This is free software, and you are welcome to redistribute it\n"
                + "under certain conditions.\n\n");
        
        String botName = "", botServer = "", botChannel = "";
        try {
            botName = args[0];
            botServer = args[1];
            botChannel = args[2];
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Utilizzo: java -jar Pizza.jar BotName server[:port] #channel");
            System.exit(-1);
        }
        
        // Prova a estrarre server e porta
        String server = "irc.freenode.net", port = "6667";
        Pattern serverRegex = Pattern.compile("(\\w+)\\.(\\w+)\\.(\\w+)(\\:(\\d+))?");
        Matcher serverMatcher = serverRegex.matcher(botServer);
        int portSeparator = botServer.indexOf(":");
        if (!serverMatcher.matches()) {
            System.out.println("Il server specificato non e' valido");
            System.exit(-1);
        } else if (portSeparator <= -1) {
            botServer = botServer + ":6667";
        } else if (portSeparator == 0) {
            System.out.println("Il nome del server e' errato");
            System.exit(-1);
        }
        server = botServer.substring(0, portSeparator);
        port = botServer.substring(portSeparator+1, botServer.length());
        
        // Controlla il nome del canale
        Pattern channelRegex = Pattern.compile("\\#(([\\w]?)([\\d]?)([\\-]?)([\\_]?)([\\.]?))+");
        Matcher channelMatcher = channelRegex.matcher(botChannel);
        if (!channelMatcher.matches()) {
            System.out.println("Il nome del canale specificato non e' valido");
            System.exit(-1);
        }
        
        // Inizializza il bot
        Pizza bot = new Pizza(args[0]);
        
        // Abilita l'output di debug
        // bot.setVerbose(true);
        
        try {
            // Connetti il bot al server
            bot.connect(server, Integer.parseInt(port));

            // E unisciti al canale
            bot.joinChannel(botChannel);
        } catch (IOException ex) {
            
        } catch (IrcException ex) {
            
        }
    }
    
}