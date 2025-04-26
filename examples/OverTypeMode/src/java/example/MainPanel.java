// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    OverTypeTextArea textArea = new OverTypeTextArea();
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    textArea.setText("Press the INSERT key to toggle the overwrite mode.\nあああ\n1234567890");
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

// https://community.oracle.com/thread/1385467 JTextPane edit mode (insert or overwrite)???
class OverTypeTextArea extends JTextArea {
  private boolean overTypeMode = true;
  private transient Caret defaultCaret;
  private transient Caret overtypeCaret;

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      // setCaretColor(Color.RED);
      defaultCaret = getCaret();
      overtypeCaret = new OverTypeCaret();
      overtypeCaret.setBlinkRate(defaultCaret.getBlinkRate());
      setOverTypeMode(overTypeMode);
    });
  }

  public boolean isOverTypeMode() {
    return overTypeMode;
  }

  /*
   * Set the caret to use depending on over-type/insert mode
   */
  public void setOverTypeMode(boolean isOverType) {
    this.overTypeMode = isOverType;
    int pos = getCaretPosition();
    if (isOverTypeMode()) {
      setCaret(overtypeCaret);
    } else {
      setCaret(defaultCaret);
    }
    setCaretPosition(pos);
  }

  /*
   * Override method from JComponent
   */
  @Override public void replaceSelection(String text) {
    // Implement over-type mode by selecting the character at the current
    // caret position
    if (isOverTypeMode()) {
      int pos = getCaretPosition();
      if (getSelectionStart() == getSelectionEnd() && pos < getDocument().getLength()) {
        moveCaretPosition(pos + 1);
      }
    }
    super.replaceSelection(text);
  }

  /*
   * Override method from JComponent
   */
  @Override protected void processKeyEvent(KeyEvent e) {
    super.processKeyEvent(e);
    // Handle release of Insert key to toggle over-type/insert mode
    if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_INSERT) {
      setCaretPosition(getCaretPosition()); // add
      moveCaretPosition(getCaretPosition()); // add
      setOverTypeMode(!isOverTypeMode());
      e.getComponent().repaint(); // add
    }
  }

  /*
   * Paint a horizontal line the width of a column and 1 pixel high
   */
  private final class OverTypeCaret extends DefaultCaret {
    /*
     * The over-type caret will simply be a horizontal line one pixel high
     * (once we determine where to paint it)
     */
    @Override public void paint(Graphics g) {
      if (isVisible()) {
        try {
          JTextComponent component = getComponent();
          TextUI mapper = component.getUI();
          Rectangle r = mapper.modelToView(component, getDot());
          // Java 9:
          // Rectangle rect = mapper.modelToView2D(c, pos, Position.Bias.Forward).getBounds();
          g.setColor(component.getCaretColor());
          int width = g.getFontMetrics().charWidth('w');
          // A patch for full width characters >>>>
          if (isOverTypeMode()) {
            int pos = getCaretPosition();
            if (pos < getDocument().getLength()) {
              if (getSelectionStart() == getSelectionEnd()) {
                String str = getText(pos, 1);
                width = g.getFontMetrics().stringWidth(str);
              } else {
                width = 0;
              }
            }
          } // <<<<
          int y = r.y + r.height - 2;
          g.drawLine(r.x, y, r.x + width - 2, y);
        } catch (BadLocationException ex) {
          // should never happen
          RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
          wrap.initCause(ex);
          throw wrap;
        }
      }
    }

    /*
     * Damage must be overridden whenever the paint method is overridden
     * (The damaged area is the area the caret is painted in. We must
     * consider the area for the default caret and this caret)
     */
    // [UnsynchronizedOverridesSynchronized]
    // Unsynchronized method damage overrides synchronized method in DefaultCaret
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    @Override protected synchronized void damage(Rectangle r) {
      if (Objects.nonNull(r)) {
        JTextComponent c = getComponent();
        x = r.x;
        y = r.y;
        // width = c.getFontMetrics(c.getFont()).charWidth('w');
        // width = c.getFontMetrics(c.getFont()).charWidth('\u3042');
        width = c.getFontMetrics(c.getFont()).charWidth('あ');
        height = r.height;
        c.repaint();
      }
    }
  }
}
