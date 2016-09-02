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
import java.time.Duration;
import javax.json.*;
import com.neroreflex.pizza.*;

/**
 * Plugin per notificare le notizie su Hacker News (https://news.ycombinator.com/)
 * La documentazione dell'API è qui https://github.com/HackerNews/API.
 *
 * @author Nitti Gianluca
 */
public final class HackerNews extends Trancio {

    private long interval = Duration.ofHours(6).toMillis(); // (= 6h) tempo tra le chiamate all'api in millisecondi

    private final String apiBaseURL = "https://hacker-news.firebaseio.com/v0/";
    private final String apiTopStories = apiBaseURL + "topstories.json";
    private final String apiItem = apiBaseURL + "item/";
    private final String defaultURL = "https://news.ycombinator.com/item?id=";

    private JsonStructure jsonApiCall(String endpoint) throws IOException{
      JsonReader rdr = Json.createReader(new URL(endpoint).openStream());
      JsonStructure result = rdr.read();
      rdr.close(); //questa chiamata chiude anche l'InputStream sottostante  (http://docs.oracle.com/javaee/7/api/javax/json/JsonReader.html#close--)
      return result;
    }

    private class NewsEntry implements Comparable<NewsEntry>{
        private int score;
        private String title;
        private String url;
        public NewsEntry(int id) throws IOException{
            JsonObject obj = (JsonObject)jsonApiCall(apiItem + id + ".json"); //apre la connessione con l'api
            score = obj.getInt("score");
            title = obj.getString("title");
            try{
                url = obj.getString("url");
            }catch(NullPointerException e){
                url = defaultURL + id;
            }
        }
        public int compareTo(NewsEntry other){
            return other.score - score;
        }
        public String toString(){
            return "From Hacker News: " + title + " " + url + " (" + score + ")";
        }
    }

    protected String onHelp() {
      return "No commands are available. The plugin will automatically notify new posts on Hacker News.";
    }

    /*protected final void onCall(String user, String channel, Vector<String> args) {
        /*Qui si potrebbe implementare un comando per far printare al bot le notizie anche se non è ancora trascorso l'interval dall'ultima volta
        tuttavia per ora non credo sia possibile perchè il thread rimane bloccato (chiamata a thread.sleep in onPoll). Vedremo in seguito
    }*/

    protected final void onPoll(){
        try{
            JsonArray apiResponse = (JsonArray)jsonApiCall(apiTopStories); //ottiene l'elenco degli id delle top stories, max 500 (quelle in home)
            List<NewsEntry> l = new ArrayList<NewsEntry>();
            for(JsonValue v: apiResponse) //riempie la lista
                l.add(new NewsEntry(((JsonNumber)v).intValue()));
            Collections.sort(l); //la ordina in base al punteggio
            String[] channels = getChannels();
            for(int i = 0; i < 5; i++) //notifica nella chat le migliori 5
                for(String chan: channels)
                    sendMessage(new Message(chan, l.get(i).toString()));
            Thread.sleep(interval);
        } catch(IOException | InterruptedException | NumberFormatException e){
            e.printStackTrace();
        }
    }
}
