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
    JTextArea textArea = new LineCursorTextArea("Line Cursor Test\n\n*******");

    JCheckBox check = new JCheckBox("LineWrap");
    check.addActionListener(e -> {
      textArea.setLineWrap(((JCheckBox) e.getSource()).isSelected());
      textArea.requestFocusInWindow();
    });
    add(check, BorderLayout.NORTH);
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

class LineCursorTextArea extends JTextArea {
  private static final Color LINE_COLOR = Color.BLUE;

  // public LineCursorTextArea() {
  //   super();
  // }

  // public LineCursorTextArea(Document doc) {
  //   super(doc);
  // }

  // public LineCursorTextArea(Document doc, String text, int rows, int columns) {
  //   super(doc, text, rows, columns);
  // }

  // public LineCursorTextArea(int rows, int columns) {
  //   super(rows, columns);
  // }

  protected LineCursorTextArea(String text) {
    super(text);
  }

  // public LineCursorTextArea(String text, int rows, int columns) {
  //   super(text, rows, columns);
  // }

  @Override public void updateUI() {
    super.updateUI();
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
    super.paintComponent(g);
    Caret c = getCaret();
    if (c instanceof DefaultCaret) {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle r = SwingUtilities.calculateInnerArea(this, null);
      DefaultCaret caret = (DefaultCaret) c;
      int y = caret.y + caret.height - 1;
      g2.setPaint(LINE_COLOR);
      g2.drawLine(r.x, y, (int) r.getMaxX(), y);
      g2.dispose();
    }
  }

  // public static int getLineAtCaret(JTextComponent component) {
  //   int caretPosition = component.getCaretPosition();
  //   Element root = component.getDocument().getDefaultRootElement();
  //   return root.getElementIndex(caretPosition) + 1;
  // }
}
