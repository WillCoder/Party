package com.dora.party.domain;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Cost extends SugarRecord<Cost> implements Serializable {

    private Date date;

    private String name;

    private double value;

    //Must clarify this
    public Cost() {

    }

    public Cost(GregorianCalendar calendar, String name, double value) {
        this.date = calendar.getTime();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getCalendar() {
        return date;
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }

    public void setCalendar(GregorianCalendar calendar) {
        this.date = calendar.getTime();
    }
}
