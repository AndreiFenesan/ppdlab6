package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class MyLinkedList {
    private LockingNode startSentinel;
    private LockingNode endSentinel;
    private final ReentrantLock podiumLock;

    public MyLinkedList() {
        this.podiumLock = new ReentrantLock();
        this.startSentinel = new LockingNode(-1, -1, "", podiumLock);
        this.endSentinel = new LockingNode(-2, -2, "", podiumLock);
        startSentinel.setNext(endSentinel);
    }

    public void deleteNodeWithId(int id) {
        startSentinel.lock();
        LockingNode previous = startSentinel;
        LockingNode current = previous.getNext();
        current.lock();
        while (current != endSentinel && current.getId() != id) {
            previous.unlock();
            previous = current;
            current = current.getNext();
            current.lock();
        }
        if (current.getId() == id) {
            //delete node
            previous.setNext(current.getNext());
        }
        previous.unlock();
        current.unlock();
    }

    public void add(Node node) {
        startSentinel.lock();
        LockingNode previous = startSentinel;
        LockingNode current = previous.getNext();
        current.lock();
        while (current != endSentinel && current.getId() != node.id) {
            previous.unlock();
            previous = current;
            current = current.getNext();
            current.lock();
        }
        if (current.getId() == node.id) {
            //increment score
            previous.unlock();
            current.addScore(node.totalScore);
            current.unlock();
        } else if (current == endSentinel) {
            //add new node
            LockingNode newNode = new LockingNode(node.id, node.totalScore, node.country, podiumLock);
            previous.setNext(newNode);
            newNode.setNext(current);
            previous.unlock();
            current.unlock();
        }
    }

    private static void writeToFile(String filename, PriorityQueue<LockingNode> nodes) throws IOException {
        var filePath = Paths.get(filename);
        var lines = new ArrayList<String>();
        nodes.forEach(node -> lines.add(node.toString()));
        Files.write(filePath, lines);
    }

    public List<Node> getPodium() {
        podiumLock.lock();
        PriorityQueue<LockingNode> nodes = new PriorityQueue<>((o1, o2) -> o2.getTotalValue() - o1.getTotalValue());
        LockingNode current = startSentinel.getNext();
        while (current != endSentinel) {
            nodes.add(current);
            current = current.getNext();
        }
        List<Node> podium = new ArrayList<>();
        for (var lockingNode : nodes) {
            podium.add(new Node(lockingNode.getId(), lockingNode.getTotalValue(), lockingNode.getCountry()));
        }
        podiumLock.unlock();
        return podium;
    }
}
