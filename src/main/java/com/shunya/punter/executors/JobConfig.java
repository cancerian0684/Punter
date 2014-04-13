package com.shunya.punter.executors;

public class JobConfig {
    private int sequence;
    private String name;

    public JobConfig(int sequence, String name) {
        this.sequence = sequence;
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "JobConfig{" +
                "sequence=" + sequence +
                ", name='" + name + '\'' +
                '}';
    }
}
