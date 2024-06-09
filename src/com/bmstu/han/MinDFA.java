package com.bmstu.han;

import java.util.*;


//最小化DFA(简化DFA)类
public class MinDFA {

    /**
     * Шаг 1
     * построить таблицу списков обратных ребер размера n×|Σ| (建立一个大小为 n×|Σ|)
     * n - номер штата источника DFA(n - 源 DFA 的状态编号)
     */
    private static Map<Edge, Set<Integer>> buildInverseTransition(DFA dfa) {
        var n = dfa.countStates();
        Map<Edge, Set<Integer>> dfaInverseEdges = new HashMap<>();

        for (int i = 0; i < n; i++) {
            for (Character c : getLiterate(dfa)) {
                dfaInverseEdges.put(buildFAEdge(i, c), dfa.getAllStatesToBySymbol(i, c));
            }
        }

        return dfaInverseEdges;
    }

    /**
     * Шаг 2
     * построить массив достижимости состояний с самого начала — достижимого размера n(从头开始构建一个状态可达性数组——可达性大小n)
     */
    private static Map<Integer, Boolean> buildReachableStateFromStart(DFA dfa) {
        Map<Integer, Boolean> result = new HashMap<>();
        var reachableStatesFromStart = dfa.getReachableStatesFromStart();
        var dfaStateCount = dfa.countStates();
        for (int i = 0; i < dfaStateCount; i++) {
            result.put(i, reachableStatesFromStart.contains(i));
        }
        return result;
    }

    /**
     * получить все состояния терминала(获取所有终端状态)
     */
    private static boolean[] getTerminalStateArray(DFA dfa) {
        var n = dfa.countStates();
        var finalStates = dfa.getFinalStates();
        boolean[] result = new boolean[n];
        for (var i = 0; i < n; i++) {
            result[i] = finalStates.contains(i);
        }
        return result;
    }

    /**
     * Шаг 3 и 4
     */
    private static boolean[][] buildTable(DFA dfa) {
        int n = dfa.countStates();
        boolean[] isTerminal = getTerminalStateArray(dfa);
        Map<Edge, Set<Integer>> dfaInverseEdges = buildInverseTransition(dfa);
        Stack<Status> statePairs = new Stack<>();
        Set<Character> literate = getLiterate(dfa);

        boolean[][] marked = new boolean[n][n];

        //Шаг 3
        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                if (!marked[i][j] && isTerminal[i] != isTerminal[j]) {
                    marked[i][j] = marked[j][i] = true;
                    statePairs.push(new Status(i, j));
                }
            }
        }

        //Шаг 4
        while (!statePairs.isEmpty()) {
            var headStatePair = statePairs.pop();
            for (Character c : literate) {
                var rList = dfaInverseEdges.get(buildFAEdge(headStatePair.getI(), c));
                for (Integer r : rList) {
                    var sList = dfaInverseEdges.get(buildFAEdge(headStatePair.getJ(), c));
                    for (Integer s : sList) {
                        if (!marked[r][s]) {
                            marked[r][s] = marked[s][r] = true;
                            statePairs.push(new Status(r, s));
                        }
                    }
                }
            }
        }
        return marked;
    }

    /**
     * Шаг 6
     * Создайте свернутый DFA(构建最小化 DFA)
     */
    private static DFA buildDFA(int[] component, DFA sourceDFA) {
        DFA result = new DFA();
        var oldFinalsState = sourceDFA.getFinalStates();
        var n = sourceDFA.countStates();
        var equivalentStates = getEquivalentState(component, sourceDFA);
        for (var state = 0; state < n; state++) {
            var currentNewState = component[state];
            if (areEquivalentState(state, currentNewState, equivalentStates) && currentNewState != state) {
                continue;
            }
            for (Transition transition : sourceDFA.getTransitions()) {
                if (transition.getFromState() == state) {
                    var toNewState = component[transition.getToState()];
                    result.setTransition(currentNewState, toNewState, transition.getSymbol());
                }
            }
            if (oldFinalsState.contains(state)) {
                result.setMinDfaFinalState(currentNewState);
            }
        }
        return result;
    }

    private static Map<Integer, List<Integer>> getEquivalentState(int[] component, DFA dfa) {
        var n = dfa.countStates();
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (var i = 0; i < n; i++) {
            var index = new ArrayList<Integer>();
            for (var j = i; j < n; j++) {
                if (component[j] == i) {
                    index.add(j);
                }
            }
            if (index.size() >= 2) {
                result.put(i, index);
            }
        }

        return result;
    }

    private static boolean areEquivalentState(int firstState, int secondState, Map<Integer, List<Integer>> mapEquivalentStates) {
        return (mapEquivalentStates.containsKey(firstState) && mapEquivalentStates.get(firstState).contains(secondState))
                || (mapEquivalentStates.containsKey(secondState) && mapEquivalentStates.get(secondState).contains(firstState));
    }

    private static Edge buildFAEdge(int state, char symbol) {
        return new Edge(state, symbol);
    }

    private static Set<Character> getLiterate(DFA dfa) {
        ArrayList<Transition> dfaTransitions = dfa.getTransitions();
        Set<Character> literate = new HashSet<>();
        for (Transition transition : dfaTransitions) {
            if (Utils.isInputCharacter(transition.getSymbol())) {
                literate.add(transition.getSymbol());
            }
        }

        return literate;
    }


    /**
     * Метод Main
     * Сворачивает исходный DFA(最小化源 DFA)
     * <p>
     * 最小化DFA(简化DFA)方法
     */
    public static DFA minimization(DFA dfa) {
        boolean[][] marked = buildTable(dfa);
        var reachable = buildReachableStateFromStart(dfa);
        var n = dfa.countStates();
        //Шаг 5
        int[] component = new int[n];
        Arrays.fill(component, -1);

        for (var i = 0; i < n; i++) {
            if (!marked[0][i]) {
                component[i] = 0;
            }
        }
        int componentsCount = 0;

        for (var i = 0; i < n; i++) {
            if (!reachable.get(i)) {
                continue;
            }
            if (component[i] == -1) {
                componentsCount++;
                component[i] = componentsCount;
                for (var j = i + 1; j < n; j++) {
                    if (!marked[i][j]) {
                        component[j] = componentsCount;
                    }
                }
            }
        }

        //Шаг 6
        return buildDFA(component, dfa);

    }

}
