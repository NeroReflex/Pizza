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
    private final String apiTopStories = apiBaseURL + "topstories.json";
    private final String apiItem = apiBaseURL + "item/";

    private ArrayList<Integer> topStories = new ArrayList<Integer>();

    protected String onHelp() {
      return "No commands are available. The plugin will automatically notify new posts on Hacker News.";
    }

    private JsonStructure jsonApiCall(String endpoint) throws IOException{
        JsonReader rdr = Json.createReader(new URL(endpoint).openStream());
        JsonStructure result = rdr.read();
        rdr.close(); //questa chiamata chiude anche l'InputStream sottostante  (http://docs.oracle.com/javaee/7/api/javax/json/JsonReader.html#close--)
        return result;
    }

    //Notifica nella chat la notizia con l'id specificato.
    private void notifyNews(int id) throws IOException{
        JsonObject obj = (JsonObject)jsonApiCall(apiItem + id + ".json"); //apre la connessione con l'api
        if(obj.getString("type").equals("story")){ //controlla che sia una story (un thread) e non un comento, poll, ecc.
            String[] channels = getChannels();
            for(String chan: channels)
                sendMessage(new Message(chan, "From Hacker News: " + obj.getString("title") + " " + obj.getString("url")));
        }
    }

    private List<Integer> getTopStories() throws IOException{
        JsonArray apiResponse = (JsonArray)jsonApiCall(apiTopStories); //ottiene l'elenco degli id delle prime 500 top stories (quelle in home)
        List<Integer> result = new ArrayList<Integer>();
        for(JsonValue v: apiResponse)
            result.add(((JsonNumber)v).intValue());
        return result;
    }

    /*protected final void onCall(String user, String channel, Vector<String> args) {

    }*/

    protected final void onPoll(){
        try{
            boolean firstTime = topStories.size() == 0;
            List<Integer> newTopStories = getTopStories();
            for(Integer id: newTopStories){ //per ogni elemento restituito dall'api
                if(!topStories.contains(id)){ //se non è nell'elenco delle già notificate
                    if(!firstTime)
                        notifyNews(id); //la notifica
                    topStories.add(id); //e la aggiunge all'elenco
                }
            }
            //pulisce l'elenco dalle notizie vecchie che non sono più segnalate dall'API
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            for(Integer i: topStories)
                if(!newTopStories.contains(i))
                    toRemove.add(i);
            topStories.removeAll(toRemove);
            Thread.sleep(interval);
        } catch(IOException | InterruptedException | NumberFormatException e){
            e.printStackTrace();
        }
    }
}
