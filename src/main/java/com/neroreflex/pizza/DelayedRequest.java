/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neroreflex.pizza;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedRequest implements Delayed  {
    
    private GregorianCalendar endingTime;
    private Request request;
    
    public DelayedRequest(Request req, Duration delay) {
        // Store the request
        this.request = req;
        
        // Store the ending time
        this.endingTime = new GregorianCalendar();
        try {
            this.endingTime.setTimeInMillis(
                    // Controllo l'overflow
                    Math.addExact(this.endingTime.getTimeInMillis(), delay.abs().toMillis())
            );
        } catch (ArithmeticException ex) {
            if (delay.toMinutes() <= (long)Integer.MAX_VALUE)
                this.endingTime.add(GregorianCalendar.MINUTE, (int)delay.toMinutes());
            else if (delay.toHours() <= (long)Integer.MAX_VALUE)
                this.endingTime.add(GregorianCalendar.MINUTE, (int)delay.toHours());
            else
                this.endingTime.add(GregorianCalendar.MINUTE, (int)delay.toDays());
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(endingTime.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return (int)Math.signum((double)this.getDelay(TimeUnit.MILLISECONDS));
    }
    
    public Request GetRequest() {
        return this.request;
    }
}