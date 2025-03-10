// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String txt = "The quick brown fox jumps over the lazy dog.";
    String repeat = String.join("\n", Collections.nCopies(3, txt));

    JTextArea textArea1 = new JTextArea("default\n" + repeat);
    JTextArea textArea2 = new JTextArea("setCaret\n" + repeat) {
      @Override public void updateUI() {
        setCaret(null);
        super.updateUI();
        Caret oldCaret = getCaret();
        int blinkRate = oldCaret.getBlinkRate();
        Caret caret = new SelectWordCaret();
        caret.setBlinkRate(blinkRate);
        setCaret(caret);
      }
    };
    add(new JScrollPane(textArea1));
    add(new JScrollPane(textArea2));
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

enum SelectingMode {
  CHAR, WORD, ROW
}

class SelectWordCaret extends DefaultCaret {
  private SelectingMode selectingMode = SelectingMode.CHAR;
  private int p0; // = Math.min(getDot(), getMark());
  private int p1; // = Math.max(getDot(), getMark());

  @Override public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
    int clickCount = e.getClickCount();
    if (SwingUtilities.isLeftMouseButton(e) && !e.isConsumed() && clickCount > 1) {
      boolean isDoubleClicked = clickCount == 2;
      if (isDoubleClicked) {
        selectingMode = SelectingMode.WORD;
        p0 = Math.min(getDot(), getMark());
        p1 = Math.max(getDot(), getMark());
      } else {
        selectingMode = SelectingMode.ROW;
        JTextComponent target = getComponent();
        int offs = target.getCaretPosition();
        try {
          p0 = Utilities.getRowStart(target, offs);
          p1 = Utilities.getRowEnd(target, offs);
          setDot(p0);
          moveDot(p1);
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
      }
    } else {
      selectingMode = SelectingMode.CHAR;
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (!e.isConsumed() && SwingUtilities.isLeftMouseButton(e)) {
      if (selectingMode == SelectingMode.WORD) {
        continuouslySelectWords(e);
      } else if (selectingMode == SelectingMode.ROW) {
        continuouslySelectRows(e);
      }
    } else {
      super.mouseDragged(e);
    }
  }

  private int getCaretPositionByLocation(JTextComponent c, Position.Bias[] biasRet, Point pt) {
    int pos = c.getUI().viewToModel(c, pt, biasRet);
    // Java 9: int pos = c.getUI().viewToModel2D(c, pt, biasRet);
    if (biasRet[0] == null) {
      biasRet[0] = Position.Bias.Forward;
    }
    return pos;
  }

  private void continuouslySelectWords(MouseEvent e) {
    Position.Bias[] biasRet = new Position.Bias[1];
    JTextComponent c = getComponent();
    int pos = getCaretPositionByLocation(c, biasRet, e.getPoint());
    try {
      if (p0 < pos && pos < p1) {
        setDot(p0);
        moveDot(p1, biasRet[0]);
      } else if (p1 < pos) {
        setDot(p0);
        moveDot(Utilities.getWordEnd(c, pos), biasRet[0]);
      } else if (p0 > pos) {
        setDot(p1);
        moveDot(Utilities.getWordStart(c, pos), biasRet[0]);
      }
    } catch (BadLocationException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
    }
  }

  private void continuouslySelectRows(MouseEvent e) {
    Position.Bias[] biasRet = new Position.Bias[1];
    JTextComponent c = getComponent();
    int pos = getCaretPositionByLocation(c, biasRet, e.getPoint());
    try {
      if (p0 < pos && pos < p1) {
        setDot(p0);
        moveDot(p1, biasRet[0]);
      } else if (p1 < pos) {
        setDot(p0);
        moveDot(Utilities.getRowEnd(c, pos), biasRet[0]);
      } else if (p0 > pos) {
        setDot(p1);
        moveDot(Utilities.getRowStart(c, pos), biasRet[0]);
      }
    } catch (BadLocationException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
    }
  }

  @Override public boolean equals(Object o) {
    boolean b = o instanceof SelectWordCaret && equals2((SelectWordCaret) o);
    return this == o || b;
  }

  private boolean equals2(SelectWordCaret that) {
    return super.equals(that) && p0 == that.p0 && p1 == that.p1;
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), p0, p1);
  }
}
