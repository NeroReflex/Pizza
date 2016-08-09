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
 * Identifica la coda di richieste da gestire per il bot.
 * 
 * @author Benato Denis
 */
public abstract class RequestQueue {
    
    private static boolean initiaziled = false;
    
    protected static Vector<Request> requests;
    
    public static synchronized void Init() {
        if (!initiaziled) {
            RequestQueue.requests = new Vector<>();
            initiaziled = true;
        }
    }
    
    public static synchronized void queueRequest(Request action) {
        RequestQueue.requests.add(action);
    }
    
    public static synchronized Request unqueueRequest(String botId, String nomeTrancio) {
        //scorri ogni trancio
        for (int i = 0; i < requests.size(); i++) {
            Request unqueuedRequest = requests.get(i);
            
            // Controlla se la richiesta e' stata fatta al trancio specificato
            if ((unqueuedRequest.getTrancio().compareTo(nomeTrancio) == 0) &&
                    (unqueuedRequest.getBotID().compareTo(botId) == 0)) {
                // Rimuovi la richiesta dalla lista
                requests.remove(i);
                
                // Ritorna la richiesta effettuata al trancio specificato
                return unqueuedRequest;
            }
        }
        
        // Di Default ritorno null
        return null;
    }
}
