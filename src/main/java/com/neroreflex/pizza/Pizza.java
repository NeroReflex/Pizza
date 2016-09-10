/*
 *    Pizza IRC Bot (for pierotofy.it community)
 *    Copyright (C) 2016 Benato Denis, Gianluca Nitti
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
package com.neroreflex.pizza;

import java.io.IOException;
import java.lang.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import java.util.regex.*;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

/**
 * La classe che, una volta istanziata identificherà un bot connesso ad un server
 * 
 * @author Benato Denis
 */
public class Pizza extends PircBot {
    
    public static final Pizza Asporto(String botName, String botServer, String botChannel, HashMap<String, String> botParams) {
        // Inizializza le API dei plugin
        PluginAPI.Init();
        
        // Inizializza il bot
        Pizza bot = new Pizza(botName, new IRCServer(botServer), botParams);
        
        // E unisciti al canale
        bot.joinChannel(botChannel);
        
        // E ritorna il nuovo bot
        return bot;
    }
    
    /**
     * L'ID del bot (SOLO USO INTERNO)
     */
    private final String botID;
    
    /**
     * La lista dei tranci di pizza attualmente attivi con il rispettivo nome
     */
    protected HashMap<String, Trancio> tranci;
    
    /**
     * La lista di threads generati dai plugins
     */
    private final HashMap<String, Thread> esecutoriTranci;
    
    /**
     * Ottieni l'ID interno del bot (usato solo all'interno del programma). 
     * 
     * @return ID del bot
     */
    public final String getBotID() {
        return this.botID;
    }
    
    /**
     * Inserisce il messaggio nella coda di messaggi da scrivere
     * 
     * @param msg il messaggio da accodare
     */
    public /*synchronized*/ void enqueueMessage(Message msg) {
        this.sendMessage(msg.getChannel(), msg.getMessage());
    }
    
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        // No plugins che si chiamano da soli (o chiamano altri plugins)
        if (sender.compareTo(this.getNick()) == 0) {
            this.enqueueMessage(new Message(channel, "***CENSORED*** spam avoid"));
            return;
        }
        
