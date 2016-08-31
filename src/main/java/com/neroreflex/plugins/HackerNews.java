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

import java.util.*;
import java.net.URL;
import java.io.*;
import javax.json.*;
import com.neroreflex.pizza.*;

/**
 * Plugin per notificare le notizie su Hacker News (https://news.ycombinator.com/)
 * La documentazione dell'API è qui https://github.com/HackerNews/API.
 *
 * @author Nitti Gianluca
 */
public final class HackerNews extends Trancio {

    private int interval = 30000; //tempo tra le chiamate all'api in millisecondi

    private final String apiBaseURL = "https://hacker-news.firebaseio.com/v0/";
    private final String apiMaxItem = apiBaseURL + "maxitem.json";
    private final String apiItem = apiBaseURL + "item/";

    private int oldMaxItem = 0;

    private JsonReader jsonApiCall(String endpoint) throws IOException{
        return Json.createReader(new URL(endpoint).openStream());
    }

    private int getMaxItem() throws IOException, NumberFormatException{
        InputStream is = new URL(apiMaxItem).openStream(); //apre una connessione con l'api come InputStream
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String stringValue = s.hasNext() ? s.next() : ""; //legge tutto lo stream
        is.close();
        return Integer.parseInt(stringValue);
    }

    protected String onHelp() {
        return "No commands are available. The plugin will automatically notify new posts on Hacker News.";
    }

    /*protected final void onCall(String user, String channel, Vector<String> args) {

    }*/

    protected final void onPoll(){
        String[] channels = getChannels();
        try{
            int maxItem = getMaxItem();
            if(oldMaxItem != 0){ //controlla che non sia la prima iterazione (altrimenti tenterebbe di scansionare tutto hacker news)
                for(int id = oldMaxItem; id <= maxItem; id++){ //per ogni nuovo elemento
                    //System.out.println(apiItem + id + ".json"); //debug: stampa l'url della richiesta
                    JsonReader rdr = jsonApiCall(apiItem + id + ".json"); //apre la connessione con l'api
                    JsonObject obj = rdr.readObject(); //legge l'oggetto json restituito dall'api
                    rdr.close(); //questa chiamata chiude anche l'InputStream sottostante (http://docs.oracle.com/javaee/7/api/javax/json/JsonReader.html#close--)
                    if(obj.getString("type").equals("story")){ //analizza solo le "stories", che sono i thread
                        for(String chan: channels)
                            sendMessage(new Message(chan, "From Hacker News: " + obj.getString("title") + " " + obj.getString("url")));
                    }
                }
            }
            oldMaxItem = maxItem + 1;
            Thread.sleep(interval);
        } catch(IOException | InterruptedException | NumberFormatException e){
            e.printStackTrace();
        }
    }
}
