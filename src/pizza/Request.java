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
package pizza;

import java.util.Vector;

/**
 *
 * @author benat
 */
public final class Request {
    
    private final String nameTrancio, botId, channel, user;
    Vector<String> args;
    
    public Request(String botID, String trancio, String channel, String user, Vector<String> args) {
        this.botId = botID;
        this.args = args;
        this.channel = channel;
        this.nameTrancio = trancio;
        this.user = user;
    }
    
    public String getBotID() {
        return this.botId;
    }
    
    public String getTrancio() {
        return this.nameTrancio;
    }
    
    public String getChannel() {
        return this.channel;
    }
    
    public String getUser() {
        return this.user;
    }
    
    public Vector<String> getArguments() {
        return this.args;
    }
}
