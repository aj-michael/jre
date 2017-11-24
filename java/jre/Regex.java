package jre;

import java.util.Deque;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Implementation of regular expressions.
 *
 * <p>Special characters are .+*|?()\ and are escaped by \.
 */
public class Regex implements Matcher {

  private final char[] pattern;

  public Regex(String pattern) {
    this.pattern = pattern.toCharArray();
  }

  @Override
  public boolean matches(String input) {
    return toNFA().evaluate(input);
  }

  public NFA toNFA() {
    return createNFA(pattern, 0, pattern.length).toNFA();
  }

  private static PartialNFA createNFA(char[] pattern, int start, int len) {
    TreeMap<Integer, Integer> outerParenLocations = new TreeMap<>();
    int depth = 0;
    int leftParen = -1;
    for (int index = start; index < start + len; index++) {
      char c = pattern[index];
      if (c == '(') {
        depth++;
        if (leftParen == -1) {
          leftParen = index;
        }
      } else if (c == ')') {
        depth--;
        if (depth < 0) {
          throw new IllegalArgumentException("Invalid paren nesting");
        } else if (depth == 0) {
          outerParenLocations.put(leftParen, index);
          leftParen = -1;
        }
      }
    }

    // At this point, outerParens contains the locations of all top-level parens in order

    Deque<PartialNFA> alternatedStack = new LinkedList<>();
    Deque<PartialNFA> stack = new LinkedList<>();

    for (int index = start; index < start + len; index++) {
      char c = pattern[index];
      if (outerParenLocations.containsKey(index)) {
        int closingParenLocation = outerParenLocations.get(index);
        int newLen = closingParenLocation - index - 1;
        stack.push(createNFA(pattern, index + 1, newLen));
        index = closingParenLocation;
      } else if (c == '|') {
        PartialNFA finalNFA = stack.pop();
        while (!stack.isEmpty()) {
          finalNFA = PartialNFA.concatenateNFAs(stack.pop(), finalNFA);
        }
        alternatedStack.push(finalNFA);
      } else if (c == '\\') {
        c = pattern[++index];
        stack.push(PartialNFA.literalNFA(c));
      } else if (c == '*') {
        stack.push(PartialNFA.makeOptionalRepeatable(stack.pop()));
      } else if (c == '+') {
        stack.push(PartialNFA.makeRepeatable(stack.pop()));
      } else if (c == '?') {
        stack.push(PartialNFA.makeOptional(stack.pop()));
      } else if (c == '.') {
        stack.push(PartialNFA.wildcardNFA());
      } else {
        stack.push(PartialNFA.literalNFA(c));
      }
    }

    PartialNFA finalNFA = stack.pop();
    while (!stack.isEmpty()) {
      finalNFA = PartialNFA.concatenateNFAs(stack.pop(), finalNFA);
    }
    while (!alternatedStack.isEmpty()) {
      finalNFA = PartialNFA.alternateNFAs(alternatedStack.pop(), finalNFA);
    }

    return finalNFA;
  }
}
