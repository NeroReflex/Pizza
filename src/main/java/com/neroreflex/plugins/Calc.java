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
 * Un plugin per eseguire semplici operazioni matematiche
 *
 * @author Nitti Gianluca
 */
public final class Calc extends RequestTrancio {
    protected String onHelp() {
        return "<number1> <operation> <number2> - where operation can either be +, - * or /";
    }
    
    protected final void onCall(String user, String channel, Vector<String> args) {
        if(args.size() != 3)
            this.sendMessage(new Message(channel, user + " Invalid arguments. Look at the plugin help to understand how to invoke cald"));
        else{
            double a;
            double b;
            try{
                a = Double.parseDouble(args.get(0));
                b = Double.parseDouble(args.get(2));
            }catch(NumberFormatException e){
                this.sendMessage(new Message(channel, user + " the given operands aren't numbers"));
                return;
            }
            char op = args.get(1).charAt(0);
            double r = 0;
            switch(op){
                case '+':
                    r = a + b;
                    break;
                case '-':
                    r = a - b;
                    break;
                case '*':
                    r = a * b;
                    break;
                case '/':
                    r = a / b;
                    break;
                default:
                    sendMessage(new Message(channel, user + " unknown operator \"" + args.get(1) + "\" :(. Only +,-,*,/ are supported"));
                    return;
            }
            sendMessage(new Message(channel, user + " " + a + op + b + "=" + r));
        }
    }
}
