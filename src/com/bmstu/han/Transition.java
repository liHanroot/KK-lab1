package com.bmstu.han;

public class Transition {
    private int fromState;
    private int toState;

    private char symbol;

    public Transition(int fromState, int toState, char symbol) {
        this.fromState = fromState;
        this.toState = toState;
        this.symbol = symbol;
    }

    public int getFromState() {
        return fromState;
    }

    public void setFromState(int fromState) {
        this.fromState = fromState;
    }

    public int getToState() {
        return toState;
    }

    public void setToState(int toState) {
        this.toState = toState;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

}
