package com.bmstu.han;

import java.util.Objects;

public class Edge {
    private int state;
    private char symbol;

    public Edge(int state, char symbol) {
        this.state = state;
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge faEdge = (Edge) o;
        return state == faEdge.state && symbol == faEdge.symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, symbol);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }
}
