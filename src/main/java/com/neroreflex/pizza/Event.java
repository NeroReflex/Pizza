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

import java.lang.String;
import java.util.Vector;
import java.util.Arrays;

/**
 * Identifica una richiesta che verrà "esaudita" da un plugin della
 * chat in un diverso thread.
 * 
 * @author Benato Denis
 */
public class Event {

    protected EventType type;

    protected String[] info;
    
    public Event(EventType eType, String ... eInfo) {
        this.type = eType;
        this.info = eInfo;
    }

    /**
     * Restituisce il tipo di evento corrente.
     *
     * @return il tipo di evento
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * Restituisce un vettore di informazioni relative
     * all'evento strutturate in maniera fissa rispetto
     * al tipo di evento.
     *
     * @return le informazioni sull'evento
     */
    public Vector<String> getInfo() {
        return new Vector(Arrays.asList(this.info));
    }
    
}
