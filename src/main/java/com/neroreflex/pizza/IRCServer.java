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
package com.neroreflex.pizza;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IRCServer {
    private String defaultServer = "irc.freenode.net";
    private int defaultPort = 6667;
    
    private String server;
    private int port;
    
    public IRCServer(String botServer) {
        // Prova a estrarre server e porta
        Pattern serverRegex = Pattern.compile("([\\.\\w]+)(\\:(\\d+))?");
        Matcher serverMatcher = serverRegex.matcher(botServer);
        if (!serverMatcher.matches()) {
            this.server = defaultServer;
            this.port = defaultPort;
        } else {
            this.server = serverMatcher.group(1);
            this.port = (serverMatcher.group(3).length() > 0)? defaultPort : Integer.parseInt(serverMatcher.group(3).substring(1));
        }
    }
    
    public String getAddress() {
        return this.server;
    }
    
    public int getPort() {
        return this.port;
    }
}
