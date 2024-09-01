package io.client.accountserviceclient.runner;

import lombok.Getter;

public class CustomTask implements Runnable {
    @Getter
    private final int id;
    private final Runnable task;

    public CustomTask(int id, Runnable task) {
        this.id = id;
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public String toString() {
//        return "IdentifiedTask{id=" + id + ", task=" + task.getClass().getName() + "}";
        return "{id=" + id + "}";
    }
}
