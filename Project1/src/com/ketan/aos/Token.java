package com.ketan.aos;

import java.io.Serializable;
import java.util.Arrays;

public class Token implements Serializable {

    private final int originator;
    private final String[] path;

    private int currentIndex;
    private int sum;
    private boolean isFinished;

    public Token(
            final int originator,
            final String[] path) {

        this.originator = originator;
        this.path = path;
        this.currentIndex = -1;
        this.sum = 0;
        this.isFinished = false;
    }

    public void addToSum(int value) {
        sum += value;
    }

    public int getSum() {
        return sum;
    }

    private boolean isLastNode() {
        if(currentIndex > path.length - 1) {
            isFinished = true;
        }
        return isFinished;
    }

    public int getNextPathNode() {
        currentIndex++;
        return isLastNode()
                ? originator
                : Integer.parseInt(path[currentIndex]);
    }

    public boolean isConsumed() {
        return isFinished;
    }

    @Override
    public String toString() {
        return "Originator: " + originator
                + " Token: " + Arrays.asList(path)
                + " Sum: " + sum;
    }
}
