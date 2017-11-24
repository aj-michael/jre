package jre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class PartialNFA {

  private final State startState;
  private final Map<State, Map<Transition, Set<State>>> transitionsTable;
  private final Map<State, Transition> danglingStateTransitions;

  private PartialNFA(
      State startState,
      Map<State, Map<Transition, Set<State>>> transitionsTable,
      Map<State, Transition> danglingStateTransitions) {
    this.startState = startState;
    this.transitionsTable = transitionsTable;
    this.danglingStateTransitions = danglingStateTransitions;
  }

  static PartialNFA literalNFA(char c) {
    State startState = new State();
    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.put(startState, new Transition.CharacterTransition(c));
    return new PartialNFA(startState, new HashMap<>(), danglingStateTransitions);
  }

  static PartialNFA wildcardNFA() {
    State startState = new State();
    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.put(startState, Transition.EMPTY_TRANSITION);
    return new PartialNFA(startState, new HashMap<>(), danglingStateTransitions);
  }

  static PartialNFA alternateNFAs(PartialNFA left, PartialNFA right) {
    State startState = new State();

    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    Map<Transition, Set<State>> startStateTransitions = new HashMap<>();
    Set<State> postTransitionStates = new HashSet<>();
    postTransitionStates.add(left.startState);
    postTransitionStates.add(right.startState);
    startStateTransitions.put(Transition.EMPTY_TRANSITION, postTransitionStates);
    transitionsTable.put(startState, startStateTransitions);
    transitionsTable.putAll(left.transitionsTable);
    transitionsTable.putAll(right.transitionsTable);

    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.putAll(left.danglingStateTransitions);
    danglingStateTransitions.putAll(right.danglingStateTransitions);

    return new PartialNFA(startState, transitionsTable, danglingStateTransitions);
  }

  static PartialNFA concatenateNFAs(PartialNFA left, PartialNFA right) {
    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    transitionsTable.putAll(left.transitionsTable);
    transitionsTable.putAll(right.transitionsTable);
    for (Map.Entry<State, Transition> danglingStateTransition :
        left.danglingStateTransitions.entrySet()) {
      Map<Transition, Set<State>> transitions = new HashMap<>();
      Set<State> transitionToStates = new HashSet<>();
      transitionToStates.add(right.startState);
      if (left.transitionsTable.containsKey(danglingStateTransition.getKey())) {
        Map<Transition, Set<State>> existingTransitions =
            left.transitionsTable.get(danglingStateTransition.getKey());
        if (existingTransitions.containsKey(danglingStateTransition.getValue())) {
          transitionToStates.addAll(existingTransitions.get(danglingStateTransition.getValue()));
        }
      }
      transitions.put(danglingStateTransition.getValue(), transitionToStates);
      transitionsTable.put(danglingStateTransition.getKey(), transitions);
    }

    return new PartialNFA(left.startState, transitionsTable, right.danglingStateTransitions);
  }

  /** Given an NFA that matches expression e, returns an NFA that matches expression e?. */
  static PartialNFA makeOptional(PartialNFA nfa) {
    State startState = new State();

    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    transitionsTable.putAll(nfa.transitionsTable);
    Map<Transition, Set<State>> startStateTransitions = new HashMap<>();
    Set<State> startStateTransitionsStates = new HashSet<>();
    startStateTransitionsStates.add(nfa.startState);
    startStateTransitions.put(Transition.EMPTY_TRANSITION, startStateTransitionsStates);
    transitionsTable.put(startState, startStateTransitions);

    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.putAll(nfa.danglingStateTransitions);
    danglingStateTransitions.put(startState, Transition.EMPTY_TRANSITION);

    return new PartialNFA(startState, transitionsTable, danglingStateTransitions);
  }

  /** Given an NFA that matches expression e, returns an NFA that matches expression e*. */
  static PartialNFA makeOptionalRepeatable(PartialNFA nfa) {
    State startState = new State();

    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    transitionsTable.putAll(nfa.transitionsTable);
    Map<Transition, Set<State>> startStateTransitions = new HashMap<>();
    Set<State> startStateTransitionsStates = new HashSet<>();
    startStateTransitionsStates.add(nfa.startState);
    startStateTransitions.put(Transition.EMPTY_TRANSITION, startStateTransitionsStates);
    for (Map.Entry<State, Transition> danglingStateTransition :
        nfa.danglingStateTransitions.entrySet()) {
      Map<Transition, Set<State>> transitions = new HashMap<>();
      Set<State> transitionToStates = new HashSet<>();
      transitionToStates.add(startState);
      transitions.put(danglingStateTransition.getValue(), transitionToStates);
      transitionsTable.put(danglingStateTransition.getKey(), transitions);
    }
    transitionsTable.put(startState, startStateTransitions);

    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.put(startState, Transition.EMPTY_TRANSITION);

    return new PartialNFA(startState, transitionsTable, danglingStateTransitions);
  }

  /** Given an NFA that matches expression e, returns an NFA that matches expression e+. */
  static PartialNFA makeRepeatable(PartialNFA nfa) {
    State loopbackState = new State();

    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    transitionsTable.putAll(nfa.transitionsTable);
    Map<Transition, Set<State>> loopbackStateTransitions = new HashMap<>();
    Set<State> nfaStartStateSet = new HashSet<>();
    nfaStartStateSet.add(nfa.startState);
    loopbackStateTransitions.put(Transition.EMPTY_TRANSITION, nfaStartStateSet);
    transitionsTable.put(loopbackState, loopbackStateTransitions);

    for (Map.Entry<State, Transition> danglingStateTransition :
        nfa.danglingStateTransitions.entrySet()) {
      Map<Transition, Set<State>> transitions = new HashMap<>();
      Set<State> toStates = new HashSet<>();
      toStates.add(loopbackState);
      toStates.addAll(
          transitionsTable
              .getOrDefault(danglingStateTransition.getKey(), new HashMap<>())
              .getOrDefault(danglingStateTransition.getValue(), new HashSet<>()));
      transitions.put(danglingStateTransition.getValue(), toStates);
      transitionsTable.put(danglingStateTransition.getKey(), transitions);
    }

    Map<State, Transition> danglingStateTransitions = new HashMap<>();
    danglingStateTransitions.put(loopbackState, Transition.EMPTY_TRANSITION);

    return new PartialNFA(nfa.startState, transitionsTable, danglingStateTransitions);
  }

  NFA toNFA() {
    State successState = new State();
    Map<State, Map<Transition, Set<State>>> transitionsTable = new HashMap<>();
    transitionsTable.putAll(this.transitionsTable);
    for (Map.Entry<State, Transition> danglingStateTransition :
        danglingStateTransitions.entrySet()) {
      Set<State> toSet = new HashSet<>();
      toSet.add(successState);
      Map<Transition, Set<State>> transitionMap = new HashMap<>();
      transitionMap.put(danglingStateTransition.getValue(), toSet);
      if (this.transitionsTable.containsKey(danglingStateTransition.getKey())) {
        Map<Transition, Set<State>> transitionSetMap =
            this.transitionsTable.get(danglingStateTransition.getKey());
        for (Transition transition : transitionSetMap.keySet()) {
          if (!transitionMap.containsKey(transition)) {
            transitionMap.put(transition, transitionSetMap.get(transition));
          } else {
            transitionMap.get(transition).addAll(transitionSetMap.get(transition));
          }
        }
      }
      transitionsTable.put(danglingStateTransition.getKey(), transitionMap);
    }
    return new NFA(startState, successState, transitionsTable);
  }
}
