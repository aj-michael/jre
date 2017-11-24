package jre;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class NFA {

  private final State startState;
  private final State successState;
  final Map<State, Map<Transition, Set<State>>> transitionsTable;

  NFA(
      State startState,
      State successState,
      Map<State, Map<Transition, Set<State>>> transitionsTable) {
    this.startState = startState;
    this.successState = successState;
    this.transitionsTable = transitionsTable;
  }

  boolean evaluate(String input) {
    Set<State> currentStates = new HashSet<>();
    currentStates.add(startState);
    applyEmptyTransitions(currentStates);
    for (char c : input.toCharArray()) {
      Set<State> nextStates = new HashSet<>();
      for (State currentState : currentStates) {
        Map<Transition, Set<State>> transition =
            transitionsTable.getOrDefault(currentState, new HashMap<>());
        Set<State> newStates =
            transition
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() instanceof Transition.CharacterTransition)
                .filter(entry -> entry.getKey().accepts(c))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        applyEmptyTransitions(newStates);
        nextStates.addAll(newStates);
      }
      currentStates = nextStates;
    }
    return currentStates.contains(successState);
  }

  private void applyEmptyTransitions(Set<State> currentStates) {
    int numStates;
    do {
      numStates = currentStates.size();
      applyEmptyTransitionsOneLevel(currentStates);
    } while (currentStates.size() > numStates);
  }

  private void applyEmptyTransitionsOneLevel(Set<State> currentStates) {
    Set<State> newStates = new HashSet<>();
    for (State currentState : currentStates) {
      Map<Transition, Set<State>> currentStateTransitions = transitionsTable.get(currentState);
      if (currentStateTransitions == null) {
        continue;
      }
      Set<State> emptyTransitionStates = currentStateTransitions.get(Transition.EMPTY_TRANSITION);
      if (emptyTransitionStates == null) {
        continue;
      }
      newStates.addAll(emptyTransitionStates);
    }
    currentStates.addAll(newStates);
  }
}
