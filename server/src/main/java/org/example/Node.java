package org.example;

public class Node {
    public int id;
    public int totalScore;
    public String country;

    public Node(int id, int totalScore, String country) {
        this.id = id;
        this.totalScore = totalScore;
        this.country = country;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", totalScore=" + totalScore +
                '}';
    }
}
