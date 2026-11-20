// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
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

// https://community.oracle.com/tech/developers/discussion/1385467
// JTextPane edit mode (insert or overwrite)???
class OverTypeTextArea extends JTextArea {
  private boolean overTypeMode = true;
  private transient Caret defaultCaret;
  private transient Caret overTypeCaret;

  @Override public void updateUI() {
    // The UI only replaces a caret that is null or a UIResource, so restore
    // the default caret first to avoid keeping the OverTypeCaret as the default one
    if (getCaret() instanceof OverTypeCaret) {
      setCaret(defaultCaret);
    }
    super.updateUI();
    // Deferred because this method is called from the super constructor
    // before the field initializer of overTypeMode is executed
    EventQueue.invokeLater(() -> {
      // setCaretColor(Color.RED);
      defaultCaret = getCaret();
      overTypeCaret = new OverTypeCaret();
      overTypeCaret.setBlinkRate(defaultCaret.getBlinkRate());
      setOverTypeMode(overTypeMode);
    });
  }

  public boolean isOverTypeMode() {
    return overTypeMode;
  }

  /*
   * Set the caret to use depending on over-type/insert mode
   */
  public void setOverTypeMode(boolean overType) {
    this.overTypeMode = overType;
    int pos = getCaretPosition();
    setCaret(overType ? overTypeCaret : defaultCaret);
    setCaretPosition(pos);
  }

  /*
   * Returns the number of chars of the character at the given position
   * that can be overwritten: 2 for a surrogate pair, 0 for a line feed
   * or the end of the document, otherwise 1.
   */
  protected int getOverTypeLength(int pos) {
    int len = Math.min(2, getDocument().getLength() - pos);
    try {
      int codePoint = len > 0 ? getText(pos, len).codePointAt(0) : '\n';
      return codePoint == '\n' ? 0 : Character.charCount(codePoint);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  /*
   * Override method from JComponent
   */
  @Override public void replaceSelection(String text) {
    // Implement over-type mode by selecting the character at the current
    // caret position (line feeds are not overwritten)
    if (isOverTypeMode() && getSelectionStart() == getSelectionEnd()) {
      int pos = getCaretPosition();
      moveCaretPosition(pos + getOverTypeLength(pos));
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
      // Clear the selection, otherwise its highlight is left behind
      // by the uninstalled caret
      setCaretPosition(getCaretPosition());
      setOverTypeMode(!isOverTypeMode());
      repaint();
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
          JTextComponent c = getComponent();
          Rectangle r = c.getUI().modelToView(c, getDot());
          // Java 9:
          // Rectangle r = c.getUI().modelToView2D(c, getDot(), Position.Bias.Forward).getBounds();
          g.setColor(c.getCaretColor());
          int width = getOverTypeCaretWidth(g.getFontMetrics());
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

    private int getOverTypeCaretWidth(FontMetrics fm) throws BadLocationException {
      int pos = getDot();
      int len = getDot() == getMark() ? getOverTypeLength(pos) : 0;
      // A patch for full width characters and surrogate pairs
      return len > 0 ? fm.stringWidth(getText(pos, len)) : fm.charWidth('w');
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
        // A full width character is wider than 'w'
        width = c.getFontMetrics(c.getFont()).charWidth('あ');
        height = r.height;
        // Repaint only the caret area instead of the whole component
        repaint();
      }
    }
  }
}
