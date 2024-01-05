package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMessenger {

    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public void setCompetitorResults(List<CompetitorResult> competitorResults) {
        this.competitorResults = competitorResults;
    }

    List<CompetitorResult> competitorResults;

    public ServerMessenger(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    public void sendBatchToServer() throws IOException {
        BatchOfCompetitiorResult batchOfCompetitorResults = new BatchOfCompetitiorResult();

        int noTimes = competitorResults.size() % 20 == 0 ?
                competitorResults.size() / 20 : competitorResults.size() / 20 + 1;

        for (int j = 0; j < noTimes; j++) {
            List<CompetitorResult> resultList =
                    competitorResults.subList(j * 20,
                            Math.min((j + 1) * 20, competitorResults.size()));

            ArrayList<CompetitorResult> toSend = new ArrayList<>(resultList);
            batchOfCompetitorResults.setResultList(toSend);
            System.out.println("Sending batch of: " + resultList.size());
            objectOutputStream.writeObject(batchOfCompetitorResults);
            objectOutputStream.flush();
            objectOutputStream.reset();
            System.out.println("Sent batch of: " + resultList.size() + " success");

            try {
                Thread.sleep(2000); // Sleep for 2 seconds (2000 milliseconds)
            } catch (InterruptedException e) {
                // Handle interruption if needed
                e.printStackTrace();
            }
        }

    }

    public void sendProvisionalPodiumRequest() throws IOException {
        System.out.println("Sending request for podium");
        objectOutputStream.writeObject(new GetPodiumRequest());
        objectOutputStream.flush();
        objectOutputStream.reset();
        System.out.println("Sent request for podium success");
    }

    public List<CountryResult> receiveProvisionPodiumRequest() throws IOException, ClassNotFoundException {

        System.out.println("Receiving response for podium");
        Object podiumResponse;
        podiumResponse = objectInputStream.readObject();
        System.out.println("Received response for podium success");

        return ((GetPodiumResponse) podiumResponse).getResults();
    }

    public void sendFinalResultsRequest() throws IOException {
        System.out.println("Sending request for final results");
        objectOutputStream.writeObject(new GetFinalResultsRequests());
        objectOutputStream.flush();
        objectOutputStream.reset();
        System.out.println("Sent request for final results");
    }

    public GetFinalResultsResponse receiveFinalResultsResponse() throws IOException, ClassNotFoundException {
        System.out.println("Receiving response for podium");
        Object finalResultsResponse;
        finalResultsResponse = objectInputStream.readObject();
        System.out.println("Received response for podium success");

        return ((GetFinalResultsResponse) finalResultsResponse);
    }

    public void sendDoneRequest() throws IOException {
        System.out.println("Sending request for ending communication");
        objectOutputStream.writeObject(new DoneRequest());
        objectOutputStream.flush();
        objectOutputStream.reset();
        System.out.println("Sent request for ending communication");
    }
}
