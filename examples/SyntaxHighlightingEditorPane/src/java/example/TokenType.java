// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

public enum TokenType {
  KEYWORD("kwd"),
  COMMENT_LINE("com"),
  COMMENT_BLOCK("com"),
  STRING("str"),
  CHAR("str"),
  NUMBER("lit"),
  ANNOTATION("lit"),
  TYPE("typ"),
  PUNCTUATION("pun"),
  IDENTIFIER("pln"),
  PLAIN("pln"),
  WHITESPACE("pln");

  private final String cssClass;

  TokenType(String cssClass) {
    this.cssClass = cssClass;
  }

  public String getCssClass() {
    return cssClass;
  }
}
