package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
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
    private long cacheExpirationMillis;
    private long cachedAt;
    private List<CountryResult> cachedPodium;
    private CyclicBarrier barrier;

    public ConnectionManager(ExecutorService readThreads,
                             AtomicInteger finishedClients,
                             ServerSocket serverSocket,
                             MyBlockingQueue myBlockingQueue,
                             int noClients,
                             MyLinkedList podiumList,
                             long cacheExpirationMillis) {
        this.readThreads = readThreads;
        this.finishedClients = finishedClients;
        this.serverSocket = serverSocket;
        this.myBlockingQueue = myBlockingQueue;
        this.noClients = noClients;
        this.podiumList = podiumList;
        cachedPodium = null;
        this.cacheExpirationMillis = cacheExpirationMillis;
        this.barrier = new CyclicBarrier(noClients);
    }


    public void startListening() {
        Thread thread = new Thread(() -> {
            while (noClients > 0) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Got a new connection");
                    ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
                    readThreads.submit(() -> {
                        try {
                            Object data;
                            do {
                                data = inputStream.readObject();
                                if (data instanceof BatchOfCompetitiorResult receivedData) {
                                    handleBatchResult(receivedData);
                                } else if (data instanceof GetPodiumRequest) {
                                    System.out.println("podium request received");
                                    handleGetPodiumRequest(outputStream);
                                }
                            } while (!(data instanceof GetFinalResultsRequests));
                        } catch (IOException e) {
                            System.out.println("Error in reading from socket " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error class not found");
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Finished reading from client");
                        finishedClients.incrementAndGet();
                        myBlockingQueue.signal();
                        System.out.println("Waiting for the other to finish");
                        try {
                            barrier.await();
                            System.out.println("All country sent the data. We must sent the final results");
                            outputStream.writeObject(getFinalResult());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }
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

    private void handleGetPodiumRequest(ObjectOutputStream outputStream) throws IOException, ExecutionException, InterruptedException {
        var computedResult = getPodiumByCountry();
        System.out.println("Sending podium");
        outputStream.writeObject(new GetPodiumResponse(computedResult));
    }

    private List<CountryResult> getPodiumByCountry() throws ExecutionException, InterruptedException {
        Future<List<CountryResult>> result = readThreads.submit(() -> {
            if (cachedPodium == null || System.currentTimeMillis() - cachedAt > cacheExpirationMillis) {
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
                cachedAt = System.currentTimeMillis();
                cachedPodium = results;
            }
            return cachedPodium;
        });
        return result.get();
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

    public GetFinalResultsResponse getFinalResult() throws ExecutionException, InterruptedException {
        var podium = podiumList.getPodium();
        var country = getPodiumByCountry();
        var podiumDto = new ArrayList<CompetitorResult>();
        podium.forEach(node -> {
            CompetitorResult competitorResult = new CompetitorResult();
            competitorResult.setId(node.id);
            competitorResult.setScore(node.totalScore);
            competitorResult.setCountry(node.country);
            podiumDto.add(competitorResult);
        });
        return new GetFinalResultsResponse(country, podiumDto);
    }
}
