package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static void readFileAndAddToQueue(MyBlockingQueue readNodes, String fileName, String country) throws IOException {
        var filePath = Paths.get(fileName);
        try (var buff = Files.newBufferedReader(filePath)) {
            buff.lines()
                    .forEach(line -> {
                        var data = line.split(" ");
                        Node node = new Node(Integer.parseInt(data[0]), Integer.parseInt(data[1]), country);
                        readNodes.addToQueue(node);
                    });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        int noClients = 2;
        long podiumCacheExpiration = 2L;
        int workingThreads = 4;
        var finishedClients = new AtomicInteger(0);
        MyBlockingQueue blockingQueue = new MyBlockingQueue(finishedClients, 100, noClients);
        MyLinkedList linkedList = new MyLinkedList();

        ExecutorService readThreads = Executors.newFixedThreadPool(12);
        ServerSocket socket = new ServerSocket(9998);
        ConnectionManager connectionManager = new ConnectionManager(readThreads, finishedClients, socket, blockingQueue, noClients, linkedList,podiumCacheExpiration);
        connectionManager.startListening();

        var start = System.currentTimeMillis();

        var thrArr = new ArrayList<Thread>();
        var fraud = new ConcurrentHashMap<Integer, Boolean>();
        for (int i = 0; i < workingThreads; i++) {
            Thread thr = new Thread(() -> {
                Node node;
                do {
                    node = blockingQueue.pool();
                    if (node != null) {
                        if (node.totalScore > 0) {
                            linkedList.add(node);
                        } else {
                            if (fraud.get(node.id) == null) {
                                //not fraud before
                                linkedList.deleteNodeWithId(node.id);
                                fraud.put(node.id, true);
                            }
                        }
                    }
                } while (node != null);
            });
            thr.start();
            thrArr.add(thr);
        }
//        readThreads.shutdown();
//        while (!readThreads.awaitTermination(10, TimeUnit.SECONDS)) ;
        thrArr.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception e) {
                System.out.println("Error in waiting");
            }
        });
        connectionManager.waitForClients();

        System.out.println("Main thread done");

//        linkedList.showPodium(outputPath);
//
//        var end = System.currentTimeMillis();
//        System.out.println(end - start);
    }
}