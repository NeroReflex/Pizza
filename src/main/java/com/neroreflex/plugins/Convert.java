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
public class Convert extends Trancio {

    protected String onHelp() {
        return "<op> <number> - where op is the name of two notations separed by a 2, like bin2dec, oct2bin, hex2bin, an so on....";
    }
    
    private Map<String, Integer> baseList;

    protected final void onInitialize(){
        baseList = new HashMap<>();
        baseList.put("bin", 2);
        baseList.put("oct", 8);
        baseList.put("dec", 10);
        baseList.put("hex", 16);
    }

    @Override
    protected final void onCall(String user, String channel, Vector<String> args) {
        if(args.size() != 2){
            sendMessage(new Message(channel, user + " sono necessari due argomenti: conversione (es. hex2bin) e valore da convertire."));
            return;
        }
        Message wrongConversionMsg = new Message(channel, user + " nome della conversione errato. Deve essere formato da base sorgente + '2' + destinazione. Le basi supportate sono bin, oct, dec, hex. Ad esempio bin2hex, hex2dec, ecc.");
        if(args.get(0).length() != 7){
            sendMessage(wrongConversionMsg);
            return;
        }
        String srcBaseName = args.get(0).substring(0, 3);
        String destBaseName = args.get(0).substring(4);
        Integer srcBase = baseList.get(srcBaseName);
        Integer destBase = baseList.get(destBaseName);
        String src = args.get(1);
        if(args.get(0).charAt(3) != '2' || srcBase == null || destBase == null)
            sendMessage(wrongConversionMsg);
        else{
            try{
                String dest = Integer.toString(Integer.parseInt(src, srcBase), destBase);
                sendMessage(new Message(channel, user + " " + src + " (" + srcBaseName + ") = " + dest + " (" + destBaseName + ")"));
            }catch(NumberFormatException e){
                sendMessage(new Message(channel, user + " formato del numero errato :( :\"" + src + "\" non è un numero in base " + srcBase));
            }
        }
    }
}
