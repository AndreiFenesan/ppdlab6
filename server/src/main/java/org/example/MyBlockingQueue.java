package org.example;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue {
    private final int noClients;
    private final Queue<Node> nodes;
    private ReentrantLock lock;
    private Condition notEmpty;
    private Condition notFull;
    private AtomicInteger noFinishedClients;
    private int maxCapacity;

    public MyBlockingQueue(AtomicInteger noFinishedClients, int maxCapacity, int noClients) {
        nodes = new ArrayDeque<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        this.noFinishedClients = noFinishedClients;
        this.maxCapacity = maxCapacity;
        this.noClients = noClients;
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

    //    public Node pool() {
//        lock.lock();
//        try {
//            if (noReadFiles.get() < 1) {
//                while (nodes.isEmpty() && noReadFiles.get() < 1) {
//                    notEmpty.await();
//                }
//                notFull.signalAll();
//                return nodes.poll();
//            } else {
//                if (!nodes.isEmpty()) {
//                    return nodes.poll();
//                }
//            }
//        } catch (InterruptedException e) {
//            System.out.println(e);
//        } finally {
//            lock.unlock();
//        }
//        return null;
//    }
    public Node pool() {
        lock.lock();
        try {
            while (nodes.isEmpty() && noFinishedClients.get() < noClients) {
                notEmpty.await();
            }
            var elem = nodes.poll();
            notFull.signalAll();
            return elem;
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