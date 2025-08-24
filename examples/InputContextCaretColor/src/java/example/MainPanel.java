// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea() {
      @Override public void updateUI() {
        super.updateUI();
        Caret caret = new InputContextCaret();
        caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
        setCaret(caret);
      }
    };
    textArea.setText("When IME is enabled, change the color of the caret.\n\n12345");
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

class InputContextCaret extends DefaultCaret {
  private final Color caretFg = UIManager.getColor("TextArea.caretForeground");

  @Override public void paint(Graphics g) {
    if (isVisible()) {
      JTextComponent c = getComponent();
      boolean b = c.getInputContext().isCompositionEnabled();
      c.setCaretColor(b ? Color.RED : caretFg);
    }
    super.paint(g);
  }
}
