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
import java.io.StringReader;
import java.io.Writer;
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

    private InteractiveExpressionContext ctx;

    @Override
    protected void onInitialize(){
        ctx = new InteractiveExpressionContext();
        ctx.setHelpVerbose(true);
        //Disabilita i comandi help e exit
        ctx.setCommands("context", "clear", "", "");
    }

    protected String onHelp() {
        return "[verbose] <expression>|<assignment>|<command> - where expression can be an expression containing numbers, variables, parenthesis and binary operators;"
                + " assignment is in the form <variable>=<expression>, or <variable>= to delete <variable>;"
                + " command can be \"context\" (print all defined variables) or \"clear\" (delete all the defined variables)."
                + " if \"verbose\" is specified before any other argument, detailed parsing/evaluation steps will be sent to you as PM.";
    }
    
    @Override
    public final void onCall(String channel, String user, String msg) {
        Writer chWriter = new IrcWriter(channel);
        Writer userWriter = NullOutputStream.getWriter();

        // Un eventuale log viene mandato in privato all'utente che ha richiesto il calcolo
        if(msg.startsWith(" verbose ")) {
            userWriter = new IrcWriter(user);
            msg = msg.substring(9);
        }else if(msg.startsWith(" ")){
            //Rimuovi lo spazio in modo che i comandi vengano riconosciuti
            msg = msg.substring(1);
        }
        //Non mi interessa l'autoFlush perchè tanto IrcWriter invia solo quando lo si chiude.
        ctx.setOutputWriter(chWriter, false);
        ctx.setErrorOutputWriter(chWriter, false);
        ctx.setVerboseOutputWriter(userWriter, false);
        ctx.setInputReader(new StringReader(msg));
        try {
            ctx.update();
            chWriter.close();
            userWriter.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
