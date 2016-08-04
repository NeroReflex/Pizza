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

import java.util.HashMap;
import java.util.Vector;

/**
 * La coda di scrittura dei messaggi
 * 
 * @author Benato Denis
 */
public abstract class MessageQueue {

    private static boolean initiaziled = false;
    
    protected static HashMap<String, Pizza> robotAttivi;
    protected static HashMap<String, Vector<Message>> queues;
    
    public static synchronized void Init() {
        // Inizializza la coda di messaggi solo se non e' gia' stato fatto
        if (!initiaziled) {
            MessageQueue.robotAttivi = new HashMap<>();
            MessageQueue.queues = new HashMap<>();
            initiaziled = true;
        }
    }
    
    public static synchronized void addBot(String botId, Pizza robot) {
        // Registra il bot
        if (!robotAttivi.containsKey(botId)) {
            robotAttivi.put(botId, robot);
            queues.put(botId, new Vector<>());
        }
    }
    
    public static synchronized void removeBot(String botId) {
        // Rimuovi il bot
        if (robotAttivi.containsKey(botId)) {
            robotAttivi.remove(botId);
            queues.remove(botId);
        }
    }
    
    public static Pizza getBot(String botId) {
        try {
            return MessageQueue.robotAttivi.get(botId);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static synchronized void enqueueMessage(String botId, Message message) {
        // Aggiungi alla coda del bot il messaggio
        if (robotAttivi.containsKey(botId)) {
            // Aggiungi il messaggio alla lista
            queues.get(botId).add(message);
        }
    }
    
    public static synchronized Message dequeueMessage(String botId) {
        // Aggiungi alla coda del bot il messaggio
        if ((robotAttivi.containsKey(botId)) && (queues.get(botId).size() > 0)) {
            // Ottieni il primo messaggio della lista
            Message message = queues.get(botId).firstElement();
            queues.get(botId).remove(0);
            return message;
        }
        
        return null;
    }
    
}
