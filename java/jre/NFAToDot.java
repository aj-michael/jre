package jre;

import java.util.Map;
import java.util.Set;

public class NFAToDot {

  private NFAToDot() {}

  public static String convert(NFA nfa) {
    StringBuilder builder = new StringBuilder();
    builder.append("digraph {\n");
    for (State fromState : nfa.transitionsTable.keySet()) {
      Map<Transition, Set<State>> transitions = nfa.transitionsTable.get(fromState);
      for (Transition transition : transitions.keySet()) {
        for (State toState : transitions.get(transition)) {
          if (transition.equals(Transition.EMPTY_TRANSITION)) {
            builder.append(String.format("%s -> %s\n", fromState, toState));
          } else {
            Transition.CharacterTransition characterTransition =
                (Transition.CharacterTransition) transition;
            builder.append(
                String.format(
                    "%s -> %s [label=%s]\n", fromState, toState, characterTransition.value));
          }
        }
      }
    }
    builder.append("}\n");
    return builder.toString();
  }
}
