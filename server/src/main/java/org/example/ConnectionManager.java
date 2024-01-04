package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ConnectionManager {
    private final ExecutorService readThreads;
    private final ExecutorService workerThreads;
    private final ServerSocket serverSocket;
    private final MyBlockingQueue myBlockingQueue;
    private Thread worker;

    public ConnectionManager(ExecutorService readThreads, ExecutorService workerThreads, ServerSocket serverSocket, MyBlockingQueue myBlockingQueue) {
        this.readThreads = readThreads;
        this.workerThreads = workerThreads;
        this.serverSocket = serverSocket;
        this.myBlockingQueue = myBlockingQueue;
    }


    public void startListening() {
        Thread thread = new Thread(() -> {
            int noClients = 1;

            while (noClients > 0) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Got a new connection");
                    readThreads.submit(() -> {
                        try {
                            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
                            BatchOfCompetitiorResult receivedData;
                            do {
                                var data = inputStream.readObject();
                                receivedData = (BatchOfCompetitiorResult) data;
                                var results = receivedData.getResultList();
                                System.out.println("Received size of: " + results.size());
                                if (!results.isEmpty()) {
                                    for (var result : results) {
                                        workerThreads.submit(() -> myBlockingQueue.addToQueue(
                                                new Node(result.getId(), result.getScore(), result.getCountry())));
                                    }
                                }
                            } while (!receivedData.getResultList().isEmpty());
                        } catch (IOException e) {
                            System.out.println("Error in reading from socket " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error class not found");
                        }
                        System.out.println("Finished reading from client");
                    });
                } catch (IOException e) {
                    System.out.println("Error in opening client connection");
                }
                noClients--;
            }
        });
        this.worker = thread;
        thread.start();
    }

    public void waitForClients() {
        try {
            this.worker.join();
        } catch (InterruptedException e) {
            System.out.println("Error in waiting for thread");
        }
    }
}
