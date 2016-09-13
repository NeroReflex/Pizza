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
    
    public enum Type {
        Request,
        Automatic
    };
    
    private final String channel, user, message;
    Type requestType;
    
    public Request(Type type, String channel, String user, String msg) {
        this.requestType = type;
        this.message = msg;
        this.channel = channel;
        this.user = user;
    }
    
    public Type GetType() {
        return this.requestType;
    }
    
    public String GetChannel() {
        return this.channel;
    }
    
    public String GetUser() {
        return this.user;
    }
    
    public String GetMessage() {
        return this.message;
    }
    
    public Vector<String> GetBasicParse() {
        String[] params = this.GetMessage().split("([\\s]+)");
        Vector<String> args = new Vector<>();
        
        for (int i = 0; i < params.length; i++)
            if (params[i].length() > 0) args.add(params[i]);
        
        return args;
    }
    
}
