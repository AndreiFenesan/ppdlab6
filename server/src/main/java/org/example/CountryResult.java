package org.example;

import java.io.Serializable;

public class CountryResult implements Serializable {
    private String country;
    private Integer totalScore;

    public CountryResult(String country, Integer totalScore) {
        this.country = country;
        this.totalScore = totalScore;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
}
