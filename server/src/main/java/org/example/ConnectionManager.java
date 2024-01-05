package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager {
    private int noClients;
    private final ExecutorService readThreads;
    private final AtomicInteger finishedClients;
    private final ServerSocket serverSocket;
    private final MyBlockingQueue myBlockingQueue;
    private Thread worker;

    public ConnectionManager(ExecutorService readThreads, AtomicInteger finishedClients, ServerSocket serverSocket, MyBlockingQueue myBlockingQueue, int noClients) {
        this.readThreads = readThreads;
        this.finishedClients = finishedClients;
        this.serverSocket = serverSocket;
        this.myBlockingQueue = myBlockingQueue;
        this.noClients = noClients;
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
                            Object data;
                            do {
                                data = inputStream.readObject();
                                if (data instanceof BatchOfCompetitiorResult receivedData) {
                                    handleBatchResult(receivedData);
                                } else if (data instanceof GetPodiumRequest) {
                                    System.out.println("Request received");
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

    private void handleBatchResult(BatchOfCompetitiorResult receivedData) {
        var results = receivedData.getResultList();
        System.out.println("Received size of: " + results.size());
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
