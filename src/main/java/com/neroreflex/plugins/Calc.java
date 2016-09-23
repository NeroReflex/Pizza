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

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
import com.neroreflex.pizza.*;
import com.github.gianlucanitti.javaexpreval.*;

/**
 * Un plugin per risolvere espressioni matematiche
 *
 * @author Nitti Gianluca
 */
public final class Calc extends Trancio {

    private class IrcWriter extends Writer{
        private String chan;
        private String buffer;

        public IrcWriter(String chan){
            this.chan = chan;
            buffer = "";
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for(int i = off; i < len; i++)
                buffer += cbuf[i];
        }

        @Override
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {
            if(buffer.length() != 0)
                for(String s: buffer.split("[\\r\\n]+"))
                    sendMessage(new Message(chan, s));
            buffer = "";
        }
    }

    protected String onHelp() {
        return "[verbose] <expr> - where expr is your expression; if verbose is present, evaluation steps will be sent to you privately.";
    }
    
    @Override
    public final void onCall(Request req) {
        String user = req.getUser(), channel = req.getChannel();
        Vector<String> args = req.getBasicParse();
        String result;
        IrcWriter w = new IrcWriter(user); //un eventuale log viene mandato in privato all'utente che ha richiesto il calcolo
        try {
            Expression expr;
            switch(args.get(0).toLowerCase()){
                case "verbose": //risolve l'espressione passata come argomento scrivendo i passaggi
                    String exprStr = "";
                    for(int i = 1; i < args.size(); i++) //concatena tutto quello che c'è dopo il comando verbose, in modo da accettare espressioni che contangono spazi
                        exprStr += args.get(i); //l'input esatto dell'utente si otterrebbe con " " + args.get(i) ma tanto gli spazi non fanno differenza per il parser
                    expr = Expression.parse(exprStr, w);
                    result = "res = " + expr.eval(w);
                    break;
                //qui verranno aggiunti nuovi comandi con le prossime versioni della libreria, ad esempio per definire variabili o funzioni
                default:
                    //se il primo argomento non corrisponde ad un comando, allora interpreta tutto come una singola espressione
                    expr = Expression.parse(req.getMessage());
                    result = "res = " + expr.eval();
            }
        }catch(ExpressionException e){
            result = e.getMessage();
        }
        sendMessage(new Message(channel, user + ", " + result));
        try {
            w.close(); //manda l'eventuale log all'utente
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
