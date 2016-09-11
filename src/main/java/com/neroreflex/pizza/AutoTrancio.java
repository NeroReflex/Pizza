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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.*;

/**
 * Per creare un plugin che si attiva automaticamente ad un intervallo di tempo
 * pre-fissato e' necessario estendere questo plugin.
 * 
 * @author Benato Denis
 */
public abstract class AutoTrancio extends Trancio implements Runnable {
    /**
     * La coda di richieste fatte al plugin
     */
    protected final DelayQueue<Delayed> request;
    
    protected Duration delay;
    
    public AutoTrancio() { 
        this.delay = Duration.ofSeconds(1);
        this.request = new DelayQueue<>();
    }
    
    public final void run() {
        while (this.isLoaded() && (!Thread.currentThread().isInterrupted())) {
            try {
                // Chiama il gestore dell'evento
                this.onPoll();
                
                // Accoda la richiesta
                this.request.put(new DelayedObject(this.delay));
                
                // Ottieni la richiesta da soddisfare quando il delay sarà scaduto
                this.request.take();
                // Il delay si cambia cambiando la proprieta' delay (long).
            } catch (InterruptedException ex) {
                
            }
        }
    }
    
    
    /*      QUESTO LO HANNO SOLO I PLUGIN ATTIVATI AUTOMATICAMENTE     */
    
    protected /*abstract*/ void onPoll()/*;*/ {}
}
