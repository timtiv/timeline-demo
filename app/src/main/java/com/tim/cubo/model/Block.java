package com.tim.cubo.model;

import java.util.List;

public class Block {

    private long min;
    private long hour;
    private List<Alert> alertList;

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }

    public List<Alert> getAlertList() {
        return alertList;
    }

    public void setAlertList(List<Alert> alertList) {
        this.alertList = alertList;
    }
}
