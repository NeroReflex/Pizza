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
    protected String onHelp() {
        return "<channel> <key> - where <channel> is the channel name and <key> the channel password";
    }
    
    @Override
    public final void onCall(Request req) {
        String user = req.getUser(), channel = req.getChannel();
        Vector<String> args = req.getBasicParse();
        
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
