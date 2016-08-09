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
package com.neroreflex.plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import com.neroreflex.pizza.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Un plugin per connettere il bot ad un altro canale
 * 
 * @author Benato Denis
 */
public class Join extends Trancio {
    protected final void onCall(String user, String channel, Vector<String> args) {
        if ((args.size() > 0) && (args.get(0).length() > 0)) {
            // A che canale deve fare il join?
            String newChannel = args.get(0);
            channel = (newChannel.charAt(0) != '#')? "#" + newChannel : channel;
            
            // Chiave del canale?
            String key = ((args.size() > 1) && (args.get(1).length() > 0))? args.get(0) : "";
            
            // Unisciti al canale specificato
            this.joinChannel(channel, user);
            
            // Joined! :)
            this.sendMessage(new com.neroreflex.pizza.Message(channel, user + " I have joined the " + channel + " channel"));
        } else {
            this.sendMessage(new com.neroreflex.pizza.Message(channel, user + " You are supposed to tell me the channel I have to join to :/"));
        }
    }
}
