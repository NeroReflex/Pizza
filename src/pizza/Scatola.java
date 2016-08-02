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

import java.sql.*;

/*
LEGGERE http://www.tutorialspoint.com/sqlite/sqlite_java.htm PER INTERAGIRE CON IL DATABASE
*/


/**
 * Questa class serve per gestire il database interno del bot, rendendolo
 * utilizzabile
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Scatola {
    /**
     * La connessione al database
     */
    protected Connection sqliteDriver;
    
    /**
     * Crea una connessione al database e lo inizializza se non esisteva
     * 
     * @param name il nome del database SENZA estensione
     */
    public Scatola(String name) {
        // Open the database that holds the enabled plugins
        try {
            this.sqliteDriver = DriverManager.getConnection("jdbc:sqlite:" + name + ".db");
            this.sqliteDriver.setAutoCommit(true);
            
            // Crea (se non esiste) la tabella dei plugin utilizzabili
            Statement stmt = this.sqliteDriver.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS plugins ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "level INTEGER,"
                    + "url TEXT,"
                    + "date TEXT"
                    + ");");
            stmt.close();
            
            //this.sqliteDriver.commit();
            
            //this.sqliteDriver.close();
        } catch (Exception e) {
            System.err.println("Errore nell'apertura del database");
            System.exit(-5);
        }
    }
    
    /**
     * Mi assicuro che il database venga chiuso quando non più necessario
     * 
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        // Chiudi la connessione al database interno
        if (!this.sqliteDriver.isClosed()) {
            try {
                this.sqliteDriver.close();
            } catch (SQLException ex) {
                System.err.println("Errore nella chiusur del database");
                System.exit(-5);
            }
        }
    }
    
}
