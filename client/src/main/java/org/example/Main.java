package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
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
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            for (int i = 1; i <= numberOfFiles; i++) {
                List<CompetitorResult> competitorResults = new ArrayList<>();


                var path = resultsFolder + "/" + country + "/" + "ResultC" + "_P" + i + ".txt";
                fileReader.readFileAndAddToList(competitorResults, path, country);

                System.out.println(competitorResults.size());

                ServerMessenger serverMessenger = new ServerMessenger(objectOutputStream, objectInputStream, competitorResults);

                serverMessenger.sendBatchToServer();

                serverMessenger.sendProvisionalPodiumRequest();

                var podiumResults = serverMessenger.receiveProvisionPodiumRequest();

                for (var result: podiumResults) {
                    System.out.println(result.getCountry() +": " + result.getTotalScore());
                }

                try {
                    Thread.sleep(2000); // Sleep for 2 seconds (2000 milliseconds)
                } catch (InterruptedException e) {
                    // Handle interruption if needed
                    e.printStackTrace();
                }

            }
        } catch (IOException | ClassNotFoundException exception) {
            System.out.println(exception);
        }
    }
}