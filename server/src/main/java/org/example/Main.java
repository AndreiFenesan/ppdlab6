package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    public static void main(String[] args) throws InterruptedException {
        var filesFinished = new AtomicInteger(0);
        int workingThreads = 4;
        String basePath = "/home/andrei/Desktop/an3_sem1/prog_paralela_si_distribuita/lab4/inputData/";
        String outputPath = "/home/andrei/Desktop/an3_sem1/prog_paralela_si_distribuita/lab5/outputData/work2.txt";

        MyBlockingQueue blockingQueue = new MyBlockingQueue(filesFinished, 100);
        MyLinkedList linkedList = new MyLinkedList();

        ExecutorService readThreads = Executors.newFixedThreadPool(12);

        var start = System.currentTimeMillis();

        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 10; j++) {
                var path = basePath + "RezultateC" + i + "_P" + j + ".txt";
                int finalI = i;
                readThreads.execute(() -> {
                    try {
                        readFileAndAddToQueue(blockingQueue, path, "C" + finalI);
                        var count = filesFinished.incrementAndGet();
                        if (count == 50) {
                            blockingQueue.signal();
                        }
                    } catch (IOException e) {
                        System.out.println("ERROR thr");
                    }
                });
            }
        }

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

        readThreads.shutdown();
        while (!readThreads.awaitTermination(10, TimeUnit.SECONDS)) ;
        thrArr.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception e) {
                System.out.println("Error in waiting");
            }
        });

        linkedList.showPodium(outputPath);

        var end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}