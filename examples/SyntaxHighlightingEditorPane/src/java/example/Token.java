// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

public record Token(TokenType type, String value, int startIndex, int endIndex) {
  @Override public String toString() {
    return String.format(
        "Token(type=%s, value=%s, pos=%d-%d)", type, value, startIndex, endIndex);
  }
}
