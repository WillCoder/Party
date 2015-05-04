package com.dora.party.domain;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Cost extends SugarRecord<Cost> implements Serializable {

    private Date date;

    private String name;

    private double value;

    //Must clarify this
    public Cost() {

    }

    public Cost(Date date, String name, double value) {
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public String getFormatedDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
