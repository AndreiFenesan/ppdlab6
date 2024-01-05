package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.groupingBy;

public class ConnectionManager {
    private int noClients;
    private final ExecutorService readThreads;
    private final AtomicInteger finishedClients;
    private final ServerSocket serverSocket;
    private final MyBlockingQueue myBlockingQueue;
    private final MyLinkedList podiumList;
    private Thread worker;

    public ConnectionManager(ExecutorService readThreads, AtomicInteger finishedClients, ServerSocket serverSocket, MyBlockingQueue myBlockingQueue, int noClients, MyLinkedList podiumList) {
        this.readThreads = readThreads;
        this.finishedClients = finishedClients;
        this.serverSocket = serverSocket;
        this.myBlockingQueue = myBlockingQueue;
        this.noClients = noClients;
        this.podiumList = podiumList;
    }


    public void startListening() {
        Thread thread = new Thread(() -> {
            while (noClients > 0) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Got a new connection");
                    readThreads.submit(() -> {
                        try {
                            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
                            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                            Object data;
                            do {
                                data = inputStream.readObject();
                                if (data instanceof BatchOfCompetitiorResult receivedData) {
                                    handleBatchResult(receivedData);
                                } else if (data instanceof GetPodiumRequest) {
                                    System.out.println("podium request received");
                                    handleGetPodiumRequest(outputStream);
                                }
                            } while (!(data instanceof DoneRequest));
                        } catch (IOException e) {
                            System.out.println("Error in reading from socket " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error class not found");
                        }
                        System.out.println("Finished reading from client");
                        finishedClients.incrementAndGet();
                        myBlockingQueue.signal();
                    });
                } catch (IOException e) {
                    System.out.println("Error in opening client connection");
                }
                this.noClients--;
            }
            System.out.println("Server received data from all clients");
        });
        this.worker = thread;
        thread.start();
    }

    private void handleGetPodiumRequest(ObjectOutputStream outputStream) throws IOException {
        var podium = podiumList.getPodium();
        var byCountry = podium.stream().collect(groupingBy(node -> node.country));
        List<CountryResult> results = new ArrayList<>();
        for (var entry : byCountry.entrySet()) {
            var country = entry.getKey();
            var resultsPerCountry = entry.getValue();
            var totalScore = resultsPerCountry.stream()
                    .map(node -> node.totalScore)
                    .reduce(0, Integer::sum);
            results.add(new CountryResult(country, totalScore));
        }
        results.sort(Comparator.comparingInt(CountryResult::getTotalScore));
        System.out.println("Sending podium");
        outputStream.writeObject(new GetPodiumResponse(results));
    }

    private void handleBatchResult(BatchOfCompetitiorResult receivedData) {
        var results = receivedData.getResultList();
        System.out.println("Received size of: " + results.size() + " From: " + results.get(0).getCountry());
        if (!results.isEmpty()) {
            for (var result : results) {
                myBlockingQueue.addToQueue(
                        new Node(result.getId(), result.getScore(), result.getCountry()));
            }
        }
    }

    public void waitForClients() {
        System.out.println("Waiting for worker to finish");
        try {
            this.worker.join();
            System.out.println("Worker finished");
        } catch (InterruptedException e) {
            System.out.println("Error in waiting for thread");
        }
    }
}
