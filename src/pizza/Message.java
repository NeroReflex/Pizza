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

/**
 * Identifica un messaggio da scrivere in un canale o ad un utente.
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public final class Message {
    
    private final String channel, message;
    
    public Message(String ch, String msg) {
        this.channel = ch;
        this.message = msg;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public String getChannel() {
        return this.channel;
    }
    
}
