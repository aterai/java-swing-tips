// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

// Java 16: public record Token(TokenType type, String value, int startIndex, int endIndex) {
@SuppressWarnings("PMD.DataClass")
public final class Token {
  private final TokenType type;
  private final String value;
  private final int startIndex;
  private final int endIndex;

  public Token(TokenType type, String value, int startIndex, int endIndex) {
    this.type = type;
    this.value = value;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public TokenType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  @Override public String toString() {
    return String.format(
        "Token(type=%s, value=%s, pos=%d-%d)", type, value, startIndex, endIndex);
  }
}
