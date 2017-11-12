package jre;

import java.util.Map;
import java.util.Set;

public final class DFA {
  private final State start;
  private final Set<State> goals;

  DFA(State start, Set<State> goals) {
    this.start = start;
    this.goals = goals;
  }

  boolean matches(String input) {
    State current = start;
    for (char c : input.toCharArray()) {
      current = current.transition(c);
    }
    return goals.contains(current);
  }
}

final class State {
  private final Map<Character, State> edges;

  State(Map<Character, State> edges) {
    this.edges = edges;
  }

  State transition(char c) {
    if (edges.containsKey(c)) {
      return edges.get(c);
    } else {
      throw new IllegalStateException("No transition for " + c);
    }
  }
}
