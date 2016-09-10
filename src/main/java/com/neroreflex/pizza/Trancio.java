/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neroreflex.pizza;

import java.util.Date;
import java.util.Vector;

/**
 *
 * @author benat
 */
public abstract class Trancio implements Runnable {
    private String botID;
    
    private Date startupDate;
    
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
    
    
    
    
    
    
    
    /*      BUON DIVERTIMENTO      */
    
    protected /*abstract*/ void onInitialize()/*;*/ {}
    
    protected /*abstract*/ void onShutdown()/*;*/ {}

    @Override
    public void run() {
        // This class in NOT SUPPOSED to be instantiated
        throw new UnsupportedOperationException("Wrong way to do things.");
    }
}
