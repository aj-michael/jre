package jre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DFATest {

  @Test
  public void testMatching() {
    Map<Character, State> state1Map = new HashMap<>();
    State state1 = new State(state1Map);
    Map<Character, State> state2Map = new HashMap<>();
    State state2 = new State(state2Map);
    state1Map.put('a', state1);
    state1Map.put('b', state2);
    state2Map.put('a', state2);
    state2Map.put('b', state1);
    Set<State> solutionStates = new HashSet<>();
    solutionStates.add(state2);
    DFA oddNumberOfB = new DFA(state1, solutionStates);
    assertTrue(oddNumberOfB.matches("aabbabbaba"));
    assertTrue(oddNumberOfB.matches("bbbbb"));
    assertFalse(oddNumberOfB.matches("bbbab"));
    assertFalse(oddNumberOfB.matches("aaaaaa"));
    assertFalse(oddNumberOfB.matches(""));
  }
}
