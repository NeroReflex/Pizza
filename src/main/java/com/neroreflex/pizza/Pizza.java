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
import java.util.TimerTask;
import java.util.Timer;

/**
 * La classe che, una volta istanziata identifichera' un bot connesso ad un server
 *
 * @author Benato Denis
 */
public class Pizza extends PircBot {
    /**
     * Il nickname del bot.
     */
    private String connectedWithName;

    /**
     * Il server IRC per il bot.
     */
    private IRCServer connectedToHost;

    /**
     * La lista di parametri passati al bot.
     */
    private HashMap<String, String> connectedWithParams;

    /**
     * Il timer che riconnette il bot.
     */
    private Timer reconnectTimer;

    /**
     * Crea una nuova istanza del bot e la connette al server specificato,
     * poi fa il join nel canale dato per rendere il bot disponibile a tutti
     * gli utenti di quel canale.
     *
     * @param botName il nome che il bot usera' per connettersi al server
     * @param botServer il server IRC al quale il bot si connettera'
     * @param botChannel il canale di al quale il bot si unira'
     * @param botParams i parametri per il bot
     * @return La istanza del bot creato
     */
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
    public void enqueueMessage(Message msg) {
        this.sendMessage(msg.getChannel(), msg.getMessage());
    }



    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        // Evito di fare controlli su un messaggio vuoto e evito di stampare il messaggio di help se l'utente scrive solo: "!"
        // I messaggi vuoti non dovrebbero esistere, vedi https://tools.ietf.org/html/rfc2812.html Sezione 2.3.1: "Empty messages are silently ignored"
        if (message.length() <= 1) return;

        // Ottengo il numero di caratteri nel nick
        int nickLength = this.getNick().length();

