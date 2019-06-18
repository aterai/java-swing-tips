// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    GridLayout gl = new GridLayout(5, 7, 5, 5);
    JPanel p = new JPanel(gl);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JTextArea log = new JTextArea(3, 0);
    ActionListener al = e -> {
      Component c = (Component) e.getSource();
      int idx = p.getComponentZOrder(c);
      int row = idx / gl.getColumns();
      int col = idx % gl.getColumns();
      log.append(String.format("Row: %d, Column: %d%n", row + 1, col + 1));
    };
    IntStream.range(0, gl.getRows() * gl.getColumns()).mapToObj(i -> new JButton()).forEach(b -> {
      b.addActionListener(al);
      p.add(b);
    });

    add(p);
    add(new JScrollPane(log), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
