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

import java.io.IOException;
import org.jibble.pircbot.*;

/**
 * The main bot class
 * 
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class Pizza extends PircBot {

    public Pizza(String botName) {
        //set the name of the bot
        this.setName(botName);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String botName = args[0];
            String botServer = args[1];
            String botChannel = args[2];
        } catch (java.lang.IndexOutOfBoundsException ex) {
            System.out.print("Usage: java -jar Pizza.jar BotName Server:Port #Channel");
        }
        
        // Now start our bot up.
        Pizza bot = new Pizza(args[0]);
        
        // Enable debugging output.
        bot.setVerbose(true);
        
        try {
            // Connect to the IRC server.
            bot.connect("irc.pierotofy.it", 6669);

            // Join the IRC channel.
            bot.joinChannel("#pierotofy.it");
        } catch (IOException ex) {
            
        }
    }
    
}