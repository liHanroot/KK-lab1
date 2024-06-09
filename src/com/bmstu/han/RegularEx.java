package com.bmstu.han;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;


//Регулярное выражение для создания NFA(从正则表达式创建 NFA 的类)
public class RegularEx {
    //a.b
    private static NFA concat(NFA a, NFA b) {
        NFA result = new NFA();
        //При конкатенации не добавляется новое состояние(连接中未添加新状态)
        result.setStates(a.getStatesCount() + b.getStatesCount());
        //ε
        //Скопируйте все старые переходы(复制所有旧转换)
        for (Transition transition : a.getTransitions()) {
            result.addTransition(transition.getFromState(), transition.getToState(), transition.getSymbol());
        }

        //Создание ссылки; конечное состояние a будет связано с начальным состоянием b
        //(创建链接；a 的最终状态将链接到 b 的初始状态)
        result.addTransition(a.getFinalState(), a.getStatesCount(), 'ε');

        //Скопируйте все старые переходы b со смещением, поскольку состояния a уже добавлены.
        //(复制 b 的所有旧转换，并加上偏移量，因为 a 的状态已经添加)
        var offset = a.getStatesCount();
        for (Transition transition : b.getTransitions()) {
            result.addTransition(transition.getFromState() + offset, transition.getToState() + offset, transition.getSymbol());
        }

        //b — конечное состояние созданного NFA
        //b 是此创建的 NFA 的最终状态
        result.setFinalState(offset + b.getStatesCount() - 1);
        return result;
    }

    //a*
    private static NFA kleene(NFA a) {
        NFA result = addStateBefore(a);

        var oldFinalState = a.getStatesCount();
        var oldInitialState = 1;
        var newInitialState = 0;
        var newFinalState = oldFinalState + 1;


        //Переход Эпсилона в новое конечное состояние(Epsilon 过渡到新的最终状态)
        result.addTransition(oldFinalState, newFinalState, 'ε');
        //Обратный эпсилон-переход(逆向 epsilon 转变)
        result.addTransition(oldFinalState, oldInitialState, 'ε');
        //Прямой полный эпсилон-переход(前向总 epsilon 转变)
        result.addTransition(newInitialState, newFinalState, 'ε');
        //Отметить окончательное состояние(标记最终状态)
        result.setFinalState(newFinalState);

        return result;
    }

    //a+
    private static NFA plus(NFA a) {
        NFA result = addStateBefore(a);
        var oldFinalState = a.getStatesCount();
        var oldInitialState = 1;
        var newFinalState = oldFinalState + 1;

        //Переход Эпсилона в новое конечное состояние(Epsilon 过渡到新的最终状态)
        result.addTransition(oldFinalState, newFinalState, 'ε');
        //Обратный эпсилон-переход(逆向 epsilon 转变)
        result.addTransition(oldFinalState, oldInitialState, 'ε');
        //Отметить окончательное состояние(标记最终状态)
        result.setFinalState(newFinalState);

        return result;
    }

    //s0->s1 как результат s0->s1->s2 , где s0->s1 переход эпсилон
    //s0->s1 结果为 s0->s1->s2 ，其中 s0->s1 epsilon 的转换
    private static NFA addStateBefore(NFA a) {
        NFA result = new NFA();

        /*
         * +2, потому что у нас будет одно новое начальное состояние с эпсилон-переходом в начальное состояние a.
         * +2，因为我们将有一个新的初始状态，其中 epsilon 过渡到 a 的初始状态
         * и одно новое конечное состояние с эпсилон-переходом из конечного состояния a и из нового созданного начального состояния.
         * 以及一个新的最终状态，其中 epsilon 转换来自 a 的最终状态和新创建的初始状态
         */
        result.setStates(a.getStatesCount() + 2);

        result.addTransition(0, 1, 'ε');

        for (Transition transition : a.getTransitions()) {
            result.addTransition(transition.getFromState() + 1, transition.getToState() + 1, transition.getSymbol());
        }

        return result;
    }

    //a|b
    private static NFA orSelection(ArrayList<NFA> selections, int noOfSelections) {
        NFA result = new NFA();
        var stateCount = 2;

        //Найдите общее количество состояний, суммируя все NFA
        //通过对所有 NFA 求和来查找总状态数
        for (var i = 0; i < noOfSelections; i++) {
            stateCount += selections.get(i).getStatesCount();
        }
        result.setStates(stateCount);
        var adderTrack = 1;
        for (var i = 0; i < noOfSelections; i++) {
            //Начальный эпсилон-переход в первый блок «ИЛИ»
            //初始 epsilon 过渡到第一个“OR”块
            result.addTransition(0, adderTrack, 'ε');

            NFA selectedNFA = selections.get(i);
            for (Transition transition : selectedNFA.getTransitions()) {
                result.addTransition(transition.getFromState() + adderTrack, transition.getToState() + adderTrack, transition.getSymbol());
            }
            adderTrack += selectedNFA.getStatesCount();

            //Добавить эпсилон-переход в конечное состояние
            //将 epsilon 转换添加到最终状态
            result.addTransition(adderTrack - 1, stateCount - 1, 'ε');
        }
        result.setFinalState(stateCount - 1);
        return result;
    }

