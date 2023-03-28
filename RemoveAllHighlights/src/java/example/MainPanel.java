// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join("\n",
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      " use Swing components in building a user interface.",
      " First we examine the simplest Swing application you can write.",
      " Then we present several progressively complicated examples of creating",
      " user interfaces using components in the javax.swing package.",
      " We cover several Swing components, such as buttons, labels, and text areas.",
      " The handling of events is also discussed,",
      " as are layout management and accessibility.",
      " This lesson ends with a set of questions and exercises",
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html",
      "");
  private static final HighlightPainter HIGHLIGHT = new DefaultHighlightPainter(Color.YELLOW);

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea() {
      private transient WordHighlightListener handler;
      @Override public void updateUI() {
        removeCaretListener(handler);
        removeMouseListener(handler);
        removeKeyListener(handler);
        super.updateUI();
        handler = new WordHighlightListener();
        addCaretListener(handler);
        addMouseListener(handler);
        addKeyListener(handler);
      }
    };
    textArea.setSelectedTextColor(Color.BLACK);
    textArea.setLineWrap(true);
    textArea.setText(TEXT);

    JButton button1 = new JButton("removeAllHighlights");
    button1.setFocusable(false);
    button1.addActionListener(e -> textArea.getHighlighter().removeAllHighlights());

    JButton button2 = new JButton("removeWordHighlights");
    button2.setFocusable(false);
    button2.addActionListener(e -> removeWordHighlights(textArea));

    Box box = Box.createHorizontalBox();
    box.add(button1);
    box.add(Box.createHorizontalStrut(2));
    box.add(button2);

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void setHighlight(JTextComponent tc, String pattern) {
    removeWordHighlights(tc);
    try {
      Highlighter highlighter = tc.getHighlighter();
      Document doc = tc.getDocument();
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        pos = matcher.end();
        highlighter.addHighlight(matcher.start(), pos, HIGHLIGHT);
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(tc);
    }
  }

  public static void removeWordHighlights(JTextComponent tc) {
    Highlighter highlighter = tc.getHighlighter();
    for (Highlighter.Highlight hh : highlighter.getHighlights()) {
      if (hh.getPainter().equals(HIGHLIGHT)) {
        highlighter.removeHighlight(hh);
      }
    }
  }

  private static final class WordHighlightListener
      extends MouseAdapter
      implements CaretListener, KeyListener {
    private boolean dragActive;
    private boolean shiftActive;

    @Override public void caretUpdate(CaretEvent e) {
      if (!dragActive && !shiftActive) {
        fire(e.getSource());
      }
    }

    @Override public void mousePressed(MouseEvent e) {
      dragActive = true;
    }

    @Override public void mouseReleased(MouseEvent e) {
      dragActive = false;
      fire(e.getSource());
    }

    @Override public void keyPressed(KeyEvent e) {
      shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
    }

    @Override public void keyReleased(KeyEvent e) {
      shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
      if (!shiftActive) {
        fire(e.getSource());
      }
    }

    @Override public void keyTyped(KeyEvent e) {
      /* empty */
    }

    private void fire(Object c) {
      if (c instanceof JTextComponent) {
        JTextComponent tc = (JTextComponent) c;
        Caret caret = tc.getCaret();
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        int offs = tc.getCaretPosition();
        try {
          String word;
          if (p0 == p1) {
            int begOffs = Utilities.getWordStart(tc, offs);
            int endOffs = Utilities.getWordEnd(tc, offs);
            word = tc.getText(begOffs, endOffs - begOffs);
          } else {
            word = tc.getSelectedText();
          }
          word = word == null ? "" : word.trim();
          if (word.isEmpty()) {
            removeWordHighlights(tc);
          } else {
            setHighlight(tc, word);
          }
        } catch (BadLocationException ex) {
          tc.getHighlighter().removeAllHighlights();
        }
      }
    }
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
