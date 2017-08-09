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
import java.util.*;

/**
 * Un plugin per convertire tra basi numeriche comunemente usate
 *
 * @author Nitti Gianluca
 */
public final class Convert extends Trancio {

    protected String onHelp() {
        return "<op> <number> - where op is the name of two notations separed by a 2, like bin2dec, oct2bin, hex2bin, an so on....";
    }
    
    private Map<String, Integer> baseList;

    @Override
    protected final void onInitialize(){
        baseList = new HashMap<>();
        baseList.put("bin", 2);
        baseList.put("oct", 8);
        baseList.put("dec", 10);
        baseList.put("hex", 16);
    }

    @Override
    public final void onCall(String channel, String user, String msg) {
        // Che bello reinventare la ruota!
        String[] params = msg.split("([\\s]+)");
        Vector<String> args = new Vector<>();
        for (int i = 0; i < params.length; i++)
            if (params[i].length() > 0) args.add(params[i]);
        
        if(args.size() != 2){
            sendMessage(new Message(channel, user + " wrong arguments number."));
            return;
        }
        
        // Ottieni i nomi delle basi da usare per la conversione
        String srcBaseName = args.get(0).substring(0, 3);
        String destBaseName = args.get(0).substring(4);
        
        // Controlla i nomi delle basi da convertire
        if ((args.get(0).length() != 7) || (args.get(0).charAt(3) != '2') || (!baseList.containsKey(srcBaseName)) || (!baseList.containsKey(destBaseName))) {
            sendMessage(
                    new Message(channel, user + " wrong conversion name. Allowed names are: <src>2<dst> where <src> and <dst> are bin, hex, dec and oct")
            );
            return;
        }
        
        // Ottengo il numero di caratteri nelle basi
        Integer srcBase = baseList.get(srcBaseName);
        Integer destBase = baseList.get(destBaseName);
        
        // Salvo il numero da convertire
        String src = args.get(1);
        
        try {
            String dest = Integer.toString(Integer.parseInt(src, srcBase), destBase).toUpperCase();
            if ((srcBase == 16) && (destBase == 2)) {
                String zeroes = "";
                int bitsNumber = (src.length() * 4) - dest.length();
                for (int i = 0; i < bitsNumber; i++) 
                    zeroes += "0";
                
                dest = insertPeriodically(zeroes + dest, " ", 4);
            }
            this.sendMessage(new Message(channel, user + " " + src + " (" + srcBaseName + ") = " + dest + " (" + destBaseName + ")"));
        } catch(NumberFormatException e){
            this.sendMessage(new Message(channel, user + " wrong format for number " + src + ": not a base " + srcBase + " number."));
        }
    }
    
    public static String insertPeriodically(String text, String insert, int period)
    {
        StringBuilder builder = new StringBuilder(
             text.length() + insert.length() * (text.length()/period)+1);

        int index = 0;
        String prefix = "";
        while (index < text.length())
        {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index, 
                Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }
    
}