package jre;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A finite graph with edges labeled by characters of a finite alphabet or a special meta-character
 * that matches any character.
 */
public class NFA implements Matcher {

  int startState;
  final Map<Integer, Function<Character, Set<Integer>>> stateTransitions;
  final Map<Integer, Set<Integer>> stateEquivalences;
  final Set<Integer> successStates;

  public NFA(
      int startState,
      Map<Integer, Function<Character, Set<Integer>>> stateTransitions,
      Map<Integer, Set<Integer>> stateEquivalences,
      Set<Integer> successStates) {
    this.startState = startState;
    this.stateTransitions = stateTransitions;
    this.stateEquivalences = stateEquivalences;
    this.successStates = successStates;
  }

  @Override
  public boolean matches(String input) {
    return !Collections.disjoint(successStates, evaluate(input));
  }

  /**
   * Computes all possible end states for the input string.
   *
   * <p>This runs exponential in input size.
   */
  private Set<Integer> evaluate(String input) {
    Set<Integer> currentStates = new HashSet<>();
    currentStates.add(startState);
    expandStateEquivalences(currentStates);
    for (char c : input.toCharArray()) {
      Set<Integer> nextStates = new HashSet<>();
      for (int currentState : currentStates) {
        Function<Character, Set<Integer>> transition =
            stateTransitions.getOrDefault(currentState, x -> new HashSet<>());
        Set<Integer> transitionStates = transition.apply(c);
        expandStateEquivalences(transitionStates);
        nextStates.addAll(transitionStates);
      }
      currentStates = nextStates;
    }
    return currentStates;
  }

  private void expandStateEquivalences(Set<Integer> states) {
    int numStates;
    do {
      numStates = states.size();
      expandStateEquivalencesOneLevel(states);
    } while (states.size() > numStates);
  }

  private void expandStateEquivalencesOneLevel(Set<Integer> states) {
    stateEquivalences.entrySet()
        .stream()
        .filter(e -> states.contains(e.getKey()))
        .forEach(e -> states.addAll(e.getValue()));
  }
}
