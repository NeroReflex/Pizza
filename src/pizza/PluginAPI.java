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
 * Una classe che Permette ai plugin di accedere alle funzioni esclusive del bot.
 * 
 * @author Benato Denis
 */
public abstract class PluginAPI {
    
    protected static HashMap<String, Pizza> robotAttivi;
    
    private static boolean initiaziled = false;
    
    /**
     * SOLO USO INTERNO: ogni chiamata da parte dell'utente e' un no-op!
     * Inizializza la lista di bot attivi.
     */
    public static synchronized void Init() {
        // Inizializza la lista di bot attivi
        if (!PluginAPI.initiaziled) {
            PluginAPI.robotAttivi = new HashMap<>();
            PluginAPI.initiaziled = true;
        }
    }
    
    /**
     * SOLO USO INTERNO: ogni chiamata da parte dell'utente e' un rischio!
     * Registra il bot e lo inserisce nella lista dei bot pronti a
     * soddisfare le richieste dei loro plugin.
     * 
     * @param robot il bot da registrare
     */
    public static synchronized void addBot(Pizza robot) {
        // Registra il bot
        if (!PluginAPI.robotAttivi.containsKey(robot.getBotID())) {
            PluginAPI.robotAttivi.put(robot.getBotID(), robot);
        }
    }
    
    /**
     * SOLO USO INTERNO: ogni chiamata da parte dell'utente e' un rischio!
     * Rimuove il bot dalla lista dei bot pronti a soddisfare le
     * richieste dei loro plugin.
     * 
     * @param robot il bot da rimuovere
     */
    public static synchronized void removeBot(Pizza robot) {
        // Rimuovi il bot
        if (PluginAPI.robotAttivi.containsKey(robot.getBotID())) {
            PluginAPI.robotAttivi.remove(robot.getBotID());
        }
    }
    
    /**
     * Ottiene la lista di canali a cui il bot specificato e' connesso.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @return la lista di canali
     */
    public static String[] getChannels(String botId) {
        // Ritorna la lista di canali a cui il bot e' connesso
        if (PluginAPI.robotAttivi.containsKey(botId))
            return PluginAPI.robotAttivi.get(botId).getChannels();
        
        // Se il bot non esiste e l'ID e' fittizzio ritorna null
        return null;
    }
    
    /**
     * Ottiene la porta usata dal bot per la connessione al server.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @return la porta usata per la connessione al server
     */
    public static int getPort(String botId) {
        // Ritorna la lista di canali a cui il bot e' connesso
        if (PluginAPI.robotAttivi.containsKey(botId))
            return PluginAPI.robotAttivi.get(botId).getPort();
        
        // Se il bot non esiste e l'ID e' fittizzio ritorna null
        return -1;
    }
    
    /**
     * Ottiene l'indirizzo a cui il bot e' connesso.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @return l'indirizzo del server
     */
    public static String getServer(String botId) {
        // Ritorna la lista di canali a cui il bot e' connesso
        if (PluginAPI.robotAttivi.containsKey(botId))
            return PluginAPI.robotAttivi.get(botId).getServer();
        
        // Se il bot non esiste e l'ID e' fittizzio ritorna null
        return null;
    }
    
    /**
     * Ottiene il nickname che il bot usa nel rimanere connesso al server.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @return il nick del bot
     */
    public static String getNick(String botId) {
        // Ritorna la lista di canali a cui il bot e' connesso
        if (PluginAPI.robotAttivi.containsKey(botId))
            return PluginAPI.robotAttivi.get(botId).getNick();
        
        // Se il bot non esiste e l'ID e' fittizzio ritorna null
        return null;
    }
    
    public static void sendMessage(String botId, Message msg) {
        // Metti in coda il messaggio
        MessageQueue.enqueueMessage(botId, msg);
    }
}
