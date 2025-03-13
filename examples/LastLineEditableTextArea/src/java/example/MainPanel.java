// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private static final String MESSAGE = "Can only edit last line, version 0.0\n";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    textArea.setMargin(new Insets(2, 5, 2, 2));
    textArea.setText(MESSAGE + NonEditableLineDocumentFilter.PROMPT);
    Document doc = textArea.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new NonEditableLineDocumentFilter());
    }
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class NonEditableLineDocumentFilter extends DocumentFilter {
  public static final String LB = "\n";
  public static final String PROMPT = "> ";

  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    if (Objects.nonNull(text)) {
      replace(fb, offset, 0, text, attr);
    }
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    Element root = doc.getDefaultRootElement();
    int count = root.getElementCount();
    int index = root.getElementIndex(offset);
    Element cur = root.getElement(index);
    int promptPosition = cur.getStartOffset() + PROMPT.length();
    if (index == count - 1 && offset - promptPosition >= 0) {
      String str = text;
      if (LB.equals(str)) {
        String line = doc.getText(promptPosition, offset - promptPosition);
        // String[] args = line.split("\\s");
        List<String> args = Stream.of(line.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        if (args.isEmpty() || args.get(0).isEmpty()) {
          str = String.format("%n%s", PROMPT);
        } else {
          str = String.format("%n%s: command not found%n%s", args.get(0), PROMPT);
        }
      }
      fb.replace(offset, length, str, attrs);
    }
  }
}
