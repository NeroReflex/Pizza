/*
 *    Pizza IRC Bot (for pierotofy.it community)
 *    Copyright (C) 2016 Benato Denis
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
package plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import pizza.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * Un plugin per l'ora attuale. Gran parte del codice e' preso da:
 * http://www.rgagnon.com/javadetails/java-0589.html
 * 
 * @author Benato Denis
 */
public final class Time extends pizza.Trancio {
    public static final int ATOMICTIME_PORT = 13;

    public static final String ATOMICTIME_SERVER = "129.6.15.30";
    
    /*
    ref : http://www.bldrdoc.gov/doc-tour/atomic_clock.html

                       49825 95-04-18 22:24:11 50 0 0 50.0 UTC(NIST) *

                       |     |        |     | | |  |      |      |
    These are the last +     |        |     | | |  |      |      |
    five digits of the       |        |     | | |  |      |      |
    Modified Julian Date     |        |     | | |  |      |      |
                             |        |     | | |  |      |      |
    Year, Month and Day <----+        |     | | |  |      |      |
                                      |     | | |  |      |      |
    Hour, minute, and second of the <-+     | | |  |      |      |
    current UTC at Greenwich.               | | |  |      |      |
                                            | | |  |      |      |
    DST - Daylight Savings Time code <------+ | |  |      |      |
    00 means standard time(ST), 50 means DST  | |  |      |      |
    99 to 51 = Now on ST, goto DST when local | |  |      |      |
    time is 2:00am, and the count is 51.      | |  |      |      |
    49 to 01 = Now on DST, goto ST when local | |  |      |      |
    time is 2:00am, and the count is 01.      | |  |      |      |
                                              | |  |      |      |
    Leap second flag is set to "1" when <-----+ |  |      |      |
    a leap second will be added on the last     |  |      |      |
    day of the current UTC month.  A value of   |  |      |      |
    "2" indicates the removal of a leap second. |  |      |      |
                                                |  |      |      |
    Health Flag.  The normal value of this    <-+  |      |      |
    flag is 0.  Positive values mean there may     |      |      |
    be an error with the transmitted time.         |      |      |
                                                   |      |      |
    The number of milliseconds ACTS is advancing <-+      |      |
    the time stamp, to account for network lag.           |      |
                                                          |      |
    Coordinated Universal Time from the National <--------+      |
    Institute of Standards & Technology.                         |
                                                                 |
    The instant the "*" appears, is the exact time. <------------+
    */
    public final static GregorianCalendar getAtomicTime() throws IOException{
        BufferedReader in = null;
        Socket conn = null;

        try {
            conn = new Socket(ATOMICTIME_SERVER, ATOMICTIME_PORT);

            in = new BufferedReader
              (new InputStreamReader(conn.getInputStream()));

            String atomicTime;
            while (true) {
               if ( (atomicTime = in.readLine()).indexOf("*") > -1) {
                  break;
               }
            }
            //System.out.println("DEBUG 1 : " + atomicTime);
            String[] fields = atomicTime.split(" ");
            GregorianCalendar calendar = new GregorianCalendar();

            String[] date = fields[1].split("-");
            calendar.set(Calendar.YEAR, 2000 +  Integer.parseInt(date[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
            calendar.set(Calendar.DATE, Integer.parseInt(date[2]));

            // deals with the timezone and the daylight-saving-time (you may need to adjust this)
            // here i'm using "EST" for Eastern Standart Time (to support Daylight Saving Time)
            TimeZone tz = TimeZone.getTimeZone("EST"); // or .getDefault()
            int gmt = (tz.getRawOffset() + tz.getDSTSavings()) / 3600000;
            //System.out.println("DEBUG 2 : " + gmt);

            String[] time = fields[2].split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]) + gmt);
            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
            calendar.set(Calendar.SECOND, Integer.parseInt(time[2]));
            return calendar;
        } catch (IOException e){
           throw e;
        } finally {
           if (in != null) { in.close();   }
           if (conn != null) { conn.close();   }
        }
    }
    
    protected final void onCall(String user, String channel, Vector<String> args) {
        // Stabilisci la zona del mondo richiesta
        String timezone = (args.size() > 0)? args.get(0) : "Europe/Rome";
        
        try {
            // Ottieni l'ora attuale
            GregorianCalendar cal = getAtomicTime();
            
            GregorianCalendar correctTimeZone = new GregorianCalendar(TimeZone.getTimeZone(timezone));
            correctTimeZone.setTimeInMillis(cal.getTimeInMillis());
            
            // Imposta il formato di visualizzazione
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

            // Metti in coda la risposta
            this.sendMessage(new Message(channel, user + " exact time: " + sdf.format(correctTimeZone.getTime())));
        } catch (Exception ex) {
            this.sendMessage(new Message(channel, user + " error occurred while retrieving the exact time for the " + timezone + ":("));
        }
    }
    
}
