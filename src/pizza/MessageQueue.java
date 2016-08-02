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
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public abstract class MessageQueue {

    private static boolean initiaziled = false;
    
    protected static HashMap<String, Pizza> robotAttivi;
    
    public static synchronized void Init() {
        if (!initiaziled) {
            MessageQueue.robotAttivi = new HashMap<>();
            initiaziled = true;
        }
    }
    
    public static synchronized void addBot(String botId, Pizza robot) {
        // Registra il bot
        if (!robotAttivi.containsKey(botId))
            robotAttivi.put(botId, robot);
    }
    
    public static synchronized void removeBot(String botId) {
        // Registra il bot
        if (robotAttivi.containsKey(botId))
            robotAttivi.remove(botId);
    }
    
    public static synchronized void writeMessage(String botId, Message message) {
        // Aggiungi alla coda del bot il messaggio
        if (robotAttivi.containsKey(botId))
            robotAttivi.get(botId).queueMessage(message);
    }
    
}
