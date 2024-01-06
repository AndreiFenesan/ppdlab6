package org.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static void writeToFile(String filePath, GetFinalResultsResponse finalResults) throws IOException {
        var pathCountry = Paths.get(filePath + "/country");
        var pathAll = Paths.get(filePath + "/all");
        try (var writer = Files.newBufferedWriter(pathCountry)) {
            var byCountry = finalResults.getCountryResults();
            byCountry.forEach(countryResult -> {
                try {
                    writer.write(countryResult.toString());
                    writer.write('\n');
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        try (var writer = Files.newBufferedWriter(pathAll)) {
            var allResults = finalResults.getCompetitorsResultList();
            allResults.forEach(result -> {
                try {
                    writer.write(result.toString());
                    writer.write('\n');
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        int noClients = 3;
        long podiumCacheExpiration = 2L;
        int workingThreads = 4;
        var finishedClients = new AtomicInteger(0);
        MyBlockingQueue blockingQueue = new MyBlockingQueue(finishedClients, 100, noClients);
        MyLinkedList linkedList = new MyLinkedList();

        ExecutorService readThreads = Executors.newFixedThreadPool(12);
        ServerSocket socket = new ServerSocket(9998);
        ConnectionManager connectionManager = new ConnectionManager(readThreads, finishedClients, socket, blockingQueue, noClients, linkedList, podiumCacheExpiration);
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

        thrArr.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception e) {
                System.out.println("Error in waiting");
            }
        });

        connectionManager.waitForClients();
        writeToFile("/home/andrei/Desktop/an3_sem1/prog_paralela_si_distribuita/lab6Output", connectionManager.getFinalResult());

        readThreads.shutdown();
        while (!readThreads.awaitTermination(10, TimeUnit.SECONDS)) ;

        System.out.println("Main thread done");
        var end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}