package org.example;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMessenger {

    ObjectOutputStream objectOutputStream;
    List<CompetitorResult> competitorResults;

    public ServerMessenger(ObjectOutputStream objectOutputStream, List<CompetitorResult> competitorResults)
    {
        this.objectOutputStream = objectOutputStream;
        this.competitorResults = competitorResults;
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
}
