package com.dora.party.domain;

import com.orm.SugarRecord;

import java.io.Serializable;

public class Donation extends SugarRecord<Donation> implements Serializable {

    private String name;

    private double value;

    //Must clarify this
    public Donation() {

    }

    public Donation(String name, double value) {
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
}
