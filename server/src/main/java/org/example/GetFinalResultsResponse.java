package org.example;

import java.util.List;

public class GetFinalResultsResponse implements Request {
    private List<CountryResult> countryResults;
    private List<CompetitorResult> competitorsResultList;

    public GetFinalResultsResponse(List<CountryResult> countryResults, List<CompetitorResult> competitorsResultList) {
        this.countryResults = countryResults;
        this.competitorsResultList = competitorsResultList;
    }

    public List<CountryResult> getCountryResults() {
        return countryResults;
    }

    public void setCountryResults(List<CountryResult> countryResults) {
        this.countryResults = countryResults;
    }

    public List<CompetitorResult> getCompetitorsResultList() {
        return competitorsResultList;
    }

    public void setCompetitorsResultList(List<CompetitorResult> competitorsResultList) {
        this.competitorsResultList = competitorsResultList;
    }
}
