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

/**
 * Rappresenta un evento di uscita di un utente da
 * un canale al quale il bot si e' unito.
 *
 * @author Benato Denis
 */
public class UserExitEvent extends Event {

    public UserExitEvent(String ... eInfo) {
        super(eInfo);

        this.type = EventType.UserExit;
    }
}
