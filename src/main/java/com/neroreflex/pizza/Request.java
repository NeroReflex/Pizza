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

import java.util.Vector;

/**
 * Identifica una richiesta che verrà "esaudita" da un plugin della
 * chat in un diverso thread.
 * 
 * @author Benato Denis
 */
public final class Request {
    
    private final String channel, user, message;
    
    public Request(String channel, String user, String msg) {
        this.message = msg;
        this.channel = channel;
        this.user = user;
    }
    
    public String getChannel() {
        return this.channel;
    }
    
    public String getUser() {
        return this.user;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public Vector<String> getBasicParse() {
        String[] params = this.getMessage().split("([\\s]+)");
        Vector<String> args = new Vector<>();
        
        for (int i = 0; i < params.length; i++)
            if (params[i].length() > 0) args.add(params[i]);
        
        return args;
    }
    
}
