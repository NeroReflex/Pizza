/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neroreflex.pizza;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedObject implements Delayed  {
    private String data;
    private long startTime;

    public DelayedObject(String data, long delay) {
        this.data = data;
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((DelayedObject) o).startTime) {
            return -1;
        }
        if (this.startTime > ((DelayedObject) o).startTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "{" +
               "data='" + data + "'" +
               ", startTime=" + startTime +
               "}";
    }
}