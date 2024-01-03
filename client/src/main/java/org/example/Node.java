package org.example;

public class Node implements Comparable{
    public int id;
    public int totalScore;
    public String country;

    public Node(int id, int totalScore, String country) {
        this.id = id;
        this.totalScore = totalScore;
        this.country = country;
    }

    public void addToScore(int score) {
        totalScore += score;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", totalScore=" + totalScore +
                ", country = " + country +
                '}';
    }

    public int compareTo(Node other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.totalScore, ((Node) o).totalScore);
    }
}
