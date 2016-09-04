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

import java.lang.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Per aggiungere funzionalità al bot è necessario estendere questa classe
 * 
 * @author Benato Denis
 */
public abstract class Trancio implements Runnable {
    
    private String botID;
    
    private Date startupDate;
    
    public Trancio() { }
    
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
    
    public final void run() {
        while (this.isLoaded()) {
            try {
                this.onPoll();
                
                // Ottieni la richiesta da soddisfare
                Request req = RequestQueue.unqueueRequest(this.getBotID(), this.getName());
                
                // Chiama il gestore dell'evento
                this.onCall(req.getUser(), req.getChannel(), req.getArguments());
            } catch (NullPointerException ex) {
                
            } finally {
                
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Gli ultimi step della de-inizializzazione possono essere personalizzati
        this.onShutdown();
        
        super.finalize();
    }
    
    
    
    
    
    
    
    /*      BUON DIVERTIMENTO      */
    
    protected /*abstract*/ String onHelp()/*;*/ { return ""; }
    
    protected /*abstract*/ void onInitialize()/*;*/ {}
    
    protected /*abstract*/ void onShutdown()/*;*/ {}
    
    protected /*abstract*/ void onCall(String user, String channel, Vector<String> args)/*;*/ {}
    
    protected /*abstract*/ void onPoll()/*;*/ {}
}
