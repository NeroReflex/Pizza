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
package com.neroreflex.pizza;

import java.util.HashMap;
import java.util.Vector;

/**
 * La coda di scrittura dei messaggi.
 * 
 * @author Benato Denis
 */
public abstract class MessageQueue {

    private static boolean initiaziled = false;
    
    protected static HashMap<String, Vector<Message>> queues;
    
    /**
     * SOLO USO INTERNO: ogni chiamata da parte dell'utente e' un no-op!
     * Inizializza le liste di messaggi.
     */
    public static synchronized void Init() {
        // Inizializza la coda di messaggi solo se non e' gia' stato fatto
        if (!MessageQueue.initiaziled) {
            MessageQueue.queues = new HashMap<>();
            MessageQueue.initiaziled = true;
        }
    }
    
    /**
     * Metti nella coda dei messaggi che il bot dovra' scrivere il messaggio
     * da scrivere
     * 
     * @param botId l'ID del bot che deve scrivere il messaggio
     * @param message il messaggio da scrivere
     */
    public static synchronized void enqueueMessage(String botId, Message message) {
        // Aggiungi alla coda del bot il messaggio
        if (!queues.containsKey(botId))
            queues.put(botId, new Vector<>());
        
        // Aggiungi il messaggio alla lista
        queues.get(botId).add(message);
    }
    
    /**
     * Ottiene UN SINGOLO messaggio (come una coda FIFO) che il bot con il dato
     * ID dovra' scrivere.
     * 
     * @param botId l'ID univoco del bot che scrivera' il messaggio
     * @return null o il messaggio da scrivere
     */
    public static synchronized Message dequeueMessage(String botId) {
        // Aggiungi alla coda del bot il messaggio
        if ((queues.containsKey(botId)) && (queues.get(botId).size() > 0)) {
            // Ottieni il primo messaggio della lista
            Message message = queues.get(botId).firstElement();
            queues.get(botId).remove(0);
            return message;
        }
        
        return null;
    }
    
}
