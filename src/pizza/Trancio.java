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

import java.lang.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Per aggiungere funzionalità al bot è necessario estendere questa classe
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Trancio implements Runnable {
    
    private String botID;
    
    private Date startupDate;
    
    private String stratupName;
    
    /**
     * Ottieni l'ID interno del bot (usato solo all'interno del programma). 
     * 
     * @return ID del bot
     */
    public final String getBotID() {
        return this.botID;
    }
    
    public final void writeMessage(Message msg) {
        // Aggiungi il messaggio alla coda di messaggi da inviare
        MessageQueue.writeMessage(this.getBotID(), msg);
    }
    
    public final void Initialize(String name, String botID) {
        // Registra l'ID del bot che usa questo trancio
        this.botID = botID;
        
        // Registra il tempo di avvio
        this.startupDate = new Date();
        
        // Registra il nome con cui e' stato avviato il plugin
        this.stratupName = name;
        
        //gli ultimi step dell'inizializzazione possono essere personalizzati
        this.onInitialize();
    }
    
    public final Date getDate() {
        return this.startupDate;
    }
    
    public final String getName() {
        return this.stratupName;
    }
    
    public void run() {
        while (true) {
            try {
                // Ottieni la richiesta da soddisfare
                Request req = RequestQueue.unqueueRequest(this.getBotID(), this.getClass().getSimpleName());

                // Crea il messaggio che sarà inviato come risposta
                Message response = this.onCall(req.getUser(), req.getChannel(), req.getArguments());

                // Invia alla coda il messaggio
                this.writeMessage(response);
            } catch (NullPointerException ex) {
                
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Gli ultimi step della de-inizializzazione possono essere personalizzati
        this.onShutdown();
        
        super.finalize();
    }
    
    
    
    
    
    
    
    /*      BUON DIVERTIMENTO!      */
    
    protected void onInitialize() {
        
    }
    
    protected void onShutdown() {
        
    }
    
    protected Message onCall(String user, String channel, Vector<String> args) {
        return new Message(channel, ":(");
    }
}
