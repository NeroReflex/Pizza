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

public class DelayedObject implements Delayed  {
    private GregorianCalendar startTime;

    public DelayedObject(Duration delay) {
        this.startTime = new GregorianCalendar();
        try {
            this.startTime.setTimeInMillis(
                    Math.addExact(this.startTime.getTimeInMillis(), delay.abs().toMillis())
            );
        } catch (ArithmeticException ex) {
            if (delay.toMinutes() <= (long)Integer.MAX_VALUE)
                this.startTime.add(GregorianCalendar.MINUTE, (int)delay.toMinutes());
            else if (delay.toHours() <= (long)Integer.MAX_VALUE)
                this.startTime.add(GregorianCalendar.MINUTE, (int)delay.toHours());
            else
                this.startTime.add(GregorianCalendar.MINUTE, (int)delay.toDays());
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime.getTimeInMillis() - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return this.startTime.compareTo(((DelayedObject) o).startTime);
    }
}