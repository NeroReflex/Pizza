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

/**
 * Identifica un messaggio da scrivere in un canale o ad un utente.
 * 
 * @author Benato Denis
 */
public final class Message {
    
    private final String channel, message;
    
    /**
     * Crea il nuovo messaggio selezionando la destinazione e il testo
     * 
     * @param ch il canale in cui mandare il messaggio
     * @param msg il test del messaggio
     */
    public Message(String ch, String msg) {
        this.channel = ch;
        this.message = msg;
    }
    
    /**
     * Ottieni il testo del messaggio
     * 
     * @return il testo
     */
    public String getMessage() {
        return this.message;
    }
    
    /**
     * Ottieni il nome del canale
     * 
     * @return ottieni il nome del canale
     */
    public String getChannel() {
        return this.channel;
    }
    
}
