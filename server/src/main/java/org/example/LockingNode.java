package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class LockingNode {
    private int id;
    private int totalValue;
    private String country;
    private LockingNode next;
    private ReentrantLock lock;
    private final ReentrantLock podiumLock;

    public LockingNode(int id, int score, String country, ReentrantLock podiumLock) {
        this.id = id;
        totalValue = score;
        lock = new ReentrantLock();
        this.country = country;
        this.podiumLock = podiumLock;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public LockingNode getNext() {
        return this.next;
    }

    public void setNext(LockingNode next) {
        this.next = next;
    }

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void addScore(int value) {
        totalValue += value;
    }

    public void lock() {
        lock.lock();
        podiumLock.lock();
    }

    public void unlock() {
        lock.unlock();
        podiumLock.unlock();
    }

    @Override
    public String toString() {
        return """
                id: %d name: %s, counrty: %s""".formatted(id, totalValue, country);
    }
}
