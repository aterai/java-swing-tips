// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class JavaSyntaxHighlighter {
  // Java 11: private static final Set<String> KEYWORDS = Set.of(
  private static final Set<String> KEYWORDS = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(
          "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
          "class", "const", "continue", "default", "do", "double", "else", "enum",
          "extends", "false", "final", "finally", "float", "for", "goto", "if",
          "implements", "import", "instanceof", "int", "interface", "long", "native",
          "new", "null", "package", "private", "protected", "public", "return", "short",
          "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
          "throws", "transient", "true", "try", "void", "volatile", "while")));

  // Checked in this order (highest priority first).
  // All groups inside each regex must be non-capturing, because the index of
  // the capturing group in the combined pattern identifies the matched rule.
  // Java 11: private static final List<TokenRule> TOKEN_RULES = List.of(
  private static final List<TokenRule> TOKEN_RULES = Collections.unmodifiableList(Arrays.asList(
      new TokenRule("/\\*[\\s\\S]*?\\*/", TokenType.COMMENT_BLOCK),
      new TokenRule("//[^\r\n]*", TokenType.COMMENT_LINE),
      new TokenRule("\"(?:\\\\.|[^\"\\\\\r\n])*\"", TokenType.STRING),
      new TokenRule("'(?:\\\\.|[^'\\\\\r\n])+'", TokenType.CHAR),
      // The floating point alternatives must precede the integer one,
      // otherwise "3.14" is split into "3", "." and "14".
      new TokenRule("0[xX][0-9a-fA-F_]+[lL]?"
          + "|\\d[\\d_]*\\.[\\d_]*(?:[eE][+-]?\\d+)?[fFdD]?"
          + "|\\.\\d[\\d_]*(?:[eE][+-]?\\d+)?[fFdD]?"
          + "|\\d[\\d_]*(?:[eE][+-]?\\d+)?[fFdDlL]?", TokenType.NUMBER),
      new TokenRule("@[a-zA-Z_$][a-zA-Z0-9_$]*", TokenType.ANNOTATION),
      // Keywords and type names are classified later by classifyIdentifier()
      new TokenRule("[a-zA-Z_$][a-zA-Z0-9_$]*", TokenType.IDENTIFIER),
      new TokenRule("[{};,()\\[\\].<>]", TokenType.PUNCTUATION),
      new TokenRule("\\s+", TokenType.WHITESPACE),
      // Single character fallback, so that the combined pattern always matches
      new TokenRule("[\\s\\S]", TokenType.PLAIN)));

  private static final Pattern TOKEN_PATTERN = Pattern.compile(TOKEN_RULES.stream()
      .map(rule -> "(" + rule.getRegex() + ")")
      .collect(Collectors.joining("|")));

  // Java 16: private record TokenRule(String regex, TokenType type) {
  private static final class TokenRule {
    private final String regex;
    private final TokenType type;

    private TokenRule(String regex, TokenType type) {
      this.regex = regex;
      this.type = type;
    }

    private String getRegex() {
      return regex;
    }

    private TokenType getType() {
      return type;
    }
  }

  private JavaSyntaxHighlighter() {
    /* Singleton */
  }

  public static String highlight(String sourceCode) {
    return generateHtml(tokenize(sourceCode));
  }

  private static List<Token> tokenize(String source) {
    List<Token> tokens = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(source);
    // The fallback rule matches any character, so each find() starts
    // exactly at the end of the previous token.
    while (matcher.find()) {
      String value = matcher.group();
      TokenType type = getMatchedType(matcher);
      if (type == TokenType.IDENTIFIER) {
        type = classifyIdentifier(value);
      }
      tokens.add(new Token(type, value, matcher.start(), matcher.end()));
    }
    return tokens;
  }

  private static TokenType getMatchedType(Matcher matcher) {
    int index = 0;
    while (matcher.start(index + 1) < 0) {
      index++;
    }
    return TOKEN_RULES.get(index).getType();
  }

  private static TokenType classifyIdentifier(String value) {
    TokenType type;
    if (KEYWORDS.contains(value)) {
      type = TokenType.KEYWORD;
    } else if (Character.isUpperCase(value.charAt(0))) {
      type = TokenType.TYPE;
    } else {
      type = TokenType.PLAIN;
    }
    return type;
  }

  private static String generateHtml(List<Token> tokens) {
    StringBuilder html = new StringBuilder(500);
    for (Token token : tokens) {
      TokenType type = token.getType();
      if (type == TokenType.WHITESPACE) {
        // Whitespace contains no characters that need to be escaped
        html.append(normalizeWhitespace(token.getValue()));
      } else if (type == TokenType.PLAIN) {
        html.append(escapeHtml(token.getValue()));
      } else {
        html.append("<span class=\"").append(type.getCssClass()).append("\">")
            .append(escapeHtml(token.getValue())).append("</span>");
      }
    }
    return html.toString();
  }

  private static String escapeHtml(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static String normalizeWhitespace(String whitespace) {
    // Replace spaces first so that a tab becomes 4 plain spaces, keep \n and \r as is
    return whitespace.replace(" ", "&#160;").replace("\t", "    ");
  }
}
