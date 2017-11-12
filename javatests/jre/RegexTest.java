package jre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RegexTest {

  @Test
  public void testNoMetacharacters() {
    Regex regex = new Regex("abc");
    assertTrue(regex.matches("abc"));
    assertFalse(regex.matches("abcd"));
  }

  @Test
  public void testEscapedMetacharacters() {
    Regex regex = new Regex("a\\+bc");
    assertTrue(regex.matches("a+bc"));
    assertFalse(regex.matches("a\\+bcd"));
  }

  @Test
  public void testAlternation() {
    Regex regex = new Regex("ab|ba|bb");
    assertTrue(regex.matches("ab"));
    assertTrue(regex.matches("ba"));
    assertTrue(regex.matches("bb"));
    assertFalse(regex.matches("aa"));
    assertFalse(regex.matches("abba"));
  }
}