        // Cerco di capire se mi e' stato dato un comando
        if (message.charAt(0) == '!') {
            // Ottieni il nome del comando
            int firstSpacePos = message.indexOf(' ');
            firstSpacePos = (firstSpacePos <= -1) ? message.length() : firstSpacePos;

            String command = message.substring(1, firstSpacePos);

            // Questa e' la stringa con ulteriori dettagli sulla operazione da eseguire
            String rawParamsString = message.substring(firstSpacePos);

            // Questo vettore mi servira' al momento della generazione dell'evento
            Vector<String> args = new Vector<>();
            args.add(channel);
            args.add(sender);
            args.add(rawParamsString);

            // Controlla se il trancio e' presente e registrato
            if (this.tranci.containsKey(command)) {
                // E se lo e' piazza la richiesta che dovra' prendere in carico
                if (this.tranci.get(command).isLoaded()) {
                    this.tranci.get(command).enqueueEvent(new Event(EventType.UserCall, args));
                }
                else {
                    this.sendMessage(channel, sender + " unavailable plugin.");
                }
            } else if (command.compareTo("help") == 0) {
                this.Help(new Event(EventType.HelpRequest, args));
            } else if (command.compareTo("info") == 0) {
                this.enqueueMessage(new Message(channel, "I'm PizzaBot (https://github.com/NeroReflex/Pizza) a small and modular IRC bot"));
            } else {
                this.enqueueMessage(new Message(channel, "The request '" + command + "' cannot be resolved"));
            }
        } else if (message.substring(0, nickLength).compareTo(this.getNick()) == 0) {
            this.enqueueMessage(new Message(channel, "type !help to get started"));
        }
    }

    /**
     * Il bot alla richiesta di aiuto fornisce la lista dei plugin e il loro
     * utilizzo.
     * Si puo' anche richiedere aiuto in uno specifico plugin.
     *
     * @param helpRequest la richiesta di aiuto
     */
    public final void Help(Event helpRequest) {
        // Dividi la richiesta di aiuto in piu' parti
        Vector<String> args = helpRequest.getBasicParse();

        // Non e' stato specificato un plugin
        if (args.isEmpty()) {
            this.tranci.entrySet().stream().forEach((entry) -> {
                        String name = entry.getKey();
                        Trancio trancio = entry.getValue();

                        // Ottengo la stringa della guida
                        String help = trancio.onHelp();

                        // Stampo la guida solo se ha senso (la stringa della guida NON E' vuota)
                        if (!help.isEmpty())
                            this.enqueueMessage(new Message(helpRequest.getInfo().get(1), "!" + name + " " + help));
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
                    this.enqueueMessage(new Message(helpRequest.getInfo().get(1), "!" + name + " " + help));
                else
                    this.enqueueMessage(new Message(helpRequest.getInfo().get(1), "No help is provided by the '" + name + "' plugin."));
            } // Se non lo ho avverto l'utente
            else {
                this.enqueueMessage(new Message(helpRequest.getInfo().get(1), "A plugin with name '" + name + "' doesn't exist."));
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
     * Connette il bot al server.
     */
    public void connectBot() {
        // Imposta il nome del bot
        this.setName(this.connectedWithName);

        try {
            // Connetti il bot al server
            this.connect(this.connectedToHost.getAddress(), this.connectedToHost.getPort());

            // identifica il bot tramite NickServ
            String password = this.connectedWithParams.get("--identify");
            if (password.length() > 0)
                this.identify(password);
        } catch (IOException ex) {
            System.err.println("Errore nella connessione al server IRC: " + ex);
        } catch (IrcException ex) {
            System.err.println("Errore nella connessione al server IRC: " + ex);
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
        // Salva cio che serve per connettersi (e riconnettersi)
        this.connectedWithName = botName;
        this.connectedToHost = botServer;
        this.connectedWithParams = botParams;

        // Inizializza la lista di tranci e la lista di esecutori
        this.tranci = new HashMap<>();

        // Inizializza le API dei plugin
        PluginAPI.Init();

        // Abilita l'output di debug (se richiesto), spento di default
        String verbose = this.connectedWithParams.get("--verbose");
        if (verbose.compareTo("on") == 0) {
            this.setVerbose(true);
        } else if (verbose.compareTo("off") == 0) {
            this.setVerbose(false);
        }

        this.connectBot();

        // Ottieni un ID univoco per il bot attuale
        SecureRandom random = new SecureRandom();
        this.botID = new BigInteger(130, random).toString(32);

        // Registra il bot attuale
        PluginAPI.addBot(this);

        // Cambia il nick
        this.setAutoNickChange(true);

        // Carica il set di plugin "standard"
        this.loadInternalPlugins();

        // Preparo tutto il necessario per controllare ogni minuto se il bot è disconnesso
        GregorianCalendar startingTime = new GregorianCalendar();
        startingTime.add(Calendar.SECOND, 60);
        this.reconnectTimer = new Timer();
        this.reconnectTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    if (!Pizza.this.isConnected()) {
                        Pizza.this.connectBot();
                    }
                }
            },
            startingTime.getTime(),
            1000 * 60 // Controlla ogni minuto
        );
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
        if (sender.equals(this.getNick()) || sender.equals(this.getName()) || sender.equals(this.getLogin())) {
            this.enqueueMessage(new Message(channel, "Salve ragazzi, sono PizzaBot: https://github.com/NeroReflex/Pizza :D"));
        } else {
            this.enqueueMessage(new Message(channel, "Welcome " + sender + " :)"));

            Vector<String> args = new Vector<>();
            args.add(channel);
            args.add(sender);
            args.add(login);
            args.add(hostname);

            Event currentEvent = new Event(EventType.UserEnter, args);

            for (Trancio plugin : this.tranci.values())
                plugin.enqueueEvent(currentEvent);
        }
    }

    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        if (!kickerNick.equals(connectedWithName)) { // Nel caso una vecchia istanza di PizzaBot venga cacciata? va implementato il ghost
        	this.joinChannel(channel);
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
