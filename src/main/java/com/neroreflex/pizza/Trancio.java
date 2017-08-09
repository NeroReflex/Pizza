/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neroreflex.pizza;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benat
 */
public abstract class Trancio {
    
    private class RequestRunner implements Runnable {

        /**
         * Il plugin del quale l'oggetto attuale e' l'ascoltatore di richieste
         */
        Trancio plugin;
        
        public RequestRunner(Trancio plg) {
            plugin = plg;
        }
        
        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    };
    
    private class TimerRunner extends TimerTask {

        /**
         * Il plugin del quale l'oggetto attuale e' l'ascoltatore di richieste
         */
        Trancio plugin;
        
        public TimerRunner(Trancio plg) {
            plugin = plg;
        }
        
        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    };
    
    /**
     * L'intervallo di tempo fra una chiamata ad onPoll e la successiva.
     */
    protected Duration delay;
    
    private String botID;
    
    private Date startupDate;
    
    /**
     * Il thread che si occupa di chiamare onCall.
     */
    private final Thread requestManager;
    
    /**
     * La prima volta che il plugin sara' attivato.
     */
    protected final GregorianCalendar firstSheduledPoll;
    
    /**
     * Il timer che chiama onPoll ad intervalli di tempo regolari.
     */
    private final Timer autoCaller;
    
    /**
     * La coda di eventi che il plugin dovra' risolvere.
     */
    protected final ArrayBlockingQueue<Event> events;
    
