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
                var path = resultsFolder + "\\" + country + "\\" + "ResultC" + "_P" + i + ".txt";
                fileReader.readFileAndAddToList(competitorResults, path, country);
            }

        System.out.println(competitorResults.size());

        try(Socket clientSocket = new Socket("localhost", 9998)) {
            BatchOfCompetitiorResult batchOfCompetitorResults = new BatchOfCompetitiorResult();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            for(int i = 0; i < competitorResults.size()/20; i++) {
                int counter = 0;
                List<CompetitorResult> resultList = new ArrayList<>();

                while(counter < 20 && !competitorResults.isEmpty()){
                    resultList.add(competitorResults.get(0));
                    competitorResults.remove(0);
                    counter++;
                }

                batchOfCompetitorResults.setResultList(resultList);

                objectOutputStream.writeObject(batchOfCompetitorResults);
                objectOutputStream.flush();

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

        }catch (IOException exception) {
            System.out.println(exception);
        }
    }
}