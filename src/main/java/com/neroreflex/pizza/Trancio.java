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
    
    protected final GregorianCalendar firstSheduledPoll;
    
    /**
     * Il timer che chiama onPoll ad intervalli di tempo regolari
     */
    private final Timer autoCaller;
    
    /**
     * La coda di richieste fatte al plugin
     */
    protected final ArrayBlockingQueue<Request> requests;
    
    public Trancio() {
        // Prepara la coda di richieste da esaudire
        this.requests = new ArrayBlockingQueue<>(250, true);
        
        // Definisco le strutture dei gestori di chiamate al plugin
        RequestRunner requestRunner = new RequestRunner(this) {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    // Ottiene la richiesta da esaudire
                    Request request = this.plugin.unqueueRequest();

                    // Chiama il gestore dell'evento
                    this.plugin.onCall(request);
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
     * Ottiene la lista di canali ai quali il bot e' connesso.
     * Se una variazione dei canali occupati dal bot e' appena
     * avvenuta potrebbe non essere riportata!
     * 
     * @return la lista di canali a cui il bot e' connesso 
     */
    protected final String[] getChannels() {
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
     * Inserisce una richeista nella coda FIFO di richieste da gestire.
     * 
     * Questa funzione e' chiamata in un thread diverso di quello che chiama
     * unqueueRequest, precisamente dal thread principale del bot.
     * 
     * Se unqueueRequest ed enqueueRequest dovessero essere synchronized
     * l'architettura dei plugins non funzionerebbe!
     * 
     * @param action la richiesta fatta da un utente che il plugin dovra' esaudire
     */
    public final void enqueueRequest(Request action) {
        try {
            // Inserisci la richiesta nella coda
            this.requests.put(action);
        } catch (InterruptedException ex) {
            System.err.println("The " + this.getName() + "plugin is interrupted, but a new request was enqueued");
            
            Logger.getLogger(Trancio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Rimuove dalla coda FIFO l'ultimo elemento inserito, ovvero l'ultima
     * richiesta effettuata (da un utente).
     * 
     * Grazie alla coda usata il thread viene messo in sleep dalla JVM se NON
     * sono presenti richieste.
     * 
     * Se unqueueRequest ed enqueueRequest dovessero essere synchronized
     * l'architettura dei plugins non funzionerebbe!
     * 
     * @return la richiesta da esaudire
     */
    protected final Request unqueueRequest() {
        // Cerco il prossimo lavoro da svolgere
        try {
            // Rimango in attesa di ricevere la richiesta (se la lista è vuota)
            return this.requests.take();
            // Grazie lumo_e per il suggerimento sulla BlockingQueue.
            // Ora il thread viene messo a dormire in caso di coda vuota
        } catch (InterruptedException ex) {
            System.err.println("The " + this.getName() + "plugin is interrupted, but a new request is going to be dequeued");
            
            Logger.getLogger(Trancio.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    
    
    
    
    /*      BUON DIVERTIMENTO      */
    
    protected void onInitialize() {}
    
    protected void onShutdown() {}

    protected String onHelp() { return ""; }
    
    public void onCall(Request req) {
        this.sendMessage(new Message(req.GetChannel(), "the '" + this.getName() + "' plugin isn't meant to be actively called."));
    }
    
    public void onPoll() {}
}
