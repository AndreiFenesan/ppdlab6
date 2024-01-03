package org.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Queue;



public class Main {

    private static void readFileAndAddToList(Queue<Node> nodes, String fileName) {
        var filePath = Paths.get(fileName);
        try (var buff = Files.newBufferedReader(filePath)) {
            buff.lines()
                    .forEach(line -> {
                        var data = line.split(" ");
                        Node node = new Node(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                        nodes.add(node);
                    });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        var resultsFolder = args[0];

        Queue<Node> nodes = new ArrayDeque<>();
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 10; j++) {
                var path = resultsFolder + "ResultC" + i + "_P" + j + ".txt";
                readFileAndAddToList(nodes, path);
            }
        }
    }
}