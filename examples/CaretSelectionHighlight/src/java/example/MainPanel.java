// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    desktop.add(makeInternalFrame("DefaultCaret", new Point(10, 10), makeTextArea(false)));
    desktop.add(makeInternalFrame("FocusCaret", new Point(50, 50), makeTextArea(true)));
    desktop.add(makeInternalFrame("FocusCaret", new Point(90, 90), makeTextArea(true)));
    EventQueue.invokeLater(() -> {
      JInternalFrame[] frames = desktop.getAllFrames();
      Arrays.asList(frames).forEach(f -> f.setVisible(true));
    });
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame makeInternalFrame(String title, Point p, Component c) {
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    f.add(c);
    f.setSize(200, 100);
    f.setLocation(p);
    return f;
  }

  private static Component makeTextArea(boolean flag) {
    JTextArea textArea = new JTextArea() {
      @Override public void updateUI() {
        setCaret(null);
        super.updateUI();
        if (flag) {
          Caret oldCaret = getCaret();
          int blinkRate = oldCaret.getBlinkRate();
          // int blinkRate = UIManager.getInt("TextField.caretBlinkRate")
          Caret caret = new FocusCaret();
          caret.setBlinkRate(blinkRate);
          setCaret(caret);
          caret.setSelectionVisible(true);
        }
      }
    };
    textArea.setText("aaa\nbbbbbb\ncccccccccccc\n");
    textArea.selectAll();
    return new JScrollPane(textArea);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class FocusCaret extends DefaultCaret {
  private static final Color COLOR = Color.GRAY.brighter();
  private static final HighlightPainter NO_FOCUS = new DefaultHighlightPainter(COLOR);

  @Override public void focusLost(FocusEvent e) {
    super.focusLost(e);
    setSelectionVisible(true);
  }

  @Override public void focusGained(FocusEvent e) {
    super.focusGained(e);
    // https://stackoverflow.com/questions/18237317/how-to-retain-selected-text-in-jtextfield-when-focus-lost
    setSelectionVisible(false); // removeHighlight
    setSelectionVisible(true); // addHighlight
    // TEST:
    // setVisible(true);
    // damage(getComponent().getBounds());
    // repaint();
  }

  @Override protected Highlighter.HighlightPainter getSelectionPainter() {
    // JComponent c = getComponent();
    // boolean selected = c.hasFocus();
    // Container f = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
    // if (f instanceof JInternalFrame) {
    //   JInternalFrame frame = (JInternalFrame) f;
    //   selected = frame.isSelected();
    // }
    // return selected ? DefaultHighlighter.DefaultPainter : nonFocusHighlightPainter;
    return getComponent().hasFocus() ? DefaultHighlighter.DefaultPainter : NO_FOCUS;
  }
}
