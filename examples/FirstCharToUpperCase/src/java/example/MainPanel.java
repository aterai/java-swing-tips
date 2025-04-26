// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextField field = new JTextField();
    Document doc = field.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new FirstCharToUpperCaseDocumentFilter(field));
    }
    field.setText("abc");

    add(makeTitledPanel("Default", new JTextField("abc")));
    add(makeTitledPanel("FirstCharToUpperCase", field));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
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

class FirstCharToUpperCaseDocumentFilter extends DocumentFilter {
  private final JTextComponent textField;

  protected FirstCharToUpperCaseDocumentFilter(JTextComponent textField) {
    super();
    this.textField = textField;
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    Document doc = fb.getDocument();
    if (offset == 0 && doc.getLength() - length > 0) {
      fb.replace(length, 1, doc.getText(length, 1).toUpperCase(Locale.ENGLISH), null);
      textField.setCaretPosition(0);
    }
    fb.remove(offset, length);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    String str = text;
    if (offset == 0 && Objects.nonNull(text) && !text.isEmpty()) {
      str = text.substring(0, 1).toUpperCase(Locale.ENGLISH) + text.substring(1);
    }
    fb.replace(offset, length, str, attrs);
  }
}
