// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new HighlightCursorTextArea();
    textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    textArea.setText("Highlight Cursor Test\n\n**************************************");

    JCheckBox check = new JCheckBox("LineWrap");
    check.addActionListener(e -> {
      textArea.setLineWrap(check.isSelected());
      textArea.requestFocusInWindow();
    });
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.getViewport().setBackground(Color.WHITE);
    add(check, BorderLayout.NORTH);
    add(scroll);
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

class HighlightCursorTextArea extends JTextArea {
  private static final Color LINE_COLOR = new Color(0xFA_FA_DC);
  private final Rectangle rect = new Rectangle();

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    Caret caret = new DefaultCaret() {
      // [UnsynchronizedOverridesSynchronized]
      // Unsynchronized method damage overrides synchronized method in DefaultCaret
      @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
      @Override protected synchronized void damage(Rectangle r) {
        if (Objects.nonNull(r)) {
          JTextComponent c = getComponent();
          x = 0;
          y = r.y;
          width = c.getSize().width;
          height = r.height;
          c.repaint();
        }
      }
    };
    // caret.setBlinkRate(getCaret().getBlinkRate());
    caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
    setCaret(caret);
  }

  @Override protected void paintComponent(Graphics g) {
    Caret c = getCaret();
    if (c instanceof DefaultCaret) {
      Graphics2D g2 = (Graphics2D) g.create();
      DefaultCaret caret = (DefaultCaret) c;
      Rectangle r = SwingUtilities.calculateInnerArea(this, rect);
      r.y = caret.y;
      r.height = caret.height;
      g2.setPaint(LINE_COLOR);
      g2.fill(r);
      g2.dispose();
    }
    super.paintComponent(g);
  }
}