    public Trancio() {
        // Prepara la coda di richieste da esaudire e quella di eventi da processare
        this.events = new ArrayBlockingQueue<Event>(75, true);
        
        // Definisco le strutture dei gestori di chiamate al plugin
        RequestRunner requestRunner = new RequestRunner(this) {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    // Ottiene la richiesta da esaudire
                    Event request = this.plugin.unqueueEvent();

                    // Chiama il gestore dell'evento
                    this.plugin.processEvent(request);
                }
            }
        };
        
        // Crea il gestore dell'onCall dei plugins
        this.requestManager = new Thread(requestRunner);
        
        // schedule every six hours
        this.autoCaller = new Timer();
        
        // Prepara i riferimenti temporali
        this.firstSheduledPoll = new GregorianCalendar();
        this.firstSheduledPoll.add(Calendar.SECOND, 1);
        this.delay = Duration.ofSeconds(1);
    }
    
    public Duration GetPollingDelay() {
        return this.delay;
    }
    
    private boolean loaded = false;
    protected final synchronized void doneLoading() {
        this.loaded = true;
    }
    protected final synchronized boolean isLoaded() {
        return this.loaded;
    }
    
    /**
     * Ottieni l'ID interno del bot (usato solo all'interno del programma). 
     * 
     * @return ID del bot
     */
    public final String getBotID() {
        return this.botID;
    }
    
    /**
     * Connetti il bot al canale specificato
     * 
     * @param channel il nome del canale a cui il bot si unira'
     * @param key la chiave usata per l'accesso al canale
     */
    protected void joinChannel(String channel, String key) {
        PluginAPI.joinChannel(this.getBotID(), channel, key);
    }
    
    /**
     * Aggiunge alla coda di messaggi da scrivere il messaggio
     * 
     * @param msg il messaggio da scrivere
     */
    protected final void sendMessage(Message msg) {
        // Aggiungi il messaggio alla coda di messaggi da inviare
        PluginAPI.sendMessage(this.getBotID(), msg);
    }

    /**
     * Ottiene la lista di utenti espressa come vettore di nickname.
     *
     * @param channel il canale di cui si ricercano informazioni sugli utenti
     * @return il vettore di nickname
     */
    protected final Vector<String> getUsers(String channel) {
        // Ottengo la lista di utenti nel canale
        return PluginAPI.getUsers(this.getBotID(), channel);
    }

    /**
     * Ottiene la lista di canali ai quali il bot e' connesso.
     * Se una variazione dei canali occupati dal bot e' appena
     * avvenuta potrebbe non essere riportata!
     * 
     * @return la lista di canali a cui il bot e' connesso 
     */
    protected final Vector<String> getChannels() {
        return PluginAPI.getChannels(this.getBotID());
    }
    
    /**
     * Ottiene il nome del plugin in lettere minuscole
     * 
     * @return il nome del plugin attuale, in lettere MINUSCOLE
     */
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
    
    /**
     * Ottiene la porta usata dal bot per la connessione al server
     * 
     * @return il numero della porta
     */
    protected final int getPort() {
        return PluginAPI.getPort(this.getBotID());
    }
    
    /**
     * Ottiene il nome del server al quale il bot e' connesso
     * o il suo indirizzo IP
     * 
     * @return l'indirizzo del server
     */
    protected final String getServer() {
        return PluginAPI.getServer(this.getBotID());
    }
    
    /**
     * Inizializza il plugin.
     * 
     * Chiamato internamente quando il plugin e' caricato.
     * 
     * @param botID l'ID univoco dell'istanza che ha caricato il plugin
     */
    public final void Initialize(String botID) {
        // Evita un doppio caricamento
        if (this.isLoaded()) return;
        
        // Registra l'ID del bot che usa questo trancio
        this.botID = botID;
        
        // Usa l'ID del bot per ottenere l'accesso l database
        PluginAPI.initializePlugin(this.getBotID(), this.getName());
        
        // Registra il tempo di avvio
        this.startupDate = new Date();
        
        // Gli ultimi step dell'inizializzazione possono essere personalizzati
        this.onInitialize();
        
        // Esegui i gestori degli eventi del plugin
        this.requestManager.start();
        this.autoCaller.scheduleAtFixedRate(
            new TimerRunner(this) {
                @Override
                public void run() {
                    this.plugin.onPoll();
                }
            },
            this.firstSheduledPoll.getTime(),
            this.delay.toMillis()
        );
        // E' importante far partire il thread autoManger in questo punto:
        // Un plugin usa onInitialize per cambiare il tempo di attesa di un plugin
        
        // Il caricamento e' stato completato
        this.doneLoading();
    }
    
    /**
     * Ottengo l'orario in cui il plugin e' stato eseguito.
     * 
     * @return L'ora di esecuzione del plugin
     */
    public final Date getDate() {
        return this.startupDate;
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Gli ultimi step della de-inizializzazione possono essere personalizzati
        this.onShutdown();
        
        // Termino il timer
        this.autoCaller.cancel();
        
        // Termino il thread del gestore di richieste
        this.requestManager.interrupt();
        
        super.finalize();
    }

    /**
     * SOLO USO INTERNO: Inserisce una richeista nella coda FIFO di eventi da processare.
     * 
     * Questa funzione e' chiamata in un thread diverso di quello che chiama
     * unqueueRequest, precisamente dal thread principale del bot.
     * 
     * Se unqueueEvent ed enqueueEvent dovessero essere entrambi synchronized
     * l'architettura dei plugins non funzionerebbe!
     * 
     * @param event la richiesta fatta da un utente che il plugin dovra' esaudire
     */
    public final void enqueueEvent(Event event) {
        try {
            // Inserisci la richiesta nella coda
            this.events.put(event);
        } catch (InterruptedException ex) {
            System.err.println("The " + this.getName() + "plugin is interrupted, but a new request was enqueued");
            
            Logger.getLogger(Trancio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * SOLO USO INTERNO: Rimuove dalla coda FIFO l'ultimo elemento inserito, ovvero l'ultima
     * evento sollevato.
     * 
     * Grazie alla coda usata il thread viene messo in sleep dalla JVM se NON
     * sono presenti richieste.
     * 
     * Se unqueueEvent ed enqueueEvent dovessero essere entrambi synchronized
     * l'architettura dei plugins non funzionerebbe!
     * 
     * @return l'evento da processare
     */
    protected final Event unqueueEvent() {
        // Cerco il prossimo lavoro da svolgere
        try {
            // Rimango in attesa di ricevere la richiesta (se la lista è vuota)
            return this.events.take();
            // Grazie lumo_e per il suggerimento sulla BlockingQueue.
            // Ora il thread viene messo a dormire in caso di coda vuota
        } catch (InterruptedException ex) {
            System.err.println("The " + this.getName() + "plugin is interrupted, but a new request is going to be dequeued");
            
            Logger.getLogger(Trancio.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    /**
     * SOLO USO INTERNO: processa un evento rimosso dalla coda degli eventi.
     * L'unico scopo di questa funzione e' chiamare la corretta funzione,
     * quindi di fatto questa funzione ha la sola funzione di smistare le richieste.
     *
     * @param event l'evento da processare
     */
    protected final void processEvent(Event event) {
        EventType type = event.getType();
        Vector<String> args = event.getInfo();

        if (type == EventType.UserRequest) {
            this.onCall(
                    args.elementAt(0),
                    args.elementAt(1),
                    args.elementAt(2)
                );
        } else if (type == EventType.UserEnter) {
            this.onUserEnterChanel(
                    args.elementAt(0),
                    args.elementAt(1),
                    args.elementAt(2),
                    args.elementAt(3)
                );
        }
        else if (type == EventType.UserExit) {
            this.onUserLeaveChanel(
                    args.elementAt(0),
                    args.elementAt(1),
                    args.elementAt(2),
                    args.elementAt(3)
                );
        }
        else if (type == EventType.UserKicked) {
            this.onUserKicked(
                    args.elementAt(0),
                    args.elementAt(1),
                    args.elementAt(2),
                    args.elementAt(3),
                    args.elementAt(4),
                    args.elementAt(5)
                );
        }
        else if (type == EventType.UserQuit) {
            this.onUserLeaveServer(
                    args.elementAt(0),
                    args.elementAt(1),
                    args.elementAt(2),
                    args.elementAt(3)
            );
        }
        else if (type == EventType.HelpRequest) {
            this.onHelp(args.elementAt(1));
        }
    }
    
    /**
     * Memorizza una coppia chiave-valore nel database associato al bot.
     * Tale database sopravvive al riavvio del bot ed e' considerato
     * un supporto di memorizzazione non volatile.
     * 
     * @param key la chiave di accesso alla stringa memorizzata
     * @param value la stringa da memorizzare
     */
    protected final void storeData(String key, String value) {
        PluginAPI.storeData(this.getBotID(), this.getName(), key, value);
    }
    
    /**
     * Carica dal database il valore associato alla chiave data.
     * Tale database sopravvive al riavvio del bot ed e' considerato
     * un supporto di memorizzazione non volatile.
     * 
     * @param key il nome della chiave contenente il valore corretto
     * @return il valore associato alla chiave o null (se una chiave con quel nome non esiste)
     */
    protected final String loadData(String key) {
        return PluginAPI.loadData(this.getBotID(), this.getName(), key);
    }
    



    
    /*      BUON DIVERTIMENTO      */

    /**
     * Callback eseguita alla richiesta di aiuto nei confronti del plugin.
     * Dovrebbe mostrare qualcosa tipo !pluginname &lt;param1&gt; [&lt;optional param&gt;]
     * nella chat privata di chi ha inviato la richiesta di aiuto
     *
     * @param sender il nickname di chi ha inviato la richiesta di aiuto
     */
    protected void onHelp(String sender) { }

    /**
     * Callback eseguita alla inizializzazione del bot.
     */
    protected void onInitialize() {}

    /**
     * Callback eseguita allo spegnimento del bot o allo spegnimento del plugin
     */
    protected void onShutdown() {}

    /**
     * Callback eseguita alla connessione di un nuovo utente a uno dei canali occupati dal bot
     *
     * @param channel il canale al quale l'utente si e' unito
     * @param sender il nickname dell'utente appena entrato
     * @param login il login dell'utente appena entrato
     * @param hostname l'hostname dell'utente appena entrato
     */
    protected void onUserEnterChanel(String channel, String sender, String login, String hostname) {}

    /**
     * Callback eseguita alla disconnessione di un utente da uno dei canali occupati dal bot
     *
     * @param channel il canale dal quale l'utente si e' disconnesso
     * @param sender il nickname dell'utente appena uscito
     * @param login il login dell'utente appena uscito
     * @param hostname l'hostname dell'utente appena uscito
     */
    protected void onUserLeaveChanel(String channel, String sender, String login, String hostname) {}

    /**
     * Callback eseguita alla disconnessione da un utente dal server occupato dal bot.
     * Un evento di disconnessione e' catturabile se e solo se l'utente era unito ad
     * un canale occupato anche dal bot.
     *
     * @param sourceNick il nickname dell'utente appena uscito
     * @param sourceLogin il login dell'utente appena uscito
     * @param sourceHostname l'hostname dell'utente appena uscito
     * @param reason la ragione dell'uscita dell'utente
     */
    protected void onUserLeaveServer(String sourceNick, String sourceLogin, String sourceHostname, String reason) { }

    /**
     * Callback eseguita al kick di un utente da uno dei canali occupati dal bot
     *
     * @param channel il canale dal quale l'utente e' stato cacciato
     * @param kickerNick il nickname dell'utente che ha eseguito il kick
     * @param kickerLogin il login dell'utente che ha eseguito il kick
     * @param kickerHostname l'hostname dell'utente che ha eseguito il kick
     * @param recipientNick il nick dell'utente che e' stato cacciato
     * @param reason la ragione del kick
     */
    protected void onUserKicked(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {}

    /**
     * Callback eseguita quando un utente richiede l'esecuzione di uno specifico plugin
     *
     * @param channel il canale usato per eseguire il plugin
     * @param user il nickname dell'utente che intende eseguire il plugin
     * @param msg il messaggio usato per eseguire il plugin
     */
    public void onCall(String channel, String user, String msg) {
        this.sendMessage(new Message(channel, "the '" + this.getName() + "' plugin isn't meant to be actively called."));
    }

    /**
     * Callback eseguita a intervalli regolari di this.delay
     * a partire da this.firstSheduledPoll
     */
    public void onPoll() {}
}
