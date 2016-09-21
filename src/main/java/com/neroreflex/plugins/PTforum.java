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
import java.time.Duration;

/**
 * Plugin per notificare nuovi messaggi sul forum di pierotofy.it
 *
 * @author Nitti Gianluca
 */
public final class PTforum extends Trancio {

    String apiEndpoint = "http://www.pierotofy.it/pages/extras/forum/api/last_topics.php";

    HashMap<String, Integer> topics = new HashMap<>(); //chiave=url, valore=numero risposte

    @Override
    public final void onPoll(){
        try {
            // Ottengo la lista di canali in cui informare gli utenti
            String[] channels = this.getChannels();

            // Mi connetto all'endpoint per le API del sito pierotofy.it
            InputStream is = new URL(apiEndpoint).openStream();
            JsonArray topicList = Json.createReader(is).readArray();
            boolean firstFetch = (topics.size() == 0);
            for(JsonValue v: topicList){
                JsonObject obj = (JsonObject)v;
                Integer oldReplies = topics.get(obj.getString("url"));

                if(oldReplies == null){
                    // Se non è già in elenco, lo aggiungo...
                    topics.put(obj.getString("url"), obj.getInt("replies"));
                    if(!firstFetch)
                        for (String chan : channels) {
                            sendMessage(new Message(chan, "New message(s) on topic \"" + obj.getString("subject") + "\": http://pierotofy.it" + obj.getString("url")));
                    }
                } else if(obj.getInt("replies") > oldReplies) {
                    // Se il numero di risposte è maggiore, allora ci sono stati nuovi post
                    for (String chan : channels) {
                        sendMessage(new Message(chan, "New message(s) on topic \"" + obj.getString("subject") + "\": http://pierotofy.it" + obj.getString("url")));
                    }
                }
                //Aggiorna il numero di risposte
                topics.put(obj.getString("url"), obj.getInt("replies"));
            }
            ArrayList<String> topicsToRemove = new ArrayList<>();
            // Pulisce la hashmap dai vecchi topic (che non sono più nella risposta dell'api)
            for(String url: topics.keySet()){
                boolean toRemove = true;
                for(JsonValue v: topicList){
                    if(((JsonObject)v).getString("url").equals(url)){
                        toRemove = false;
                        break;
                    }
                }
                if(toRemove)
                    topicsToRemove.add(url);
            }
            for(String s: topicsToRemove)
                topics.remove(s);
        } catch(IOException e){
            e.printStackTrace();
            //sendMessage(new Message(chan, "Attenzione: impossibile recuperare gli aggiornamenti dal forum."));
        }
    }
    
    @Override
    protected final void onInitialize() {
        // Un poll ogni 2.5 secondi
        this.delay = Duration.ofMinutes(1);
    }
}
