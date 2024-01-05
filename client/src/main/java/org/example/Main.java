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
        int numberOfFiles = 2;

        FileReader fileReader = new FileReader();

        try (Socket clientSocket = new Socket("localhost", 9998)) {
            for (int i = 1; i <= numberOfFiles; i++) {
                List<CompetitorResult> competitorResults = new ArrayList<>();


                var path = resultsFolder + "/" + country + "/" + "ResultC" + "_P" + i + ".txt";
                fileReader.readFileAndAddToList(competitorResults, path, country);

                System.out.println(competitorResults.size());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ServerMessenger serverMessenger = new ServerMessenger(objectOutputStream, competitorResults);

                serverMessenger.sendBatchToServer();

                serverMessenger.sendProvisionalPodiumRequest();

                try {
                    Thread.sleep(2000); // Sleep for 2 seconds (2000 milliseconds)
                } catch (InterruptedException e) {
                    // Handle interruption if needed
                    e.printStackTrace();
                }

            }
        } catch (IOException exception) {
            System.out.println(exception);
        }
    }
}