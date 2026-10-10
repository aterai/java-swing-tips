// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.BreakIterator;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join(
      "\n",
      "Abc-Def_Ghi",
      "abc-def_ghi",
      "aa1-bb2_cc3",
      "aa_(bb)_cc;",
      "11-22_33");

  private MainPanel() {
    super(new BorderLayout());
    Action selectWordAction = new TextAction(DefaultEditorKit.selectWordAction) {
      @Override public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
          try {
            int pos = target.getCaretPosition();
            int start = TextUtils.getWordStart(target, pos);
            int end = TextUtils.getWordEnd(target, pos);
            target.setCaretPosition(start);
            target.moveCaretPosition(end);
          } catch (BadLocationException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(target);
          }
        }
      }
    };
    JTextArea textArea = new JTextArea(TEXT);
    textArea.getActionMap().put(DefaultEditorKit.selectWordAction, selectWordAction);
    Component c1 = createTitledPanel("Default", new JTextArea(TEXT));
    Component c2 = createTitledPanel("Break words: _ and -", textArea);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, c1, c2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.add(new JLabel(title), BorderLayout.NORTH);
    p.add(new JScrollPane(c));
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
      Logger.getGlobal().severe(ex::getMessage);
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

final class TextUtils {
  private static final String DELIMITERS = "_-";

  private TextUtils() {
    /* HideUtilityClassConstructor */
  }

  // @see javax.swing.text.Utilities#getWordStart(JTextComponent, int)
  public static int getWordStart(JTextComponent c, int offs) throws BadLocationException {
    Element line = getParagraphElement(c, offs);
    int lineStart = line.getStartOffset();
    Segment seg = getLineText(c.getDocument(), line);
    int start = offs;
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      // Clamp to the last character of the line (e.g. clicked past the line end)
      int pos = Math.min(offs - lineStart, seg.count - 1);
      // BreakIterator indices start at seg.offset (the Segment's begin index),
      // while Segment#charAt(int) takes an index relative to the line start
      words.following(seg.offset + pos);
      start = lineStart + words.previous() - seg.offset;
      for (int i = lineStart + pos; i > start; i--) {
        if (isDelimiter(seg.charAt(i - lineStart))) {
          start = i + 1;
          break;
        }
      }
    }
    return start;
  }

  // @see javax.swing.text.Utilities#getWordEnd(JTextComponent, int)
  public static int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
    Element line = getParagraphElement(c, offs);
    int lineStart = line.getStartOffset();
    Segment seg = getLineText(c.getDocument(), line);
    int end = offs;
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      int pos = Math.min(offs - lineStart, seg.count - 1);
      end = lineStart + words.following(seg.offset + pos) - seg.offset;
      for (int i = offs; i < end; i++) {
        if (isDelimiter(seg.charAt(i - lineStart))) {
          end = i;
          break;
        }
      }
    }
    return end;
  }

  private static boolean isDelimiter(char ch) {
    return DELIMITERS.indexOf(ch) >= 0;
  }

  private static Element getParagraphElement(JTextComponent c, int offs)
      throws BadLocationException {
    return Optional.ofNullable(Utilities.getParagraphElement(c, offs))
        .orElseThrow(() -> new BadLocationException("No word at " + offs, offs));
  }

  // Excludes the implicit newline at the end of the document
  private static Segment getLineText(Document doc, Element line) throws BadLocationException {
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    Segment seg = new Segment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    return seg;
  }
}
