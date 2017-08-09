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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.User;

/**
 * Una classe che Permette ai plugin di accedere alle funzioni esclusive del bot.
 * 
 * @author Benato Denis
 */
public abstract class PluginAPI {
    
    protected static HashMap<String, Pizza> robotAttivi;
    protected static HashMap<String, Connection> databaseHandlers;
    
    private static boolean initiaziled = false;
    
    /**
     * SOLO USO INTERNO: ogni chiamata da parte dell'utente e' un no-op!
     * Inizializza la lista di bot attivi.
     */
    public static synchronized void Init() {
        // Inizializza la lista di bot attivi
        if (!PluginAPI.initiaziled) {
            PluginAPI.robotAttivi = new HashMap<>();
            PluginAPI.databaseHandlers = new HashMap();
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
        if (!PluginAPI.robotAttivi.containsKey(robot.getBotID())) {
            // Registra il robot
            PluginAPI.robotAttivi.put(robot.getBotID(), robot);
            
            // Crea/Apri il database
            try {
                PluginAPI.databaseHandlers.put(robot.getBotID(), DriverManager.getConnection("jdbc:sqlite:" + PluginAPI.robotAttivi.get(robot.getBotID()).getName() + ".db"));
            } catch (SQLException ex) {
                System.err.println("The execution cannot continue without a database");
                
                System.exit(-8);
            }
        }
    }
    
    /**
     * SOLO USO INTERNO: Inizializza nel database una tabella che il plugin potra'
     * usare come memoria permanente di memorizzazoine dati.
     * 
     * @param botId ID univoco del bot al quale il plugin è collegato
     * @param pluginName il nome del plugin da inizializzare
     */
    public static synchronized void initializePlugin(String botId, String pluginName) {
        if (!PluginAPI.databaseHandlers.containsKey(botId)){
            System.out.println("The given bot is not registered as an active bot");
            
            System.exit(-11);
        }
        
        try {
            // Crea (se non esiste) la tabella del db usata dal plugin per memorizzare dati
            PreparedStatement stmt = PluginAPI.databaseHandlers.get(botId).prepareStatement(
                      "CREATE TABLE IF NOT EXISTS \"" + pluginName + "\" ("
                    + "key TEXT NOT NULL,"
                    + "value TEXT"
                    + ")");
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("It was impossible to create the table for the " + pluginName + " plugin: " + ex.getMessage());
            
            System.exit(-10);
        }
    }
    
    public static synchronized String loadData(String botId, String pluginName, String key) {
        if (!PluginAPI.databaseHandlers.containsKey(botId)){
            System.out.println("The given bot is not registered as an active bot");
            
            System.exit(-11);
        }
        
        try {
            ResultSet rs;
            try ( // Cerco la chiave data, per restituire il valore associato
                    PreparedStatement stmt = PluginAPI.databaseHandlers.get(botId).prepareStatement(
                            "SELECT * FROM \"" + pluginName + "\" WHERE key=?"
                    )) {
                stmt.setString(1, key);
                rs = stmt.executeQuery();
                // C'e' una coppia chiave-valore con la chiave data e il valore di value va ritornato
                if (rs.next())
                    return rs.getString("value");
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("It was impossible to fetch the given data: " + ex.getMessage());
            
            System.exit(-10);
        }
        
        return null;
    }
    
    public static synchronized void storeData(String botId, String pluginName, String key, String value) {
        if (!PluginAPI.databaseHandlers.containsKey(botId)){
            System.out.println("The given bot is not registered as an active bot");
            
            System.exit(-11);
        }
        
        try {
            // Cerco la chiave data, per sapere se devo eseguire un insert o un update
            PreparedStatement stmt = PluginAPI.databaseHandlers.get(botId).prepareStatement(
                    "SELECT * FROM \"" + pluginName + "\" WHERE key=?"
                );
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            
            // Non c'e' una coppia chiave-valore con la chiave data
            if (!rs.next()) {
                // Devo creare la coppia chiave-valore
                stmt = PluginAPI.databaseHandlers.get(botId).prepareStatement(
                        "INSERT INTO \"" + pluginName + "\" (value, key) VALUES (?, ?)"
                    );
            } else {
                // Devo aggiornare la coppia chiave-valore
                stmt = PluginAPI.databaseHandlers.get(botId).prepareStatement(
                        "UPDATE \"" + pluginName + "\" SET \"value\"=? WHERE \"key\"=?");
            }
            
            // Evito SQLInjection automatizzando l'escape ed eseguo la query
            stmt.setString(1, value);
            stmt.setString(2, key);
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println("It was impossible to save the given data: " + ex.getMessage());
            
            System.exit(-10);
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
            try { PluginAPI.databaseHandlers.get(robot.getBotID()).close(); } catch (Exception ex) { }
            PluginAPI.databaseHandlers.remove(robot.getBotID());
        }
    }
    
    /**
     * Ottiene la lista di canali a cui il bot specificato e' connesso.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @return il vettore di nomi di canali
     */
    public static Vector<String> getChannels(String botId) {
        Vector<String> channels = new Vector<>();

        // Ritorna la lista di canali a cui il bot e' connesso
        if (PluginAPI.robotAttivi.containsKey(botId))
            for (String channel : PluginAPI.robotAttivi.get(botId).getChannels())
                channels.add(channel);

        return channels;
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

        return null;
    }
    
    /**
     * Scrive nella lista di messaggi del bot il messaggio dato.
     * 
     * @param botId l'ID univoco del bot che dovra' scrivere il messaggio
     * @param msg il messaggio che dovra' essere scritto
     */
    public static void sendMessage(String botId, Message msg) {
        // Metti in coda il messaggio
        PluginAPI.robotAttivi.get(botId).enqueueMessage(msg);
    }
    
    /**
     * Connetti il bot al nuovo canale.
     * 
     * @param botId l'ID univoco del bot con cui interagire
     * @param channel il nome del nuovo canale
     * @param key la chiave da usare nella connessione del canale
     */
    public static void joinChannel(String botId, String channel, String key) {
        // Unisci il bot al canale
        if (PluginAPI.robotAttivi.containsKey(botId))
            PluginAPI.robotAttivi.get(botId).joinChannel(channel, key);
    }

    /**
     * Ottiene la lista di utenti espressa come vettore di nick.
     *
     * @param botId l'ID univoco del bot con cui interagire
     * @param channel il canale di cui si ricercano informazioni sugli utenti
     * @return il vettore di nickname
     */
    public static Vector<String> getUsers(String botId, String channel) {
        Vector<String> users = new Vector<>();

        // Ottieni la lista di utenti
        if (PluginAPI.robotAttivi.containsKey(botId)) {

            // Inserisci nel vettore il nickname completo di ogni utente
            for (User user : PluginAPI.robotAttivi.get(botId).getUsers(channel)) {
                users.add(user.getNick()); // o e' meglio .toString() ?
            }
        }

        return users;
    }
}
