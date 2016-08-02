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

import java.sql.*;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import java.util.regex.*;

/**
 * La classe che, una volta istanziata identificherà un bot connesso ad un server
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Pizza extends PircBot {
    
    protected Connection sqliteDriver;
    
    protected HashMap<String, Trancio> Tranci;
    
    /**
     * Registra un trancio per essere utilizzato nel bot attuale
     * 
     * @param nomeTrancio il nome con cui sarà attivabile il plugin dalla chat
     * @param istanzaTrancio la istanza del trancio pronta ad essere utilizzata
     */
    public void RegistraTrancio(String nomeTrancio, Trancio istanzaTrancio) {
        // Registra il nuovo trancio
        this.Tranci.put(nomeTrancio, istanzaTrancio);
        
        // Inizializza il plugin
        this.Tranci.get(nomeTrancio).Initialize(nomeTrancio);
    }
    
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (message.compareTo(this.getNick()) == 0) {
            this.sendMessage(channel, "Sono il vostro amichevole robottino mangione :)");
            return;
        }

        // Analizza il messaggio per identificare richieste fatte al bot
        Pattern invokeRegex = Pattern.compile(this.getNick() + "([\\s]+)([\\S]+)([\\s\\S]+)");
        Matcher invokeMatcher = invokeRegex.matcher(message);
        if (invokeMatcher.matches()) {
            //get the command name
            String command = invokeMatcher.group(2);
            
            this.sendMessage(channel, "Dovrei fare qualcosa.... Ma non so cosa significa '" + command + "' :(");
        }
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        // Chiudi la connessione al database interno
        if (!this.sqliteDriver.isClosed()) {
            try {
                this.sqliteDriver.close();
            } catch (SQLException ex) {
                
            }
        }
    }
    
    public Pizza(String botName, String botServer, int botPort, HashMap<String, String> botParams) {
        // Set the name of the bot
        this.setName(botName);
        
        // Open the database that holds the enabled plugins
        try {
            this.sqliteDriver = DriverManager.getConnection("jdbc:sqlite:" + this.getName() + ".db");
            this.sqliteDriver.setAutoCommit(true);
            
            // Crea (se non esiste) la tabella dei plugin utilizzabili
            Statement stmt = this.sqliteDriver.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS plugins ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "level INTEGER,"
                    + "url TEXT,"
                    + "date TEXT"
                    + ");");
            stmt.close();
            
            //this.sqliteDriver.commit();
            
            //this.sqliteDriver.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        
        // Abilita l'output di debug (se richiesto), spento di default
        String verbose = botParams.get("--verbose");
        if (verbose.compareTo("on") == 0) {
            this.setVerbose(true);
        } else if (verbose.compareTo("off") == 0) {
            this.setVerbose(false);
        } else {
            System.out.println("Il valore del parametro --verbose non e' riconosciuto");
            System.exit(-1);
        }
        
        try {
            // Connetti il bot al server
            this.connect(botServer, botPort);

            // identifica il bot tramite NickServ
            String password = botParams.get("--identify");
            if (password.length() > 0)
                this.identify(password);
        } catch (IOException ex) {
            System.out.println("Errore nella connessione al server IRC: " + ex);
            System.exit(-2);
        } catch (IrcException ex) {
            System.out.println("Errore nella connessione al server IRC: " + ex);
            System.exit(-3);
        }
    }
    
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.compareTo(this.getNick()) != 0) {
            this.sendMessage(channel, "Benvenuto " + sender + " :)");
        } else {
            this.sendMessage(channel, "Buongiorno ragazzi, sono PizzaBot: https://github.com/NeroReflex/Pizza :D");
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Pizza.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        // Stampa la licenza
        System.out.print("Pizza  Copyright (C) 2016  pierotofy.it community\n"
                + "This program comes with ABSOLUTELY NO WARRANTY;\n"
                + "This is free software, and you are welcome to redistribute it\n"
                + "under certain conditions.\n\n");
            
        // Leggi l'input
        String botName = "", botServer = "", botChannel = "";
        HashMap<String, String> botParams = new HashMap<>();
        try {
            int read = 0;
           
            botName = args[read++];
            botServer = args[read++];
            botChannel = args[read++];
                
            while ((args.length - read) >= 2) {
                // Leggi il paramentro attuale
                botParams.put(args[read], args[read + 1]);
                    
                //incrementa il contatore di argomenti letti
                read = read + 2;
            }
                
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
            
        if (!botParams.containsKey("--verbose")) {
            System.out.println("Puoi aggiungere output aggiuntivo utilizzando il parametro --verbose on");
            botParams.put("--verbose", "off");
        }
            
        if (!botParams.containsKey("--identify")) {
            System.out.println("Puoi identificate il bot tramite NickServ utilizzando il parametro --identify password");
            botParams.put("--identify", "");
        }
            
        // Inizializza il bot
        Pizza bot = new Pizza(botName, server, Integer.parseInt(port), botParams);
            
        // E unisciti al canale
        bot.joinChannel(botChannel);
    }    
}