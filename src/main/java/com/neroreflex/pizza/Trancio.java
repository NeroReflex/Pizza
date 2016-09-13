/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neroreflex.pizza;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author benat
 */
public abstract class Trancio /* implements Runnable*/ {
    
    private class PluginRunner implements Runnable {

        Trancio plugin;
        
        public PluginRunner(Trancio plg) {
            plugin = plg;
        }
        
        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    };
    
    protected Duration delay;
    
    private String botID;
    
    private Date startupDate;
    
    private final Thread requestManager, autoManager;
    
    /**
     * La coda di richieste fatte al plugin
     */
    protected final DelayQueue<DelayedRequest> requests;
    
    public Trancio() {
        this.delay = Duration.ofSeconds(1);
        this.requests = new DelayQueue<>();
        
        // Definisco le strutture dei gestori di chiamate al plugin
        PluginRunner requestRunner = new PluginRunner(this) {
            @Override
            public void run() {
                while ((this.plugin.isLoaded() && (!Thread.currentThread().isInterrupted()))) {
                    // Ottiene la richiesta da esaudire
                    Request request = this.plugin.unqueueRequest(Request.Type.Request);

                    // Chiama il gestore dell'evento
                    this.plugin.onCall(request);
                }
            }
        };
        PluginRunner automaticRunner = new PluginRunner(this) {
            @Override
            public void run() {
                while ((this.plugin.isLoaded() && (!Thread.currentThread().isInterrupted()))) {
                    // Chiama il gestore dell'evento
                    this.plugin.onPoll();
                    
                    // Inserisci una richiesta automatica affinche' l'istruzione successiva metta il thread in sleep
                    this.plugin.enqueueRequest(new Request(Request.Type.Automatic, "", "", ""), this.plugin.GetPollingDelay());
                    
                    // Ottiene la richiesta da esaudire (attendi il tempo stabilito)
                    this.plugin.unqueueRequest(Request.Type.Automatic);
                }
            }
        };
                
        // Crea il gestore dell'onCall dei plugins
        this.requestManager = new Thread(requestRunner);
        this.autoManager = new Thread(automaticRunner);
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
        
        // Esegui i gestori degli eventi del plugin
        try {
            Class<?>[] onCallArgs = {  }, onPollArgs = {  };
            
            Method[] methods = Trancio.class.getMethods();
            for (Method method : methods) {
                if (method.getName().compareTo("onCall") == 0) onCallArgs = method.getParameterTypes();
                else if (method.getName().compareTo("onPoll") == 0) onPollArgs = method.getParameterTypes();
            }
            
            if (this.getClass().getMethod("onCall", onCallArgs).getDeclaringClass() != Trancio.class) this.requestManager.start();
            if (this.getClass().getMethod("onPoll", onPollArgs).getDeclaringClass() != Trancio.class) this.autoManager.start();
        } catch (NoSuchMethodException ex) {
            System.err.println("Method onCall or onPoll not found!");
            System.exit(-7);
        }
        
        // Gli ultimi step dell'inizializzazione possono essere personalizzati
        this.onInitialize();
        
        // Il caricamento e' stato completato
        this.doneLoading();
    }
    
    public final Date getDate() {
        return this.startupDate;
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Gli ultimi step della de-inizializzazione possono essere personalizzati
        this.onShutdown();
        
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
     * @param timer  il lasso di tempo nel quale la richiesta NON potra' essere esaudita
     */
    public final void enqueueRequest(Request action, Duration timer) {
        // Inserisci la richiesta nella coda
        this.requests.put(new DelayedRequest(action, timer));
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
    protected final Request unqueueRequest(Request.Type requestType) {
        // Cerco il prossimo lavoro da svolgere
        try {
            // Rimango in attesa di ricevere la richiesta (se la lista è vuota)
            Request req;
            do {
                req = this.requests.take().GetRequest();
                
                if (req.GetType() != requestType)
                    this.requests.put(new DelayedRequest(req, Duration.ofMillis(2)));
            } while (req.GetType() != requestType);
            
            return req;
            // Grazie lumo_e per il suggerimento sulla BlockingQueue.
            // Ora il thread viene messo a dormire in caso di coda vuota
        } catch (InterruptedException ex) {
            
        }
        
        return null;
    }
    
    
    
    
    
    /*      BUON DIVERTIMENTO      */
    
    protected /*abstract*/ void onInitialize()/*;*/ {}
    
    protected /*abstract*/ void onShutdown()/*;*/ {}

    protected /*abstract*/ String onHelp()/*;*/ { return ""; }
    
    public /*abstract*/ void onCall(Request req)/*;*/ {}
    
    public /*abstract*/ void onPoll()/*;*/ {}
}
