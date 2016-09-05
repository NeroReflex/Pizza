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

    protected String onHelp() {
      return "Call this plugin without arguments to show the first 5 top news";
    }

    private synchronized void sendHackerNews(String ... channels) {
        try {
            Vector<String> messages = new Vector<String>(5);

            JsonArray apiResponse = (JsonArray)jsonApiCall(apiTopStories); //ottiene l'elenco degli id delle top stories, max 500 (quelle in home)
            for(int i = 0; i < 5; i++) {
                int id = apiResponse.getInt(i, -1); //id della i-esima notizia, o -1 se per qualche motivo la risposta dell'api fosse più corta
                if(i == -1) break; //esce se l'id non è stato trovato
                JsonObject obj = (JsonObject)jsonApiCall(apiItem + id + ".json"); //recupera i dettagli della notizia
                int score = obj.getInt("score");
                String title = obj.getString("title");
                String url = obj.getString("url", url = defaultURL + id); //se non ha il campo url il 2° argomento viene usato come default (url della pagina dei commenti)

                messages.add( (i+1) + ". " + title + " " + url + " (" + score + ")");
            }

            for(String chan: channels) {
                sendMessage(new Message(chan, "From Hacker News: "));
                for (String m : messages) {
                    sendMessage(new Message(chan, m));
                }
            }
        } catch(IOException | NumberFormatException e) {
             e.printStackTrace();
        }
    }

    protected final void onCall(String user, String channel, Vector<String> args) {
        this.sendHackerNews(channel);
    }

    protected final void onInitialize() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        int currHour = cal.get(Calendar.HOUR_OF_DAY);
        //le notifiche vengono inviate alle 2, 8, 14, 20; la formula determina il prossimo orario più vicino tra quelli.
        int startHour = ((int)Math.floor(((currHour - 2)/6.0)) + 1)* 6 + 2;
        cal.set(Calendar.HOUR_OF_DAY, startHour);
        cal.set(Calendar.MINUTE, 0); //ora esatta (altrimenti mantiene minuti e secondi dell'istande della chiamata a Calendar.getInstance)
        cal.set(Calendar.SECOND, 0);

        //System.out.println(cal.getTime().toString()); //Debug: stampa l'ora della prima notifica

        // schedule every six hours
        new Timer().scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    HackerNews.this.sendHackerNews(HackerNews.this.getChannels());
                }
            },
            cal.getTime(),
            Duration.ofHours(6).toMillis()
        );
    }
}
