package com.AlForce.android.runvolution.history;

import java.util.Date;

/**
 * Created by iqbal on 16/02/18.
 */

public class HistoryItem {
    private int id;
    private Date date;
    private int steps;
    private float distance;

    public HistoryItem(int id, Date date, int steps, float distance) {
        this.id = id;
        this.date = date;
        this.steps = steps;
        this.distance = distance;
    }

    public HistoryItem() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
