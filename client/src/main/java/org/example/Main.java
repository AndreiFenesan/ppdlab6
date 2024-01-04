package org.example;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        var resultsFolder = args[0];
        var country = args[1];
        var deltaX = Integer.parseInt(args[2]);

        FileReader fileReader = new FileReader();

        List<CompetitorResult> competitorResults = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            var path = resultsFolder + "/" + country + "/" + "ResultC" + "_P" + i + ".txt";
            fileReader.readFileAndAddToList(competitorResults, path, country);
        }

        System.out.println(competitorResults.size());

        try (Socket clientSocket = new Socket("localhost", 9998)) {
            BatchOfCompetitiorResult batchOfCompetitorResults = new BatchOfCompetitiorResult();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            int noTimes = competitorResults.size() % 20 == 0 ?
                    competitorResults.size() / 20 : competitorResults.size() / 20 + 1;
            for (int i = 0; i < noTimes; i++) {
                List<CompetitorResult> resultList =
                        competitorResults.subList(i * 20,
                                Math.min((i + 1) * 20, competitorResults.size()));

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

            //sends empty list to know it ended
            List<CompetitorResult> resultList = new ArrayList<>();
            batchOfCompetitorResults.setResultList(resultList);

            objectOutputStream.writeObject(batchOfCompetitorResults);
            objectOutputStream.flush();
            try {
                Thread.sleep(2000); // Sleep for 2 seconds (2000 milliseconds)
            } catch (InterruptedException e) {
                // Handle interruption if needed
                e.printStackTrace();
            }

        } catch (IOException exception) {
            System.out.println(exception);
        }
    }
}