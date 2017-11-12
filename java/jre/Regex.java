package jre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of regular expressions.
 *
 * <p>Special characters are .+*?()\ and are escaped by \.
 */
public class Regex implements Matcher {

  private final char[] pattern;

  public Regex(String pattern) {
    this.pattern = pattern.toCharArray();
  }

  @Override
  public boolean matches(String input) {
    return createNFA().matches(input);
  }

  private NFA createNFA() {
    List<NFA> leftConstructedNFAs = new LinkedList<>();
    LiteralNFABuilder builder = new LiteralNFABuilder(/* startState = */ 0);
    for (int index = 0; index < pattern.length; index++) {
      char c = pattern[index];
      switch (c) {
        case '|':
          leftConstructedNFAs.add(builder.build());
          builder = new LiteralNFABuilder(builder.currentState + 1);
          break;
        case '.':
          builder.addWildcardTransition();
          break;
        case '\\':
          c = pattern[++index];
          // Fall through is intentional here.
        default:
          // c is a character literal. Create a new state and add an edge to the old state's
          // transition function.
          builder.addLiteralTransition(c);
      }
    }
    leftConstructedNFAs.add(builder.build());
    AtomicInteger nextState = new AtomicInteger(builder.currentState);
    return leftConstructedNFAs.stream().reduce(alternateNFA(nextState)).get();
  }

  /**
   * WARNING: This is a destructive operation on both {@code nfa1} and {@code nfa2}.
   */
  private static BinaryOperator<NFA> alternateNFA(AtomicInteger nextState) {
    return (nfa1, nfa2) -> {
      nfa1.stateTransitions.putAll(nfa2.stateTransitions);
      nfa1.successStates.addAll(nfa2.successStates);
      Set<Integer> startStateEquivalences = new HashSet<>();
      startStateEquivalences.add(nfa1.startState);
      startStateEquivalences.add(nfa2.startState);
      int newState = nextState.incrementAndGet();
      nfa1.stateEquivalences.put(newState, startStateEquivalences);
      nfa1.startState = newState;
      return nfa1;
    };
  }

  /** A builder the constructs NFAs containing literals and wildcard characters. */
  private static class LiteralNFABuilder {

    private final int startState;
    private int currentState;
    private Map<Integer, Function<Character, Set<Integer>>> stateTransitions = new HashMap<>();

    private LiteralNFABuilder(int startState) {
      this.startState = startState;
      this.currentState = startState;
    }

    private LiteralNFABuilder addLiteralTransition(char c) {
      stateTransitions.put(currentState, transitionFunction(++currentState, c1 -> c1 == c));
      return this;
    }

    private LiteralNFABuilder addWildcardTransition() {
      stateTransitions.put(currentState, transitionFunction(++currentState, c -> true));
      return this;
    }

    private NFA build() {
      Set<Integer> successStates = new HashSet<>();
      successStates.add(currentState);
      return new NFA(startState, stateTransitions, new HashMap<>(), successStates);
    }

    private static Function<Character, Set<Integer>> transitionFunction(
        int nextState, Predicate<Character> condition) {
      return character -> {
        Set<Integer> nextStates = new HashSet<>();
        if (condition.test(character)) {
          nextStates.add(nextState);
        }
        return nextStates;
      };
    }
  }
}
