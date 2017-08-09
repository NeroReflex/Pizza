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

import java.util.Vector;
import com.neroreflex.pizza.*;

/**
 * Un plugin per connettere il bot ad un altro canale
 * 
 * @author Benato Denis
 */
public final class Join extends Trancio {

    @Override
    protected void onHelp(String sender) {
        String help = "<channel> [<key>] - where <channel> is the channel name and <key> the channel password";

        this.sendMessage(new Message(sender, "!" + this.getClass().getSimpleName().toLowerCase() + " " + help));
    }

    @Override
    public final void onCall(String channel, String user, String msg) {
        // Che bello reinventare la ruota!
        String[] params = msg.split("([\\s]+)");
        Vector<String> args = new Vector<>();
        for (int i = 0; i < params.length; i++)
            if (params[i].length() > 0) args.add(params[i]);
        
        if (args.size() > 0) {
            // A che canale deve fare il join?
            String newChannel = args.get(0);
            newChannel = (newChannel.charAt(0) != '#') ? "#" + newChannel : newChannel;
            
            // Chiave del canale?
            String key = ((args.size() > 1) && (args.get(1).length() > 0))? args.get(0) : "";

            this.joinChannel(newChannel, key);
            this.sendMessage(new Message(channel, user + " I have joined " + newChannel + "."));
        } else {
            this.sendMessage(new Message(channel, user + " Please specify a channel"));
        }
    }
}
