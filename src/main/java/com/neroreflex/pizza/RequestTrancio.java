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
import java.util.concurrent.*;

/**
 * Per creare un plugin che si attiva a richiesta (dell'utente) e' necessario
 * estendere questa classe.
 * 
 * @author Benato Denis
 */
public abstract class RequestTrancio extends Trancio implements Runnable {
    
    /**
     * La coda di richieste fatte al plugin
     */
    protected final LinkedBlockingQueue<Request> requests;
    
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
        // Registro il lavoro da svolgere
        try {
            // Inserisci la richiesta nella coda
            this.requests.put(action);
        } catch (InterruptedException ex) {
            
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
            
        }
        
        return null;
    }
    
    public RequestTrancio() {
        this.requests = new LinkedBlockingQueue<>(250);
    }
    
    public final void run() {
        while (this.isLoaded() && (!Thread.currentThread().isInterrupted())) {
            try {
                // Ottieni la richiesta da soddisfare
                Request req = this.unqueueRequest();
                
                // Chiama il gestore dell'evento
                this.onCall(req.getUser(), req.getChannel(), req.getArguments());
            } catch (NullPointerException ex) {
                
            }
        }
    }
    
    
    
    /*      QUESTO LO HANNO SOLO I PLUGIN INTERROGABILI A RICHIESTA     */
    
    protected /*abstract*/ String onHelp()/*;*/ { return ""; }
    
    protected /*abstract*/ void onCall(String user, String channel, Vector<String> args)/*;*/ {}
}
