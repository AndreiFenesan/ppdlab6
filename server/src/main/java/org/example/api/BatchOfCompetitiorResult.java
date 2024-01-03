package org.example.api;

import java.io.Serializable;
import java.util.List;

public class BatchOfCompetitiorResult implements Serializable {
    private List<CompetitorResult> resultList;

    public BatchOfCompetitiorResult() {
    }

    public List<CompetitorResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<CompetitorResult> resultList) {
        this.resultList = resultList;
    }
}