        // Analizza il messaggio per identificare richieste fatte al bot
        Pattern invokeRegex = Pattern.compile("(" + this.getNick() + ")([\\s]+)([\\w]+)([\\s]*)([\\s\\S\\/\\:\\\\]*)");
        Matcher invokeMatcher = invokeRegex.matcher(message);
        if (invokeMatcher.matches()) {
            // Ottieni il nome del comando
            String command = invokeMatcher.group(3).toLowerCase();
            
            // Questa e' la stringa con ulteriori dettagli sulla operazione da eseguire
            String rawParamsString = invokeMatcher.group(5);
            String[] params = rawParamsString.split("([\\s]+)");
            Vector<String> args = new Vector<>();
            
            try {
                for (int i = 0; i < params.length; i++) {
                    if (params[i].length() > 0) args.add(params[i]);
                }
            } catch (Exception ex) {
                
            }
            
            // Controlla se il bot e' stato salutato
            if ((command.compareTo("hello") == 0) && (args.isEmpty())) {
                this.enqueueMessage(new Message(channel, "Hello " + sender + " :)"));
            } // Qualcuno ha chiesto aiuto?
            else if ((command.compareTo("help") == 0) && (args.isEmpty())) {
                this.enqueueMessage(new Message(channel, sender + " you should type: " + this.getNick() + " plugin list"));
            }
            else if ((command.compareTo("plugin") == 0) && (!args.isEmpty())) {
                if (args.elementAt(0).compareTo("list") == 0) {
                    this.tranci.entrySet().stream().forEach((entry) -> {
                        String name = entry.getKey();
                        Trancio trancio = entry.getValue();
                        
                        this.enqueueMessage(new Message(channel, "Plugin \"" + name + "\" Started at: " + trancio.getDate()));
                    });
                } else if ((args.elementAt(0).compareTo("help") == 0) && (args.size() >= 2)) {
                    if (this.tranci.containsKey(args.get(1))) {
                        if (RequestTrancio.class.isAssignableFrom(this.tranci.get(args.get(1)).getClass())) {
                            String helpMsgByPlugin = ((RequestTrancio)this.tranci.get(args.get(1))).onHelp();
                            if (helpMsgByPlugin.length() > 0)
                                    this.enqueueMessage(new Message(channel, "Usage: " + this.getNick() + " " + args.get(1) + " " + helpMsgByPlugin));
                            else
                                this.enqueueMessage(new Message(channel, "The plugin doesn't provide an help message"));
                        } else {
                            this.enqueueMessage(new Message(channel, "The plugin doesn't provide an help message because it cannot be actively queryed"));
                        }
                    } else {
                        this.enqueueMessage(new Message(channel, "Plugin \"" + args.get(1) + "\" not found."));
                    }
                }
            }
            // Controlla se il trancio e' presente e registrato 
            else if (this.tranci.containsKey(command)) {
                // E se lo e' piazza la richiesta che dovra' prendere in carico
                if (RequestTrancio.class.isAssignableFrom(this.tranci.get(command).getClass())) {
                    RequestTrancio trancio = (RequestTrancio)this.tranci.get(command);
                    trancio.enqueueRequest(new Request(channel, sender, args));
                } else {
                    this.enqueueMessage(new Message(channel, "The requested plugin cannot be actively activated"));
                }
            } // E se non lo e' invia l'errore
            else {
                this.enqueueMessage(new Message(channel, "I don't know what I have to do at '" + command + "' request :("));
            }
        } else if (this.getNick().compareTo(message) == 0) {
            this.enqueueMessage(new Message(channel, "Sono il vostro amichevole robottino mangiapizza :)"));
        }
    }
    
    /**
     * Registra un trancio per essere utilizzato nel bot attuale
     * 
     * @param istanzaTrancio la istanza del trancio pronta ad essere utilizzata
     * @return true se la registrazione è andata a buon fine
     */
    public boolean registerTrancio(Trancio istanzaTrancio) {
        // Ottieni il nome del trancio di pizza
        String nomeTrancio = istanzaTrancio.getName();
        
        // Se il plugin e' gia' registrato segnala l'errore
        if (this.esecutoriTranci.containsKey(nomeTrancio)) return false;
        
        // Registra il nuovo trancio
        this.tranci.put(nomeTrancio, istanzaTrancio);
        
        // Chiama l'inizializzatore del trancio
        this.tranci.get(nomeTrancio).Initialize(this.getBotID());
        
        // Crea l'esecutore di operazioni in un thread separato
        this.esecutoriTranci.put(istanzaTrancio.getName(), new Thread(istanzaTrancio));
        this.esecutoriTranci.get(istanzaTrancio.getName()).start();
        
        // Installazione completata!
        return true;
    }
    
    protected void loadInternalPlugins() {
        // Trova tutti i plugin da caricare automaticamente
        org.reflections.Reflections reflections = new org.reflections.Reflections("com.neroreflex.plugins");

        // Esegui la ricerca di tutti i plugins con attivazione a richiesta
        Set<Class<? extends com.neroreflex.pizza.RequestTrancio>> allClasses = 
                reflections.getSubTypesOf(com.neroreflex.pizza.RequestTrancio.class);
        Iterator<Class<? extends com.neroreflex.pizza.RequestTrancio>> it = allClasses.iterator();
        
        // Ed istanzia tutti i plugins con attivazione a richiesta
        while (it.hasNext()) {
            Class<?> futureObj = it.next();
            
            try {
                this.registerTrancio((RequestTrancio)futureObj.getConstructor().newInstance());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {   
                System.out.println("Couldn't load the internal request plugin set (at " + futureObj.getSimpleName() + "): " + ex.getClass().getSimpleName() + ", message:" + ex.getMessage());
                System.exit(-5);
            }
        }
        
        // Esegui la ricerca di tutti i plugins con attivazione automatica
        Set<Class<? extends com.neroreflex.pizza.AutoTrancio>> allAutoClasses = 
                reflections.getSubTypesOf(com.neroreflex.pizza.AutoTrancio.class);
        Iterator<Class<? extends com.neroreflex.pizza.AutoTrancio>> ait = allAutoClasses.iterator();
        
        // Ed istanzia tutti i plugins con attivazione allClasses
        while (ait.hasNext()) {
            Class<?> futureObj = ait.next();
            
            try {
                this.registerTrancio((AutoTrancio)futureObj.getConstructor().newInstance());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {   
                System.out.println("Couldn't load the internal auto plugin set (at " + futureObj.getSimpleName() + "): " + ex.getClass().getSimpleName() + ", message:" + ex.getMessage());
                System.exit(-5);
            }
        }
        
        
        // Stampa tutti i plugins avviati
        for(Entry<String, Trancio> entry : this.tranci.entrySet()) {
            Trancio pluginCaricato = entry.getValue();

            System.out.println("plugin " + pluginCaricato.getName() + " loaded for bot [" +this.getName() + "/" + this.getBotID() + "]");
        }
    }
    
    /**
     * Inizializza una nuova istanza del bot e la connette al server dato.
     * 
     * @param botName il nome del bot
     * @param botServer il server IRC
     * @param botParams i parametri passati al bot al momento dell'avvio
     */
    public Pizza(String botName, IRCServer botServer, HashMap<String, String> botParams) {
        // Inizializza la lista di tranci e la lista di esecutori
        this.tranci = new HashMap<>();
        this.esecutoriTranci = new HashMap<>();
        
        // Inizializza le API dei plugin
        PluginAPI.Init();
        
        // Imposta il nome del bot
        this.setName(botName);
        
        // Abilita l'output di debug (se richiesto), spento di default
        String verbose = botParams.get("--verbose");
        if (verbose.compareTo("on") == 0) {
            this.setVerbose(true);
        } else if (verbose.compareTo("off") == 0) {
            this.setVerbose(false);
        }
        
        try {
            // Connetti il bot al server
            this.connect(botServer.getAddress(), botServer.getPort());

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
        
        // Registra il bot attuale
        PluginAPI.addBot(this);
        
        // Cambia il nick
        this.setAutoNickChange(true);
        this.changeNick("PizzaBot");
        
        // Carica il set di plugin "standard"
        this.loadInternalPlugins();
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
            
            // Rimuovi il bot dalla lista dei bot attivi
            PluginAPI.removeBot(this);
        } finally {
            super.finalize();
        }
    }
    
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        // Saluta il nuovo utente o presentati
        if ((sender.compareTo(this.getNick()) == 0) || (sender.compareTo(this.getName()) == 0) || (sender.compareTo(this.getLogin()) == 0)) {
            this.enqueueMessage(new Message(channel, "Salve ragazzi, sono PizzaBot: https://github.com/NeroReflex/Pizza :D"));
        } else {
            this.enqueueMessage(new Message(channel, "Welcome " + sender + " :)"));
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
            System.out.println("Usage: java -jar Pizza.jar \"BotName\" \"server[:port]\" \"#channel\"");
            System.exit(-1);
        }
        
        // Controlla il nome del canale
        Pattern channelRegex = Pattern.compile("\\#(([\\w]?)([\\d]?)([\\-]?)([\\_]?)([\\.]?))+");
        Matcher channelMatcher = channelRegex.matcher(botChannel);
        if (!channelMatcher.matches()) {
            System.err.println("Il nome del canale specificato non e' valido");
            System.exit(-1);
        }
            
        if (!botParams.containsKey("--verbose")) {
            System.out.println("You can add verbose output adding \"--verbose\" \"on\" to the arguments list");
            botParams.put("--verbose", "off");
        } else if ((botParams.get("--verbose").compareTo("off") != 0) && (botParams.get("--verbose").compareTo("on") != 0)) {
            System.err.println("The value of the \"--verbose\" argument is not valid");
            System.exit(-1);
        }
            
        if (!botParams.containsKey("--identify")) {
            System.out.println("You can identify the bot using NickServ by adding \"--identify\" \"password\" to the list of arguments");
            botParams.put("--identify", "");
        }
            
        // Esegui la prima istanza del bot
        Pizza.Asporto(botName, botServer, botChannel, botParams);
    }
}