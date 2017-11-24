package jre;

import java.util.Map;
import java.util.Set;

final class DFA {
  private final Node start;
  private final Set<Node> goals;

  DFA(Node start, Set<Node> goals) {
    this.start = start;
    this.goals = goals;
  }

  boolean matches(String input) {
    Node current = start;
    for (char c : input.toCharArray()) {
      current = current.transition(c);
    }
    return goals.contains(current);
  }
}

final class Node {
  private final Map<Character, Node> edges;

  Node(Map<Character, Node> edges) {
    this.edges = edges;
  }

  Node transition(char c) {
    if (edges.containsKey(c)) {
      return edges.get(c);
    } else {
      throw new IllegalStateException("No transition for " + c);
    }
  }
}