    private static boolean isNotOperator(char currentSymbol) {
        return currentSymbol != '(' && currentSymbol != ')' && currentSymbol != '*'
                && currentSymbol != '|' && currentSymbol != '.' && currentSymbol != '+';
    }


    //正则表达式转NFA方法
    public static NFA regexToNfa(String regex) {
        regex = Utils.normalizeInputRegex(regex);
        Stack<Character> operators = new Stack<>();
        Stack<NFA> operands = new Stack<>();
        char operatorSymbol;
        int operatorCount;
        char currentSymbol;
        NFA newSym;
        char[] x = regex.toCharArray();
        for (char value : x) {
            currentSymbol = value;
            if (isNotOperator(currentSymbol)) //Должен быть персонаж, поэтому создайте простейший NFA.(必须是一个字符，所以构建最简单的NFA)
            {
                newSym = new NFA();
                newSym.setStates(2);
                newSym.addTransition(0, 1, currentSymbol);
                newSym.setFinalState(1);
                operands.push(newSym);//отодвинь это назад(将其推回)
            } else {
                switch (currentSymbol) {
                    case '*':
                        NFA starSym = operands.pop();
                        operands.push(kleene(starSym));
                        break;
                    case '+':
                        NFA plusSym = operands.pop();
                        operands.push(plus(plusSym));
                        break;
                    case '.':
                    case '|':
                    case '(':
                        operators.push(currentSymbol);
                        break;
                    default:
                        operatorCount = 0;
                        operatorSymbol = operators.peek();
                        //Продолжайте поиск операндов(继续搜索操作数)
                        if (operatorSymbol == '(') {
                            continue;
                        }
                        //Собрать операнды(收集操作数)
                        do {
                            operators.pop();
                            operatorCount++;
                        } while (operators.peek() != '(');
                        operators.pop();
                        NFA firstOperand;
                        NFA secondOperand;
                        ArrayList<NFA> selections = new ArrayList<>();
                        if (operatorSymbol == '.') {
                            for (int ii = 0; ii < operatorCount; ii++) {
                                secondOperand = operands.pop();
                                firstOperand = operands.pop();
                                operands.push(concat(firstOperand, secondOperand));
                            }
                        } else if (operatorSymbol == '|') {
                            for (int j = 0; j < operatorCount + 1; j++) {
                                selections.add(new NFA());
                            }

                            int tracker = operatorCount;
                            for (int k = 0; k < operatorCount + 1; k++) {
                                selections.set(tracker, operands.pop());
                                tracker--;
                            }
                            operands.push(orSelection(selections, operatorCount + 1));
                        }
                        break;
                }
            }
        }
        return operands.peek();//Верните единую сущность. операнды.poll() тоже в порядке(返回单个实体。operands.poll() 也可以)
    }


    //NFA转DFA方法
    public static DFA nfaToDfa(NFA nfa) {
        DFA dfa = new DFA();
        ArrayList<Integer> start = new ArrayList<>();
        start.add(0);
        ArrayList<Integer> s0 = nfa.eclosure(start);
        int stateFrom = dfa.addEntry(s0);
        while (stateFrom != -1) {
            ArrayList<Integer> T = dfa.entryAt(stateFrom);
            dfa.markEntry(stateFrom);
            ArrayList<Character> symbols = nfa.findPossibleInputSymbols(T);
            for (char a : symbols) {
                ArrayList<Integer> U = nfa.eclosure(nfa.move(T, a));
                int stateTo = dfa.findEntry(U);
                if (stateTo == -1) { // U еще не в S'(U 尚未加入 S')
                    stateTo = dfa.addEntry(U);
                }
                dfa.setTransition(stateFrom, stateTo, a);
            }
            stateFrom = dfa.nextUnmarkedEntryIdx();
        }
        // Конечными состояниями DFA являются те, которые содержат любое из конечных состояний NFA.
        // DFA 的完成状态是包含 NFA 的任意完成状态的状态。
        dfa.setFinalState(nfa.getFinalState());
        return dfa;
    }

    public static void recognise() {
        String eval = "";
        while (!eval.equals("quit")) {
            System.out.println("В настоящее время поддерживаются только буквы a и b(当前只支持字母a和b)., Поддержка оператора(操作符支持) '.', '|', '*','(',')'");
            System.out.println("Введите регулярное выражение(输入正则表达式). Например(例如): a*、b*、(a|b)*、a*b(b|a)* и т.д.");
            System.out.println("Regular Expressions: ");
            Scanner sc = new Scanner(System.in);
            String regex = sc.next();

            System.out.println("\nПостроить NFA на основе регулярного выражения(根据正则表达式构造NFA): ");
            NFA requiredNfa;
            requiredNfa = regexToNfa(regex);
            requiredNfa.display();
            System.out.println("\nПостроить DFA на основе NFA(基于NFA构造DFA) :");
            DFA requiredDfa = nfaToDfa(requiredNfa);
            requiredDfa.display();
            DFA requiredMinDfa = MinDFA.minimization(requiredDfa);
            System.out.println("\nУпростить DFA(简化 DFA) :");
            requiredMinDfa.displayMinDFA();


            System.out.println("Введите quit, чтобы закончить, или введите любую строку, чтобы продолжить.(输入 quit 结束，或输入任意行继续。)");
            eval = new Scanner(System.in).next();
        }

    }
}
