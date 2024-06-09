package com.bmstu.han;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


//NFA类
public class NFA {
    private final ArrayList<Integer> states = new ArrayList<>();
    private final ArrayList<Transition> transitions = new ArrayList<>();
    private int finalState;

    public int getStatesCount() {
        return this.states.size();
    }

    public void setStates(int totalStates) {
        for (int i = 0; i < totalStates; i++) {
            this.states.add(i);
        }
    }

    public void addTransition(int stateFrom, int stateTo, char symbol) {
        Transition trans = new Transition(stateFrom, stateTo, symbol);
        this.transitions.add(trans);
    }

    public void setFinalState(int finalState) {
        this.finalState = finalState;
    }

    public int getFinalState() {
        return this.finalState;
    }

    public List<Transition> getTransitions() {
        return this.transitions;
    }

    public void display() {
        for (Transition temp : transitions) {
            System.out.println("s" + temp.getFromState() + " " + temp.getSymbol() + " --> s" + temp.getToState());
        }
        System.out.println("конечное состояние(最终状态) s" + getFinalState());
    }
    //ε
    public ArrayList<Character> findPossibleInputSymbols(ArrayList<Integer> states) {
        ArrayList<Character> result = new ArrayList<>();
        for (int stateFrom : states) {
            for (Transition transition : transitions) {
                if (transition.getFromState() == stateFrom && transition.getSymbol() != 'ε') {
                    result.add(transition.getSymbol());
                }
            }
        }
        return result;
    }

    public ArrayList<Integer> unique(ArrayList<Integer> list) {
        return IntStream
                .range(0, list.size())
                .filter(i -> ((i < list.size() - 1 && !list.get(i).equals(list.get(i + 1))) || i == list.size() - 1))
                .mapToObj(list::get).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Integer> eclosure(ArrayList<Integer> states) {
        ArrayList<Integer> result = new ArrayList<>();
        boolean[] visited = new boolean[getStatesCount()];
        for (Integer integer : states) {
            eclosure(integer, result, visited);
        }
        Collections.sort(result);
        return unique(result);
    }

    public void eclosure(int x, ArrayList<Integer> result, boolean[] visited) //Simple DFS
    {
        result.add(x);
        for (Transition transition : transitions) {
            if (transition.getFromState() == x && transition.getSymbol() == 'ε') {
                int y = transition.getToState();
                if (!visited[y]) {
                    visited[y] = true;
                    eclosure(y, result, visited);
                }
            }
        }
    }

    public ArrayList<Integer> move(ArrayList<Integer> T, char symbol) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int t : T) {
            for (Transition transition : transitions) {
                if (transition.getFromState() == t && transition.getSymbol() == symbol) {
                    result.add(transition.getToState());
                }
            }
        }
        Collections.sort(result);
        int l1 = result.size();
        unique(result);
        int l2 = result.size();
        if (l2 < l1) {
            System.out.println("move(T, a) returns non-unique ArrayList");
            System.exit(1);
        }
        return result;
    }
}
