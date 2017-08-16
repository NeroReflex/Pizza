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
import java.util.Arrays;
import java.util.Vector;

/**
 * Un plugin per fare load 'n' store di coppie chiavi-valore
 * 
 * @author Benato Denis
 */
public final class LoadStore  extends Trancio {

    @Override
    protected void onHelp(String sender) {
        String help = "<load|store> <key> [<value>] - where load or store is the action and value is needed only on store";

        this.sendMessage(new Message(sender, "!" + this.getClass().getSimpleName().toLowerCase() + " " + help));
    }

    @Override
    public final void onCall(String channel, String user, String msg) {
        Vector<String> args = new Vector<>(Arrays.asList(msg.split("\\s+")));
        args.removeIf(i -> i.isEmpty());
        
        if (args.size() == 3) {
            if (args.get(0).equals("store")) {
                // Memorizza la coppia chiave-valore
                this.storeData(args.get(1), args.get(2));
                
                // Informa l'utente dell'esito
                this.sendMessage(new Message(channel, user + " the value of '" + args.get(1) + "' has been changed"));
                
                return;
            }
        } else if (args.size() == 2) {
            if (args.get(0).equals("load")) {
                // Leggi il valore associato alla chiave
                String key = args.get(1);
                String value = this.loadData(key);
                
                if (value != null)
                    this.sendMessage(new Message(channel, user + " " + key + "=" + value));
                else
                    this.sendMessage(new Message(channel, user + " key '" + key + "' not found"));
                
                return;
            }
        }
        
        this.sendMessage(new Message(channel, user + " invalid arguments. Type !help loadstore"));
        
    }
}
