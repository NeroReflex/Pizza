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
package com.neroreflex.pizza.Events;

import com.neroreflex.pizza.Event;
import com.neroreflex.pizza.EventType;
import java.util.Vector;

/**
 * Identifica una richiesta di aiuto verso un plugin o il bot,
 * che verrà "esaudita" da un plugin della
 * chat in un diverso thread (o dal bot).
 *
 * @author Benato Denis
 */
public class HelpRequestEvent extends Event {

    public HelpRequestEvent(String ... eInfo) {
        super(eInfo);

        this.type = EventType.HelpRequest;
    }

    public Vector<String> getBasicParse() {
        String message = this.info[2];

        String[] params = message.split("([\\s]+)");
        Vector<String> args = new Vector<>();

        for (int i = 0; i < params.length; i++)
            if (params[i].length() > 0) args.add(params[i]);

        return args;
    }

}