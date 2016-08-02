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
public class Trancio {
    
    private Date startupDate;
    
    private String stratupName;
    
    public final void Initialize(String name) {
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
    
    protected void onInitialize() {
        
    }
    
    protected void onShutdown() {
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        // Gli ultimi step della de-inizializzazione possono essere personalizzati
        this.onShutdown();
    }
    
    protected String onCall(String user, Vector<String> args) {
        return "";
    }
}
