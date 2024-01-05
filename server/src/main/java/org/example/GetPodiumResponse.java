package org.example;

import java.util.List;

public class GetPodiumResponse implements Request {
    private List<CountryResult> results;

    public GetPodiumResponse(List<CountryResult> results) {
        this.results = results;
    }

    public List<CountryResult> getResults() {
        return results;
    }

    public void setResults(List<CountryResult> results) {
        this.results = results;
    }
}
