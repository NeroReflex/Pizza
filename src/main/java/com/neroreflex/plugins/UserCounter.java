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
package com.neroreflex.plugins;

import com.neroreflex.pizza.*;
import java.util.HashMap;
import java.lang.String;
import java.lang.Integer;

/**
 * Un plugin per contare gli utenti nei diversi canali e
 * eseguire periodicamente un report verso l'url specificato.
 *
 * @author Benato Denis
 */
public class UserCounter extends Trancio {

    static String apiEndpoint = "";

    protected HashMap<String, Integer> users;

    @Override
    protected final void onInitialize() {
        this.users = new HashMap<>();
    }

    @Override
    protected void onUserConnection(String channel, String sender, String login, String hostname) {

    }

}
