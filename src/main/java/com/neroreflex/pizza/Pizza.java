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
import java.time.Duration;
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
        // Ottengo il numero di caratteri nel nick
        int nickLength = this.getNick().length();
        
        // Cerco di capire se mi e' stato dato un comando
        if ((message.charAt(0) == '!') && (message.length() > 1)) {
            // Ottieni il nome del comando
            int firstSpacePos = message.indexOf(' ');
            firstSpacePos = (firstSpacePos <= -1) ? message.length() : firstSpacePos;
            
            String command = message.substring(1, firstSpacePos);
            
            // Questa e' la stringa con ulteriori dettagli sulla operazione da eseguire
            String rawParamsString = message.substring(firstSpacePos);
            
            // Controlla se il trancio e' presente e registrato
            if (this.tranci.containsKey(command)) {
                // E se lo e' piazza la richiesta che dovra' prendere in carico
                if (this.tranci.get(command).isLoaded())
                    this.tranci.get(command).enqueueRequest(new Request(channel, sender, rawParamsString));
                else
                    this.sendMessage(channel, sender + " sorry but the requested plugin is not loaded (yet)");
            } else if (command.compareTo("help") == 0) {// E se non lo e' invia l'errore
                this.Help(new Request(channel, sender, rawParamsString));
            } else if (command.compareTo("info") == 0) {// E se non lo e' invia l'errore
                this.Info(new Request(channel, sender, rawParamsString));
            } else {
                this.enqueueMessage(new Message(channel, "The request '" + command + "' cannot be resolved"));
            }
        } else if (message.substring(0, nickLength).compareTo(this.getNick()) == 0) {
            this.enqueueMessage(new Message(channel, "type !help to get started"));
        }
    }
    
    public final void Info(Request helpRequest) {
        this.enqueueMessage(new Message(helpRequest.GetChannel(), "I'm PizzaBot (https://github.com/NeroReflex/Pizza) a small and modular IRC bot"));
 
    }
    
    public final void Help(Request helpRequest) {
        // Dividi la richiesta di aiuto in piu' parti
        Vector<String> args = helpRequest.GetBasicParse();
        
        // Non e' stato specificato un plugin
        if (args.isEmpty()) {
            this.tranci.entrySet().stream().forEach((entry) -> {
                        String name = entry.getKey();
                        Trancio trancio = entry.getValue();
                        
                        // Ottengo la stringa della guida
                        String help = trancio.onHelp();
                        
                        // Stampo la guida solo se ha senso (la stringa della guida NON E' vuota)
                        if (!help.isEmpty())
                            this.enqueueMessage(new Message(helpRequest.GetUser(), "!" + name + " " + help));
                    });
        } else {
            // Ottengo il nome del plugin
            String name = args.get(0);
            
            // Ho caricato un plugin che si chiama con quel nome?
            if (this.tranci.containsKey(name)) {
                // Ottengo la stringa della guida
                String help = this.tranci.get(name).onHelp();
                        
                // Stampo la guida solo se ha senso (la stringa della guida NON E' vuota)
                if (!help.isEmpty())
                    this.enqueueMessage(new Message(helpRequest.GetUser(), "!" + name + " " + help));
                else
                    this.enqueueMessage(new Message(helpRequest.GetUser(), "No help is provided by the '" + name + "' plugin."));
            } // Se non lo ho avverto l'utente
            else {
                this.enqueueMessage(new Message(helpRequest.GetUser(), "A plugin with name '" + name + "' doesn't exist."));
            }
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
        if (this.tranci.containsKey(nomeTrancio)) return false;
        
        // Registra il nuovo trancio
        this.tranci.put(nomeTrancio, istanzaTrancio);
        
        // Chiama l'inizializzatore del trancio
        this.tranci.get(nomeTrancio).Initialize(this.getBotID());
        
        // Installazione completata!
        return true;
    }
    
    /**
     * Usato internamente per caricare TUTTI i plugins nel package dei plugins.
     * 
     * Di fatto usa la reflection per ottenere TUTTE le classi che ereditano
     * dalla classe base Trancio (ovvero la classe da estendere per implementare un plugin)
     */
    protected void loadInternalPlugins() {
        // Trova tutti i plugin da caricare automaticamente
        org.reflections.Reflections reflections = new org.reflections.Reflections("com.neroreflex.plugins");

        // Esegui la ricerca di tutti i plugins
        Set<Class<? extends com.neroreflex.pizza.Trancio>> allClasses = 
                reflections.getSubTypesOf(com.neroreflex.pizza.Trancio.class);
        Iterator<Class<? extends com.neroreflex.pizza.Trancio>> it = allClasses.iterator();
        
        // Ed istanzia tutti i plugins
        while (it.hasNext()) {
            Class<?> futureObj = it.next();
            
            try {
                this.registerTrancio((Trancio)futureObj.getConstructor().newInstance());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {   
                System.out.println("Couldn't load the internal request plugin set (at " + futureObj.getSimpleName() + "): " + ex.getClass().getSimpleName() + ", message:" + ex.getMessage());
                System.exit(-5);
            }
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