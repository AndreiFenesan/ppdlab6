package org.example;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue {
    private final Queue<Node> nodes;
    private ReentrantLock lock;
    private Condition notEmpty;
    private Condition notFull;
    private AtomicInteger noReadFiles;
    private int maxCapacity;

    public MyBlockingQueue(AtomicInteger noReadFiles, int maxCapacity) {
        nodes = new ArrayDeque<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        this.noReadFiles = noReadFiles;
        this.maxCapacity = maxCapacity;
    }

    public void addToQueue(Node node) {
        lock.lock();
        try {
            while (nodes.size() >= maxCapacity) {
                notFull.await();
            }
            nodes.add(node);
            notEmpty.signalAll();
        } catch (InterruptedException e) {
            System.out.println("Error thr add to queue");
        } finally {
            lock.unlock();
        }
    }

    public void signal() {
        lock.lock();
        notEmpty.signalAll();
        lock.unlock();
    }

    public Node pool() {
        lock.lock();
        try {
            if (noReadFiles.get() < 50) {
                while (nodes.isEmpty() && noReadFiles.get() < 50) {
                    notEmpty.await();

                }
                notFull.signalAll();
                return nodes.poll();
            } else {
                if (!nodes.isEmpty()) {
                    return nodes.poll();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public int size() {
        int s = 0;
        lock.lock();
        s = nodes.size();
        lock.unlock();
        return s;
    }

}