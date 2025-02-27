// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextField field = new JTextField(12);
    Document doc = field.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new SizeFilter());
      // ((AbstractDocument) doc).setDocumentFilter(new DocumentSizeFilter(5));
    }

    ActionMap am = field.getActionMap();
    String delPrev = DefaultEditorKit.deletePrevCharAction; // "delete-previous";
    am.put(delPrev, new SilentDeleteTextAction(delPrev, am.get(delPrev)));

    String delNext = DefaultEditorKit.deleteNextCharAction; // "delete-next";
    am.put(delNext, new SilentDeleteTextAction(delNext, am.get(delNext)));

    add(makeTitledPanel("Default", new JTextField()));
    add(makeTitledPanel("Override delete-previous, delete-next beep", field));
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

class SilentDeleteTextAction extends TextAction {
  private final transient Action deleteAction;

  protected SilentDeleteTextAction(String name, Action deleteAction) {
    super(name);
    this.deleteAction = deleteAction;
  }

  @Override public void actionPerformed(ActionEvent e) {
    JTextComponent c = getTextComponent(e);
    if (Objects.isNull(c) || !c.isEditable() || !skipBeep(c)) {
      deleteAction.actionPerformed(e);
    }
  }

  private boolean skipBeep(JTextComponent textComponent) {
    Caret caret = textComponent.getCaret();
    int dot = caret.getDot();
    int mark = caret.getMark();
    boolean skip;
    if (DefaultEditorKit.deletePrevCharAction.equals(getValue(NAME))) {
      // @see javax/swing/text/DefaultEditorKit.java DeletePrevCharAction
      skip = dot == 0 && mark == 0;
    } else {
      // @see javax/swing/text/DefaultEditorKit.java DeleteNextCharAction
      Document doc = textComponent.getDocument();
      skip = dot == mark && doc.getLength() == dot;
    }
    return skip;
  }
}

class SizeFilter extends DocumentFilter {
  private static final int MAX = 5;

  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    int len = fb.getDocument().getLength();
    if (len + text.length() > MAX) {
      Toolkit.getDefaultToolkit().beep();
      return;
    }
    fb.insertString(offset, text, attr);
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    // fb.remove(offset, length);
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    int len = fb.getDocument().getLength();
    if (len - length + text.length() > MAX) {
      Toolkit.getDefaultToolkit().beep();
      return;
    }
    fb.replace(offset, length, text, attrs);
  }
}

// // https://docs.oracle.com/javase/tutorial/uiswing/components/generaltext.html
// // Text Component Features (The Java™ Tutorials > ... > Using Swing Components)
// // https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TextComponentDemoProject/src/components/DocumentSizeFilter.java
// class DocumentSizeFilter extends DocumentFilter {
//   int maxCharacters;
//   protected DocumentSizeFilter(int maxChars) {
//     maxCharacters = maxChars;
//   }
//
//   @Override public void insertString(DocumentFilter.FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
//     // This rejects the entire insertion if it would make
//     // the contents too long. Another option would be
//     // to truncate the inserted string so the contents
//     // would be exactly maxCharacters in length.
//     if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
//       super.insertString(fb, offs, str, a);
//     } else {
//       Toolkit.getDefaultToolkit().beep();
//     }
//   }
//
//   @Override public void replace(DocumentFilter.FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
//     // This rejects the entire replacement if it would make
//     // the contents too long. Another option would be
//     // to truncate the replacement string so the contents
//     // would be exactly maxCharacters in length.
//     if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
//       super.replace(fb, offs, length, str, a);
//     } else {
//       Toolkit.getDefaultToolkit().beep();
//     }
//   }
// }
