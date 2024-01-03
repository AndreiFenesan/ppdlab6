package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class LockingNode {
    private int id;
    private int totalValue;
    private String country;
    private LockingNode next;
    private ReentrantLock lock;

    public LockingNode(int id, int score, String country) {
        this.id = id;
        totalValue = score;
        lock = new ReentrantLock();
        this.country = country;
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
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public String toString() {
        return """
                 id: %d name: %s, counrty: %s""".formatted(id, totalValue, country);
    }
}
