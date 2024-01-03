package org.example;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


public class Main {

    private static void readFileAndAddToList(List<CompetitorResult> nodes, String fileName, String country) {
        var filePath = Paths.get(fileName);
        try (var buff = Files.newBufferedReader(filePath)) {
            buff.lines()
                    .forEach(line -> {
                        var data = line.split(" ");

                        CompetitorResult node = new CompetitorResult();
                        node.setId(Integer.parseInt(data[0]));
                        node.setScore(Integer.parseInt(data[1]));
                        node.setCountry(country);

                        nodes.add(node);
                    });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        var resultsFolder = args[0];
        var country = args[1];

        try{
            Socket mySocket = new Socket("localhost", 9999);
            List<CompetitorResult> myList = new ArrayList<>();
            CompetitorResult node = new CompetitorResult();
            node.setId(1);
            node.setScore(10);
            node.setCountry("Romania");

            CompetitorResult node2 = new CompetitorResult();
            node.setId(2);
            node.setScore(30);
            node.setCountry("Anglia");

            myList.add(node);
            myList.add(node2);

            BatchOfCompetitiorResult myBatch = new BatchOfCompetitiorResult();
            myBatch.setResultList(myList);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(mySocket.getOutputStream());
            objectOutputStream.writeObject(myBatch);
            objectOutputStream.flush();
        }
        catch (IOException exception) {
            System.out.println(exception);
        }


        List<CompetitorResult> nodes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
                var path = resultsFolder + "ResultC" + "_P" + i + ".txt";
                readFileAndAddToList(nodes, path, country);
            }
        }
}