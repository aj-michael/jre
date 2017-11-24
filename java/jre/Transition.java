package jre;

interface Transition {

  boolean accepts(char c);

  Transition EMPTY_TRANSITION = c -> true;

  final class CharacterTransition implements Transition {
    final char value;

    CharacterTransition(char value) {
      this.value = value;
    }

    @Override
    public boolean accepts(char c) {
      return c == value;
    }
  }
}
