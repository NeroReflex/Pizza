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
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import java.util.regex.*;
import java.security.SecureRandom;
import java.math.BigInteger;


/**
 * La classe che, una volta istanziata identificherà un bot connesso ad un server
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Pizza extends PircBot implements Runnable {
    
    /**
     * L'ID del bot (SOLO USO INTERNO)
     */
    private final String botID;
    
    /**
     * La coda di messaggi da scrivere nel server attualmente connesso
     */
    private final Vector<Message> messages;
    
    protected HashMap<String, Trancio> tranci;
    
    /**
     * Il database usato per memorizzare certe operazioni
     */
    protected Scatola scatola;
    
    /**
     * Il thread che si occupa di scrivere i messaggi in coda
     */
    private final Thread messageWriter;
    
    /**
     * Ottieni l'ID interno del bot (usato solo all'interno del programma). 
     * 
     * @return ID del bot
     */
    public final String getBotID() {
        return this.botID;
    }
    
    /**
     * Usato da MessageQueue per inserire nella lista un messaggio da scrivere
     * nel server occupato dal bot.
     * 
     * @param msg il messaggio da inserire nella coda dei messaggi da scrivere 
     */
    protected final synchronized void queueMessage(Message msg) {
        // Aggiungi il messaggio da scrivere
        this.messages.add(msg);
    }
    
    /**
     * Usato dal thread dedicato alla sola scrittura dei messaggi
     */
    protected final synchronized void dequeueMessage() {
        // Se almeno un messaggio è in coda....
        if (this.messages.size() > 0) {
            // Scrivi il primo messaggio della coda
            Message toWrite = this.messages.firstElement();
            this.sendMessage(toWrite.getChannel(), toWrite.getMessage());
            
            // Rimuovi il messaggio appena scritto
            this.messages.remove(0);
        }
    }
    
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        // Analizza il messaggio per identificare richieste fatte al bot
        Pattern invokeRegex = Pattern.compile("(" + this.getNick() + ")([\\s]+)([\\w]+)([\\s]*)([\\s\\S]*)");
        Matcher invokeMatcher = invokeRegex.matcher(message);
        if (invokeMatcher.matches()) {
            // Ottieni il nome del comando
            String command = invokeMatcher.group(3);
            
            // E' richiesta l'installazione di un plugin?
            if (command.compareTo("install") == 0) {
                this.queueMessage(new Message(channel, "Al momento questo non e' possibile, scusa :*"));
            } else {
                // Controlla se il trancio e' presente e registrato
                if (!this.tranci.containsKey(command)) {
                    this.queueMessage(new Message(channel, "Dovrei fare qualcosa.... Ma non so cosa fare alla richiesta '" + command + "' :("));
                } else {
                    RequestQueue.queueRequest(new Request(this.getBotID(), command, channel, sender, new Vector<String>() ));
                }
            }
        } else if (this.getNick().compareTo(message) == 0) {
            this.queueMessage(new Message(channel, "Sono il vostro amichevole robottino mangiapizza :)"));
        }
    }
    
    /**
     * Registra un trancio per essere utilizzato nel bot attuale
     * 
     * @param nomeTrancio il nome con cui sarà attivabile il plugin dalla chat
     * @param istanzaTrancio la istanza del trancio pronta ad essere utilizzata
     */
    public void registerTrancio(Trancio istanzaTrancio) {
        // Ottieni il nome del trancio di pizza
        String nomeTrancio = istanzaTrancio.getClass().getSimpleName();
        
        // Registra il nuovo trancio
        this.tranci.put(nomeTrancio, istanzaTrancio);
        
        // Chiama l'inizializzatore del trancio
        this.tranci.get(nomeTrancio).Initialize(nomeTrancio, this.getBotID());
    }
    
    /**
     * Usato internamente per scrivere la lista di messaggi in maniera asincrona
     */
    @Override
    public final void run() {
        // Scrivi continuamente il primo messaggio della lista
        while (this.isConnected()) this.dequeueMessage();
    }
    
    /**
     * Inizializza una nuova istanza del bot e la connette al server dato.
     * 
     * @param botName il nome del bot
     * @param botServer l'indirizzo ip o DNS del server
     * @param botPort la porta sulla quale il server e' in ascolto
     * @param botParams i parametri passati al bot al momento dell'avvio
     */
    public Pizza(String botName, String botServer, int botPort, HashMap<String, String> botParams) {
        // Inizializza la coda di messaggi
        MessageQueue.Init();
        
        // Inizializza la coda di richieste
        RequestQueue.Init();
        
        // Imposta il nome del bot
        this.setName(botName);
        
        // Apri il database
        this.scatola = new Scatola(this.getName());
        
        // Inizializza lo spammer di messaggi in coda
        messages = new Vector<>();
        this.messageWriter = new Thread(this);
        
        // Abilita l'output di debug (se richiesto), spento di default
        String verbose = botParams.get("--verbose");
        if (verbose.compareTo("on") == 0) {
            this.setVerbose(true);
        } else if (verbose.compareTo("off") == 0) {
            this.setVerbose(false);
        }
        
        try {
            // Connetti il bot al server
            this.connect(botServer, botPort);

            // identifica il bot tramite NickServ
            String password = botParams.get("--identify");
            if (password.length() > 0)
                this.identify(password);
        } catch (IOException ex) {
            System.err.println("Errore nella connessione al server IRC: " + ex);
            System.exit(-2);
        } catch (IrcException ex) {
            System.err.println("Errore nella connessione al server IRC: " + ex);
            System.exit(-3);
        }
        
        // Ottieni un ID univoco per il bot attuale
        SecureRandom random = new SecureRandom();
        this.botID = new BigInteger(130, random).toString(32);
        MessageQueue.addBot(this.getBotID(), this);
        
        // Apri lo scrittore di messaggi in coda
        this.messageWriter.start();
    }
    
    /**
     * Chiudi la connessione al server e deinizializza il bot
     * 
     * @throws Throwable 
     */
    @Override
    public void finalize() throws Throwable {
        try {
            // Disconnettiti dal server
            this.disconnect();
            
            // Smetti di scrivere messaggi!
            this.messageWriter.interrupt();
            
            // Disattiva la coda di messaggi per il bot
            MessageQueue.removeBot(this.getBotID());
        } finally {
            super.finalize();
        }
    }
    
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.compareTo(this.getNick()) != 0) {
            this.queueMessage(new Message(channel, "Benvenuto " + sender + " :)"));
        } else {
            this.queueMessage(new Message(channel, "Buongiorno ragazzi, sono PizzaBot: https://github.com/NeroReflex/Pizza :D"));
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Inizializza il driver sqlite
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
            System.err.println("Il server specificato non e' valido");
            System.exit(-1);
        } else if (portSeparator <= -1) {
            botServer = botServer + ":6667";
        } else if (portSeparator == 0) {
            System.err.println("Il nome del server e' errato");
            System.exit(-1);
        }
        server = botServer.substring(0, portSeparator);
        port = botServer.substring(portSeparator+1, botServer.length());
            
        // Controlla il nome del canale
        Pattern channelRegex = Pattern.compile("\\#(([\\w]?)([\\d]?)([\\-]?)([\\_]?)([\\.]?))+");
        Matcher channelMatcher = channelRegex.matcher(botChannel);
        if (!channelMatcher.matches()) {
            System.err.println("Il nome del canale specificato non e' valido");
            System.exit(-1);
        }
            
        if (!botParams.containsKey("--verbose")) {
            System.out.println("Puoi aggiungere output aggiuntivo utilizzando il parametro --verbose on");
            botParams.put("--verbose", "off");
        } else if ((botParams.get("--verbose").compareTo("off") != 0) && (botParams.get("--verbose").compareTo("on") != 0)) {
            System.err.println("Il valore del parametro --verbose non e' riconosciuto");
            System.exit(-1);
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