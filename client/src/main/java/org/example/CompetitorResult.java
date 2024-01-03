package org.example;

import java.io.Serializable;

public class CompetitorResult implements Serializable {
    private int id;
    private int score;
    private String country;

    public CompetitorResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}