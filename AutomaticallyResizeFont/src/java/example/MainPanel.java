// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TEST = "1234567890\nabcdefghijklmn";

  private MainPanel() {
    super(new BorderLayout());
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    JTextPane editor1 = new JTextPane();
    editor1.setFont(font);
    editor1.setText("Default\n" + TEST);

    JTextPane editor2 = new JTextPane() {
      private final Rectangle rect = new Rectangle();
      private float fontSize;
      @Override public void doLayout() {
        float f = .08f * SwingUtilities.calculateInnerArea(this, rect).width;
        boolean diff = Math.abs(fontSize - f) > 1.0e-1;
        if (diff) {
          setFont(font.deriveFont(f));
          fontSize = f;
        }
        super.doLayout();
      }
    };
    editor2.setFont(font);
    editor2.setText("doLayout + deriveFont\n" + TEST);

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor1, editor2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
    EventQueue.invokeLater(() -> split.setDividerLocation(.5));
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
