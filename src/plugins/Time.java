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
package plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import pizza.Message;

/**
 * Un plugin per l'ora attuale.
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public final class Time extends pizza.Trancio {
    
    protected Message onCall(String user, String channel, Vector<String> args) {
        // Ottieni l'ora attuale
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        
        // e metti in coda la risposta
        return new Message(channel, user + " sono le " + sdf.format(cal.getTime()));
    }
    
}
