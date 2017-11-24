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
    Map<Character, Node> state1Map = new HashMap<>();
    Node state1 = new Node(state1Map);
    Map<Character, Node> state2Map = new HashMap<>();
    Node state2 = new Node(state2Map);
    state1Map.put('a', state1);
    state1Map.put('b', state2);
    state2Map.put('a', state2);
    state2Map.put('b', state1);
    Set<Node> solutionStates = new HashSet<>();
    solutionStates.add(state2);
    DFA oddNumberOfB = new DFA(state1, solutionStates);
    assertTrue(oddNumberOfB.matches("aabbabbaba"));
    assertTrue(oddNumberOfB.matches("bbbbb"));
    assertFalse(oddNumberOfB.matches("bbbab"));
    assertFalse(oddNumberOfB.matches("aaaaaa"));
    assertFalse(oddNumberOfB.matches(""));
  }
}
